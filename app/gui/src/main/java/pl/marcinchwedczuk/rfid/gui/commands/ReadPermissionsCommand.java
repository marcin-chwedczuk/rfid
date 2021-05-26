package pl.marcinchwedczuk.rfid.gui.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.*;

import java.util.List;
import java.util.function.Consumer;

import static pl.marcinchwedczuk.rfid.card.acr122.Block.TRAILER;

public class ReadPermissionsCommand extends BaseUiCommand<Sector> {
    private static final Logger logger = LoggerFactory.getLogger(ReadPermissionsCommand.class);

    private final CardService cardService;
    private final byte[] key;
    private final SelectedKey selectedKey;
    private final Sector sector;
    private final Consumer<TrailerBlock> callback;

    private TrailerBlock result = null;

    public ReadPermissionsCommand(UiServices uiServices,
                                  AcrCard card,
                                  byte[] key, SelectedKey selectedKey,
                                  Sector sector,
                                  Consumer<TrailerBlock> callback) {
        super(uiServices);

        this.cardService = new CardService(card);
        this.key = key.clone();
        this.selectedKey = selectedKey;
        this.sector = sector;
        this.callback = callback;
    }

    @Override
    protected String operationName() {
        return String.format("Reading permissions for sector %s...", sector);
    }

    @Override
    protected List<Sector> defineWorkItems() {
        return List.of(sector);
    }

    @Override
    protected void before() {
        cardService.loadKey(key);
    }

    @Override
    protected void doWork(Sector sector) {
        cardService.authenticateSector(sector, selectedKey);

        byte[] blockBytes = cardService.readBlockData(DataAddress.of(sector, TRAILER));
        this.result = new TrailerBlock(blockBytes);
    }

    @Override
    protected void after() {
        if (error() != null) {
            displayError(error());
        }
        else {
            callback.accept(result);
        }
    }
}
