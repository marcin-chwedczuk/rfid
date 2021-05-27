package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.ATR;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static pl.marcinchwedczuk.rfid.card.fake.StringUtils.byteArrayFromHexString;

public class FakeMifare1K {
    private final byte[] fakeKeyA = byteArrayFromHexString("11 22 33 44 55 66");
    private final byte[] fakeKeyB = byteArrayFromHexString("FF FF FF FF FF FF");

    private byte[] keyRegister0 = null;
    private byte[] keyRegister1 = null;

    private int authenticatedSector = -1;

    public ATR getATR() {
        return new ATR(byteArrayFromHexString(
                "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A"
        ));
    }

    public ResponseAPDU accept(CommandAPDU cmd) {
        if (matchesPattern(cmd, "FF CA 00 00 04") ) {
            // Get Card ID
            return new ResponseAPDU(byteArrayFromHexString(
                    // Card ID LSB - MSB, SW1, SW2
                    "AA BB CC DD EE FF 90 00"
            ));
        }
        else if (matchesPattern(cmd, "FF CA 01 00 04")) {
            // Get ATS (Answer to Select)
            throw new RuntimeException("Not implemented.");
        }
        else if(matchesPattern(cmd, "FF 82 00 (00|01) 06 .*")) {
            // Load key into register
            byte[] key = cmd.getData().clone();

            // See ACR122 API specification to understand P1, P2 and other stuff
            if (cmd.getP2() == 0) {
                keyRegister0 = key;
            } else {
                keyRegister1 = key;
            }
        } else if (matchesPattern(cmd, "FF 86 00 00 05 01 00 .*")) {
            byte[] data = cmd.getData();
            int blockNumber = Byte.toUnsignedInt(data[2]);
            boolean keyA = (data[3] == 0x60);
            int register = data[4];

            byte[] loadedKey = (register == 0) ? keyRegister0 : keyRegister1;
            byte[] expectedKey = keyA ? fakeKeyA : fakeKeyB;
        }

        return new ResponseAPDU(new byte[] { });
    }

    private boolean matchesPattern(CommandAPDU cmd, String regex) {
        String bytes = StringUtils.byteArrayToHexString(cmd.getBytes());
        return bytes.matches(regex);
    }
}
