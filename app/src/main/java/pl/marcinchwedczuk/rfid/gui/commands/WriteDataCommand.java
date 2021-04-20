package pl.marcinchwedczuk.rfid.gui.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.gui.DataRow;
import pl.marcinchwedczuk.rfid.lib.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static pl.marcinchwedczuk.rfid.lib.KeyRegister.REGISTER_0;

public class WriteDataCommand extends BaseUiCommand<DataRow> {
    private static Logger logger = LogManager.getLogger(WriteDataCommand.class);

    private final AcrCard card;
    private final byte[] key;
    private final KeyType keyType;
    private final int fromSector;
    private final int toSector;
    private final DataRow[] data;

    public WriteDataCommand(UiServices uiServices,
                            AcrCard card,
                            byte[] key, KeyType keyType,
                            int fromSector, int toSector,
                            DataRow[] data) {
        super(uiServices);

        this.card = card;
        this.key = key.clone();
        this.keyType = keyType;
        this.fromSector = fromSector;
        this.toSector = toSector;
        this.data = data.clone();
    }

    @Override
    protected String operationName() {
        return String.format("Writing data to card sectors %d - %d...", fromSector, toSector);
    }

    @Override
    protected List<DataRow> defineWorkItems() {
        return Arrays.stream(data)
                .filter(dr -> !dr.isSectorTrailer())
                .collect(toList());
    }

    @Override
    protected void before() {
        if (data.length == 0) {
            failWith("There is no data to write!");
        }

        // TODO: Add better validation
        if (data[0].sector != fromSector ||
                data[data.length-1].sector != toSector-1) {
            failWith(
                "Number of sectors in the data grid is different then " +
                "number of sectors to be written to the card. " +
                "Please read the requested number of sectors first, before writing them to the card.");
        }

        try {
            card.loadKeyToRegister(key, REGISTER_0);
        } catch (AcrException e) {
            throw new UiCommandFailedException("Cannot load key to card register: " + e.getMessage());
        }
    }

    @Override
    protected void doWork(DataRow dataRow) {
        Sector sector = Sector.of(dataRow.sector);
        Block block = Block.of(dataRow.block);

        authenticateSector(sector);
        writeDataToBlock(dataRow, sector, block);

        authenticateSector(sector);
        verifyWriteSuccessful(dataRow, sector, block);
    }

    private void authenticateSector(Sector sector) {
        try {
            card.authenticateSector(sector, keyType, REGISTER_0);
        } catch (Exception e) {
            failWith("Cannot authenticate to sector %s: %s", sector, e.getMessage());
        }
    }

    private void writeDataToBlock(DataRow dataRow, Sector sector, Block block) {
        try {
            logger.info("Writing data {} to sector {}, block {}",
                    dataRow.bytes, sector, block);
            card.writeBinaryBlock(DataAddress.of(sector, block), dataRow.bytes);
        } catch (Exception e) {
            failWith("Writing block %s of sector %s failed: %s", block, sector, e.getMessage());
        }
    }

    private void verifyWriteSuccessful(DataRow dataRow, Sector sector, Block block) {
        try {
            byte[] dataOnCard = card.readBinaryBlock(DataAddress.of(sector, block), 16);
            if (!Arrays.equals(dataOnCard, dataRow.bytes)) {
                failWith("Write data verification failed for sector %s and block %s.", sector, block);
            }
        } catch (Exception e) {
            failWith("Reading data back from card failed for sector %s and block %s: %s",
                    sector, block, e.getMessage());
        }
    }

    @Override
    protected void after() {
        if (error() != null) {
            uiServices().showErrorDialog(error().getMessage());
        }
    }
}
