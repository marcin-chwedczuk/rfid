package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.impl.AcrStandardErrors;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;
import java.nio.charset.StandardCharsets;

public abstract class AcrTerminalCommands {
    private static final Logger logger = LoggerFactory.getLogger(AcrTerminal.class);

    public abstract byte[] sendCommand(byte[] commandBytes) throws CardException;

    // VisibleForTests
    abstract CardTerminal getUnderlyingTerminal();

    public PiccOperatingParameter getPiccOperatingParameter() {
        logger.info("getPiccOperatingParameter()");

        byte[] commandBytes = ByteArrays.of(0xFF, 0x00, 0x50, 0x00, 0x00);

        try {
            byte[] responseBytes = this.sendCommand(commandBytes);

            // Format: [0x90, PICC byte]
            if (responseBytes.length != 2 || responseBytes[0] != (byte) 0x90) {
                logger.error("Unexpected bytes returned from terminal {}.", responseBytes);
                throw AcrStandardErrors.unexpectedResponseBytes(responseBytes);
            }

            return PiccOperatingParameter.parseByte(responseBytes[1]);
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void setPiccOperatingParameter(PiccOperatingParameter parameter) {
        logger.info("setPiccOperatingParameter({})", parameter);

        int parameterByte = parameter.toUnsignedByte();
        byte[] commandBytes = ByteArrays.of(0xFF, 0x00, 0x51, parameterByte, 0x00);

        try {
            byte[] responseBytes = sendCommand(commandBytes);

            if (responseBytes.length != 2 || responseBytes[0] != (byte) 0x90) {
                logger.error("Unexpected bytes returned from terminal {}.", responseBytes);
                throw AcrStandardErrors.unexpectedResponseBytes(responseBytes);
            }
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void configureLedAndBuzzer(LedBuzzerSettings settings) {
        logger.info("configureLedAndBuzzer({})", settings);

        int[] bdc = settings.toUnsignedControlBytes();

        byte[] commandBytes = ByteArrays.of(
                0xFF, 0x00, 0x40,
                settings.getLedSettings().toUnsignedControlByte(),
                0x04, bdc[0], bdc[1], bdc[2], bdc[3]
        );

        try {
            ResponseAPDU response = new ResponseAPDU(
                this.sendCommand(commandBytes)
            );

            int sw = response.getSW();
            if (sw == 0x6300) {
                throw AcrStandardErrors.operationFailed();
            } else if (response.getSW1() != 0x90) {
                throw AcrStandardErrors.unexpectedResponseCode(sw);
            }
            // Success
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }

    public void setBuzzOnCardDetection(boolean buzz) {
        int param = buzz ? 0xFF : 0x00;
        byte[] commandBytes = ByteArrays.of(0xFF, 0x00, 0x52, param, 0x00);

        try {
            ResponseAPDU response = new ResponseAPDU(
                    sendCommand(commandBytes));

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

    public static final int DISABLE_TIMEOUT = 0x00;
    public static final int WAIT_INDEFINITELY = 0xFF;

    /**
     * Sets how long terminal will wait for a card to respond
     * to the request in seconds.
     *
     * @param seconds Timeout must be multiple of 5 seconds, e.g. 5, 10, 15 but not 12.
     *                Use {@link #DISABLE_TIMEOUT} to disable timeout check.
     *                Use {@link #WAIT_INDEFINITELY} to wait for a response without timing out.
     */
    public void setTimeout(int seconds) {
        if (seconds < 0 || seconds > 0xFF) {
            throw new IllegalArgumentException("seconds");
        }

        logger.info("setTimeout({})", seconds);

        // Timeout is measured in 5 seconds unit. We round up
        // to the closed unit.
        int roundedUpUnits = (seconds + 4) / 5;
        byte[] commandBytes = ByteArrays.of(0xFF, 0x00, 0x41, roundedUpUnits, 0x00);

        try {
            ResponseAPDU response = new ResponseAPDU(sendCommand(commandBytes));

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

    public String getReaderFirmwareVersion() {
        logger.info("getReaderFirmwareVersion()");

        byte[] commandBytes = ByteArrays.of(0xFF, 0x00, 0x48, 0x00, 0x00);
        try {
            ResponseAPDU response = new ResponseAPDU(sendCommand(commandBytes));
            return new String(response.getBytes(), StandardCharsets.US_ASCII);
        } catch (CardException e) {
            throw AcrException.ofCardException(e);
        }
    }
}
