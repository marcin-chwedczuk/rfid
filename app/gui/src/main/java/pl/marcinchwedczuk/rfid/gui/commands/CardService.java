package pl.marcinchwedczuk.rfid.gui.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.card.acr122.*;

import static pl.marcinchwedczuk.rfid.card.acr122.KeyRegister.REGISTER_0;

public class CardService {
    private static Logger logger = LogManager.getLogger(CardService.class);

    private final AcrCard card;

    public CardService(AcrCard card) {
        this.card = card;
    }

    public void loadKey(byte[] key) {
        try {
            card.loadKeyToRegister(key, REGISTER_0);
        } catch (AcrException e) {
            failWith(e, "Cannot load key to card register.");
        }
    }

    public void authenticateSector(Sector sector, SelectedKey selectedKey) {
        try {
            card.authenticateSector(sector, selectedKey, REGISTER_0);
        } catch (Exception e) {
            failWith(e, "Cannot authenticate to sector %s using provided key.", sector);
        }
    }

    public byte[] readBlockData(DataAddress address) {
        try {
            return card.readBinaryBlock(address, 16);
        } catch (Exception e) {
            failWith(e, "Reading block %s of sector %s failed.",
                    address.block, address.sector);
            return null; // not reachable
        }
    }

    public void writeData(DataAddress address, byte[] data) {
        try {
            logger.info("Writing data {} to sector {}, block {}.",
                    data, address.sector, address.block);
            card.writeBinaryBlock(address, data);
        } catch (Exception e) {
            failWith(e, "Writing block %s of sector %s failed.",
                    address.block, address.sector);
        }
    }

    static void failWith(String format, Object... args) {
        failWith(null, format, args);
    }

    static void failWith(Throwable e, String format, Object... args) {
        logger.error("Unexpected failure", e);
        throw new OperationFailedException(String.format(format, args), e);
    }
}
