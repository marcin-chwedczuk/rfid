package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.gui.*;
import pl.marcinchwedczuk.rfid.lib.*;

import java.util.List;

import static pl.marcinchwedczuk.rfid.lib.Block.BLOCK_0;
import static pl.marcinchwedczuk.rfid.lib.KeyRegister.REGISTER_0;

public class ReadDataCommand extends BaseUiCommand<Sector> {
    private static Logger logger = LogManager.getLogger(ReadDataCommand.class);

    private final AcrCard card;
    private final byte[] key;
    private final KeyType keyType;
    private final int fromSector;
    private final int toSector;
    private final ObservableList<DataRow> resultContainer;

    public ReadDataCommand(Scene ownerWindow,
                           AcrCard card,
                           byte[] key, KeyType keyType,
                           int fromSector, int toSector,
                           ObservableList<DataRow> resultContainer) {
        super(ownerWindow);

        this.card = card;
        this.key = key.clone();
        this.keyType = keyType;
        this.fromSector = fromSector;
        this.toSector = toSector;
        this.resultContainer = resultContainer;
    }

    @Override
    protected String operationName() {
        return String.format("Reading data from card sectors %d - %d...", fromSector, toSector);
    }

    @Override
    protected List<Sector> defineWorkItems() {
        return Sector.range(fromSector, toSector);
    }

    @Override
    protected void before() {
        resultContainer.clear();

        try {
            card.loadKeyToRegister(key, REGISTER_0);
        } catch (AcrException e) {
            throw new UiCommandFailedException("Cannot load key to card register: " + e.getMessage());
        }
    }

    @Override
    protected void doWork(Sector sector) {
        try {
            card.authenticateSector(sector, keyType, REGISTER_0);

            for (Block block: Block.all()) {
                byte[] data = card.readBinaryBlock(SectorBlock.of(sector, block), 16);

                DataRow dataRow = new DataRow(sector.index, block.index, data, block.isTrailer());
                resultContainer.add(dataRow);
            }
        } catch (Exception e) {
            throw new UiCommandFailedException(
                    String.format("Reading sector %s failed: %s", sector, e.getMessage()));
        }
    }

    @Override
    protected void after() {
        if (error() != null) {
            DialogBoxes.error(error().getMessage());
        }
    }
}
