package pl.marcinchwedczuk.rfid.gui.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.card.commons.KeyType;

import java.util.Arrays;
import java.util.List;

import static pl.marcinchwedczuk.rfid.card.acr122.Block.TRAILER;

public class WritePermissionsCommand extends BaseUiCommand<Sector> {
    private static final Logger logger = LoggerFactory.getLogger(WritePermissionsCommand.class);

    private final CardService cardService;
    private final byte[] key;
    private final KeyType selectedKey;
    private final int fromSector;
    private final int toSector;
    private final TrailerBlock template;

    public WritePermissionsCommand(UiServices uiServices,
                                   AcrCard card,
                                   byte[] key, KeyType selectedKey,
                                   int fromSector, int toSector,
                                   TrailerBlock template) {
        super(uiServices);

        this.cardService = new CardService(card);
        this.key = key.clone();
        this.selectedKey = selectedKey;
        this.fromSector = fromSector;
        this.toSector = toSector;
        this.template = template;
    }

    @Override
    protected String operationName() {
        if (fromSector == toSector - 1) {
            return String.format("Writing permissions for sector %d...", fromSector);
        } else {
            return String.format("Writing permissions for sectors %d - %d...",
                    fromSector, toSector - 1);
        }
    }

    @Override
    protected List<Sector> defineWorkItems() {
        return Sector.range(fromSector, toSector);
    }

    @Override
    protected void before() {
        cardService.loadKey(key);
    }

    @Override
    protected void doWork(Sector sector) {
        byte[] trailerBytes = template.toBytes();
        DataAddress dataAddress = DataAddress.of(sector, TRAILER);

        cardService.authenticateSector(sector, selectedKey);
        cardService.writeData(dataAddress, trailerBytes);

        // TODO: Key may have changed, sector may no longer be readable
        /*
        cardService.authenticateSector(sector, selectedKey);
        byte[] bytesOnCard = cardService.readBlockData(dataAddress);

        verifyWriteSuccessful(dataAddress, trailerBytes, bytesOnCard);
        */
    }

    private void verifyWriteSuccessful(
            DataAddress dataAddress, byte[] trailerBytes, byte[] dataOnCard) {
        if (!Arrays.equals(dataOnCard, trailerBytes)) {
            failWith("Sector trailer write verification failed " +
                            "for sector %s and block %s.",
                    dataAddress.sector, dataAddress.block);
        }
    }

    @Override
    protected void after() {
        if (error() != null) {
            displayError(error());
        }
    }
}
