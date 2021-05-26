package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.gui.*;
import pl.marcinchwedczuk.rfid.acr122.*;

import java.util.List;

public class ReadDataCommand extends BaseUiCommand<Sector> {
    private static Logger logger = LogManager.getLogger(ReadDataCommand.class);

    private final CardService cardService;
    private final byte[] key;
    private final SelectedKey selectedKey;
    private final int fromSector;
    private final int toSector;
    private final ObservableList<DataRow> resultContainer;

    public ReadDataCommand(UiServices uiServices,
                           AcrCard card,
                           byte[] key, SelectedKey selectedKey,
                           int fromSector, int toSector,
                           ObservableList<DataRow> resultContainer) {
        super(uiServices);

        this.cardService = new CardService(card);
        this.key = key.clone();
        this.selectedKey = selectedKey;
        this.fromSector = fromSector;
        this.toSector = toSector;
        this.resultContainer = resultContainer;
    }

    @Override
    protected String operationName() {
        return String.format("Reading data from card sectors %d - %d...", fromSector, toSector - 1);
    }

    @Override
    protected List<Sector> defineWorkItems() {
        return Sector.range(fromSector, toSector);
    }

    @Override
    protected void before() {
        resultContainer.clear();
        cardService.loadKey(key);
    }

    @Override
    protected void doWork(Sector sector) {
        cardService.authenticateSector(sector, selectedKey);

        for (Block block: Block.allBlocksInSector()) {
            byte[] blockBytes = cardService.readBlockData(DataAddress.of(sector, block));
            DataRow dataRow = new DataRow(sector, block, blockBytes);
            resultContainer.add(dataRow);
        }
    }

    @Override
    protected void after() {
        if (error() != null) {
            displayError(error());
        }
    }
}
