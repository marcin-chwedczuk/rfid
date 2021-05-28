package pl.marcinchwedczuk.rfid.card.fake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.ATR;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static pl.marcinchwedczuk.rfid.card.commons.StringUtils.byteArrayFromHexString;
import static pl.marcinchwedczuk.rfid.card.commons.StringUtils.toHexString;

class FakeMifare1K {
    private static final Logger logger = LoggerFactory.getLogger(FakeMifare1K.class);

    /**
     * Capital letter 'C' - means negation of bit.
     */
    public static final String ACCESS_BITS_POSITIONS =
            "C23 C22 C21 C20 C13 C12 C11 C10 " +
            "c13 c12 c11 c10 C33 C32 C31 C30 " +
            "c33 c32 c31 c30 c23 c22 c21 c20";

    private MifareSector[] sectors = new MifareSector[16];
    {
        for (int i = 0; i < sectors.length; i++) {
            sectors[i] = new MifareSector();
        }
    }

    private int piccOperatingParameter = 0xff;

    private byte[] keyRegister0 = null;
    private byte[] keyRegister1 = null;

    private KeyType authenticatedUsingKey = null;
    private int authenticatedSectorIndex = -1;

    public ATR getATR() {
        return new ATR(byteArrayFromHexString(
                "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A"
        ));
    }

    public ResponseAPDU execute(CommandAPDU cmd) throws CardException {
        if (matchesPattern(cmd, "FF CA 00 00 04") ) {
            // Get Card ID
            return new ResponseAPDU(byteArrayFromHexString(
                    // Card ID LSB - MSB, SW1, SW2
                    "AA BB CC DD EE FF 90 00"
            ));
        }

        if (matchesPattern(cmd, "FF CA 01 00 04")) {
            // Get ATS (Answer to Select)
            throw new RuntimeException("Not implemented.");
        }

        if(matchesPattern(cmd, "FF 82 00 (00|01) 06 .. .. .. .. .. ..")) {
            // Load key into register
            byte[] key = cmd.getData().clone();

            // See ACR122 API specification to understand P1, P2 and other stuff
            if (cmd.getP2() == 0) {
                keyRegister0 = key;
            } else {
                keyRegister1 = key;
            }

            return new ResponseAPDU(byteArrayFromHexString("90 00"));
        }

        if (matchesPattern(cmd, "FF 86 00 00 05 01 00 .. .. ..")) {
            // Authenticate to sector
            byte[] data = cmd.getData();

            int blockNumber = Byte.toUnsignedInt(data[2]);
            int sectorIndex = blockNumber / 4;

            boolean keyA = (data[3] == 0x60); // 0x61 for KEY_B
            int register = data[4]; // 0 or 1

            byte[] loadedKey = (register == 0) ? keyRegister0 : keyRegister1;

            boolean success = sectors[sectorIndex].authenticate(keyA, loadedKey);
            if (success) {
                authenticatedSectorIndex = sectorIndex;
                authenticatedUsingKey = keyA ? KeyType.KEY_A : KeyType.KEY_B;
                return new ResponseAPDU(byteArrayFromHexString("90 00"));
            }
            else {
                authenticatedSectorIndex = -1;
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }
        }

        if (matchesPattern(cmd, "FF B0 00 .. ..")) {
            // Read data
            byte[] data = cmd.getBytes();

            byte blockNumber = data[3];
            byte nBytes = data[4];

            int sectorIndex = blockNumber / 4;
            int blockIndex = blockNumber % 4;

            if (authenticatedSectorIndex != sectorIndex) {
                // Not authenticated
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }

            byte[] dataToReturn = sectors[sectorIndex].read(
                    blockIndex,
                    nBytes,
                    authenticatedUsingKey);

            if (dataToReturn == null) {
                // Access not allowed
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }

            return new ResponseAPDU(byteArrayFromHexString(
                    toHexString(dataToReturn) +  " 90 00"));
        }

        if (matchesPattern(cmd, "FF D6 00 .. .." +
                " .. .. .. .. .. .. .. .. .. .. .. .. .. .. .. ..")) {
            // Write / Update
            byte[] data = cmd.getBytes();

            byte blockNumber = data[3];
            byte nBytes = data[4];

            int sectorIndex = blockNumber / 4;
            int blockIndex = blockNumber % 4;

            byte[] blockData = Arrays.copyOfRange(data, 5, 5 + 16);
            if (blockData.length != 16) {
                // Invalid block data length
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }

            if (authenticatedSectorIndex != sectorIndex) {
                // Not authenticated
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }

            boolean success = sectors[sectorIndex].write(blockIndex, blockData, authenticatedUsingKey);
            if (!success) {
                // Write failed
                return new ResponseAPDU(byteArrayFromHexString("63 00"));
            }

            return new ResponseAPDU(byteArrayFromHexString("90 00"));
        }

        if (matchesPattern(cmd, "FF 00 48 00 00")) {
            // Get firmware version
            return new ResponseAPDU("FAKE1.0".getBytes(StandardCharsets.US_ASCII));
        }

        if (matchesPattern(cmd, "FF 00 50 00 00")) {
            // Get PICC operating parameter
            return new ResponseAPDU(new byte[] { (byte)0x90, (byte)piccOperatingParameter });
        }

        if (matchesPattern(cmd, "FF 00 51 .. 00")) {
            // Save PICC operating parameter
            byte newPicc = cmd.getBytes()[3];
            this.piccOperatingParameter = newPicc & 0xFF;
            return new ResponseAPDU(new byte[] { (byte)0x90, (byte)piccOperatingParameter });
        }

        if (matchesPattern(cmd, "FF 00 40 .. 04 .. .. .. ..")) {
            // Let & Buzzer settings - return failure, fake card does not supports it
            return new ResponseAPDU(new byte[] { (byte)0x63, (byte)0x00 });
        }

        logger.error("Unknown sequence of bytes: {}", toHexString(cmd.getBytes()));
        throw new CardException("Not implemented in fake card!");
    }

    private boolean matchesPattern(CommandAPDU cmd, String regex) {
        String bytes = toHexString(cmd.getBytes());
        return bytes.matches(regex);
    }
}
