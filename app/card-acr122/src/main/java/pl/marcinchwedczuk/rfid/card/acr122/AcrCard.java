package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.impl.AcrStandardErrors;
import pl.marcinchwedczuk.rfid.card.commons.KeyType;
import pl.marcinchwedczuk.rfid.card.commons.Register;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import javax.smartcardio.*;

import java.util.Objects;

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

    @Override
    CardTerminal getUnderlyingTerminal() {
        return terminal.getUnderlyingTerminal();
    }

    @Override
    public byte[] sendCommand(byte[] commandBytes) throws CardException {
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
        return ByteArrays.toMacString(uid);
    }

    // TODO: Refactor this can only return ATS or serial number of the tag
    private byte[] getData(int p1, int p2, int len) {
        byte[] commandBytes = ByteArrays.of(0xFF, 0xCA, p1, p2, len);

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
            throw AcrException.ofCardException(e,
                    "Error while retrieving data from the card.");
        }
    }

    public void loadKeyIntoRegister(byte[] key, Register register) {
        if (key.length != 6) {
            throw new IllegalArgumentException("Invalid key length (key should be 6 bytes long).");
        }

        logger.debug("Load key {} into register {}.", ByteArrays.toHexString(key), register);

        byte[] commandBytes = ByteArrays.concat(
                ByteArrays.of(0xFF, 0x82, 0x00, register.index(), 0x06),
                key);
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
            throw AcrException.ofCardException(e,
                    "Error while loading key into a register.");
        }
    }

    public void authenticateToSector(Sector sector, KeyType selectedKey, Register registerWithKey) {
        Objects.requireNonNull(selectedKey);

        logger.debug("Authenticate sector {} with key {} (register {}).", sector, selectedKey, registerWithKey);

        byte[] commandBytes = ByteArrays.of(
                0xFF, 0x86, 0x00, 0x00, 0x05, 0x01, 0x00,
                DataAddress.of(sector, BLOCK_0).blockNumber(),
                (selectedKey == KeyType.KEY_A ? 0x60 : 0x61),
                registerWithKey.index());

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

    public byte[] readData(DataAddress block, int numberOfBytes) {
        logger.debug("Read binary block {} (nbytes = {}).", block, numberOfBytes);

        byte[] commandBytes = ByteArrays.of(
                0xFF, 0xB0, 0x00, block.blockNumber(), numberOfBytes);

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

    public void writeData(DataAddress block, byte[] data16) {
        logger.debug("Write binary block {} (data = {}).", block, ByteArrays.toHexString(data16));

        if (data16.length != 16) {
            throw new IllegalArgumentException("Block is 16 bytes long for Mifare 1K.");
        }

        byte[] commandBytes = ByteArrays.concat(
                ByteArrays.of(0xFF, 0xD6, 0x00, block.blockNumber(), data16.length),
                data16);

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
