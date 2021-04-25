package pl.marcinchwedczuk.rfid.acr122;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.*;
import java.nio.charset.StandardCharsets;

import static pl.marcinchwedczuk.rfid.acr122.Block.BLOCK_0;

public class AcrCard {
    private static Logger logger = LogManager.getLogger(AcrCard.class);
    private static final byte FF = (byte)0xFF;

    private final AcrTerminal terminal;
    private final Card card;

    public AcrCard(AcrTerminal terminal, Card card) {
        this.terminal = terminal;
        this.card = card;
    }

    public AcrTerminal terminal() { return terminal; }

    public AtrInfo atrInfo() {
        ATR atr = card.getATR();
        return AtrInfo.parse(atr.getBytes());
    }

    public String getReaderFirmwareVersion() {
        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x48, 0x00, 0x00
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            return new String(response.getBytes(), StandardCharsets.US_ASCII);
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public PiccOperatingParameter readPiccOperatingParameter() {
        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x50, 0x00, 0x00
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            byte[] responseBytes = response.getBytes();

            // Format: [0x90, PICC byte]
            if (responseBytes.length != 2 || responseBytes[0] != (byte)0x90) {
                logger.error("Unexpected bytes returned from terminal {}.", responseBytes);
                throw AcrStandardErrors.unexpectedResponseBytes(responseBytes);
            }

            return PiccOperatingParameter.fromBitPattern(responseBytes[1]);
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void savePiccOperatingParameter(PiccOperatingParameter parameter) {
        byte parameterByte = parameter.toByte();

        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x51, parameterByte, 0x00
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            byte[] responseBytes = response.getBytes();
            if (responseBytes.length != 2 || responseBytes[0] != (byte)0x90) {
                logger.error("Unexpected bytes returned from terminal {}.", responseBytes);
                throw AcrStandardErrors.unexpectedResponseBytes(responseBytes);
            }

        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public static final int DISABLE_TIMEOUT = 0x00;
    public static final int WAIT_INDEFINITELY = 0xFF;
    /**
     * Sets how long terminal will wait for a card to respond
     * to the request in seconds.
     *
     * @param seconds
     *      Timeout must be multiple of 5 seconds, e.g. 5, 10, 15 but not 12.
     *      Use {@link #DISABLE_TIMEOUT} to disable timeout check.
     *      Use {@link #WAIT_INDEFINITELY} to wait for a response without timing out.
     */
    public void setTimeout(int seconds) {
        if (seconds < 0 || seconds > 0xFF) {
            throw new IllegalArgumentException("seconds");
        }

        // Timeout is measured in 5 seconds unit. We round up
        // to the closed unit.
        byte roundedUpUnits = (byte)((seconds + 4) / 5);

        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x41, roundedUpUnits, 0x00
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void configureBuzzerOnCartDetection(boolean buzzOnCartDetection) {
        byte param = buzzOnCartDetection ? FF : 0x00;
        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x52, param, 0x00
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            // !!! This is different value than provided by the documentation.
            // Looks like the format is [0x90, currentValue]
            else if (sw != 0x90FF && sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void configureLedAndBuzzer(LedBuzzerSettings settings) {
        byte[] bdc = settings.toControlBytes();

        byte[] commandBytes = new byte[] {
                FF, 0x00, 0x40,
                settings.getLedSettings().toControlByte(),
                0x04, bdc[0], bdc[1], bdc[2], bdc[3]
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (response.getSW1() != 0x90) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public String getCardUID() {
        byte[] uid = getData(0x00, 0x00, 0x00);
        return ByteUtils.asHexString(uid, ":");
    }

    // TODO: Refactor this can only return ATS or serial number of the tag
    private byte[] getData(int p1, int p2, int len) {
        byte[] commandBytes = new byte[] {
                (byte)0xFF, (byte)0xCA, (byte)(p1 & 0xFF), (byte)(p2 & 0xFF), (byte)(len & 0xFF)
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (sw == 0x6A81) {
                throw AcrStandardErrors.functionNotSupported();
            }
            else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }

            return response.getData();
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }


    public void loadKeyToRegister(byte[] key, KeyRegister register) {
        if (key.length != 6) {
            throw new IllegalArgumentException("Invalid key length (key should be 6 bytes long).");
        }

        byte[] commandBytes = new byte[] {
                (byte)0xFF, (byte)0x82, 0x00, (byte)register.index(), 0x06,
                key[0], key[1], key[2], key[3], key[4], key[5]
        };
        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void authenticateSector(Sector sector, SelectedKey selectedKey, KeyRegister registerWithKey) {
        if (selectedKey == null) {
            throw new NullPointerException("keyType");
        }

        byte[] commandBytes = new byte[] {
                (byte)0xFF, (byte)0x86, 0x00, 0x00, 0x05,
                0x01, 0x00, (byte) DataAddress.of(sector, BLOCK_0).blockNumber(),
                (byte)(selectedKey == SelectedKey.KEY_A ? 0x60 : 0x61),
                (byte)registerWithKey.index()
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public byte[] readBinaryBlock(DataAddress block, int numberOfBytes) {
        byte[] commandBytes = new byte[] {
                (byte)0xFF, (byte)0xB0, 0x00, (byte)block.blockNumber(), (byte)numberOfBytes
        };

        try {
            ResponseAPDU response =
                    card.getBasicChannel().transmit(new CommandAPDU(commandBytes));

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            }
            else if (sw != 0x9000) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
            return response.getData();

        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void writeBinaryBlock(DataAddress block, byte[] data16) {
        if (data16.length != 16) {
            throw new IllegalArgumentException("Block is 16 bytes long for Mifare 1K/4K.");
        }

        byte[] commandBytes = new byte[] {
                (byte)0xFF, (byte)0xD6, 0x00, (byte)block.blockNumber(),
                (byte)data16.length, // must be 16 for Mifare 1K/4K
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
            }
            else if (sw != 0x9000) {
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
