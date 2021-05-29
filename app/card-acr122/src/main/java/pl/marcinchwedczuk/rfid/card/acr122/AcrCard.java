package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;

import javax.smartcardio.*;

import static pl.marcinchwedczuk.rfid.card.acr122.Block.BLOCK_0;

public class AcrCard extends AcrTerminalCommands {
    private static final Logger logger = LoggerFactory.getLogger(AcrCard.class);
    private static final byte FF = (byte) 0xFF;

    private final AcrTerminal terminal;
    private final Card card;

    public AcrCard(AcrTerminal terminal, Card card) {
        this.terminal = terminal;
        this.card = card;
    }

    public byte[] sendRawCommand(byte[] commandBytes) throws CardException {
        return card.getBasicChannel()
                .transmit(new CommandAPDU(commandBytes))
                .getBytes();
    }

    public AcrTerminal terminal() {
        return terminal;
    }

    public AtrInfo atrInfo() {
        ATR atr = card.getATR();
        return AtrInfo.parse(atr.getBytes());
    }

    public String getCardUID() {
        logger.debug("Getting card UID (via ATR)");
        byte[] uid = getData(0x00, 0x00, 0x00);
        return ByteArrays.toHexString(uid, ":");
    }

    // TODO: Refactor this can only return ATS or serial number of the tag
    private byte[] getData(int p1, int p2, int len) {
        byte[] commandBytes = new byte[]{
                (byte) 0xFF, (byte) 0xCA, (byte) (p1 & 0xFF), (byte) (p2 & 0xFF), (byte) (len & 0xFF)
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (sw == 0x6A81) {
                throw AcrStandardErrors.functionNotSupported();
            } else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }

            return response.getData();
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }


    public void loadKeyToRegister(byte[] key, KeyRegister register) {
        logger.debug("Load key {} to register {}.", ByteArrays.toHexString(key), register);

        if (key.length != 6) {
            throw new IllegalArgumentException("Invalid key length (key should be 6 bytes long).");
        }

        byte[] commandBytes = new byte[]{
                (byte) 0xFF, (byte) 0x82, 0x00, (byte) register.index(), 0x06,
                key[0], key[1], key[2], key[3], key[4], key[5]
        };
        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void authenticateSector(Sector sector, SelectedKey selectedKey, KeyRegister registerWithKey) {
        logger.debug("Authenticate sector {} with key {} (register {}).", sector, selectedKey, registerWithKey);

        if (selectedKey == null) {
            throw new NullPointerException("keyType");
        }

        byte[] commandBytes = new byte[]{
                (byte) 0xFF, (byte) 0x86, 0x00, 0x00, 0x05,
                0x01, 0x00, (byte) DataAddress.of(sector, BLOCK_0).blockNumber(),
                (byte) (selectedKey == SelectedKey.KEY_A ? 0x60 : 0x61),
                (byte) registerWithKey.index()
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public byte[] readBinaryBlock(DataAddress block, int numberOfBytes) {
        logger.debug("reading binary block {} (bytes {})", block, numberOfBytes);

        byte[] commandBytes = new byte[]{
                (byte) 0xFF, (byte) 0xB0, 0x00, (byte) block.blockNumber(), (byte) numberOfBytes
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
            return response.getData();

        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void writeBinaryBlock(DataAddress block, byte[] data16) {
        logger.debug("Writing binary block {} (bytes {})", block, ByteArrays.toHexString(data16));

        if (data16.length != 16) {
            throw new IllegalArgumentException("Block is 16 bytes long for Mifare 1K/4K.");
        }

        byte[] commandBytes = new byte[]{
                (byte) 0xFF, (byte) 0xD6, 0x00, (byte) block.blockNumber(),
                (byte) data16.length, // must be 16 for Mifare 1K/4K
                data16[0], data16[1], data16[2], data16[3],
                data16[4], data16[5], data16[6], data16[7],
                data16[8], data16[9], data16[10], data16[11],
                data16[12], data16[13], data16[14], data16[15]
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void disconnect() {
        try {
            card.disconnect(true);
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }
}
