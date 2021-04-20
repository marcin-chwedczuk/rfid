package pl.marcinchwedczuk.rfid.gui.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.gui.DataRow;
import pl.marcinchwedczuk.rfid.lib.*;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pl.marcinchwedczuk.rfid.lib.KeyRegister.REGISTER_0;

public class WriteDataCommand extends BaseUiCommand<DataRow> {
    private static Logger logger = LogManager.getLogger(WriteDataCommand.class);

    private final CardService cardService;
    private final byte[] key;
    private final SelectedKey selectedKey;
    private final int fromSector;
    private final int toSector;
    private final DataRow[] data;

    public WriteDataCommand(UiServices uiServices,
                            AcrCard card,
                            byte[] key, SelectedKey selectedKey,
                            int fromSector, int toSector,
                            DataRow[] data) {
        super(uiServices);

        this.cardService = new CardService(card);
        this.key = key.clone();
        this.selectedKey = selectedKey;
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

        cardService.loadKey(key);
    }

    @Override
    protected void doWork(DataRow dataRow) {
        Sector sector = Sector.of(dataRow.sector);
        Block block = Block.of(dataRow.block);
        DataAddress dataAddress = DataAddress.of(sector, block);

        cardService.authenticateSector(sector, selectedKey);
        cardService.writeData(dataAddress, dataRow.bytes);

        cardService.authenticateSector(sector, selectedKey);
        byte[] dataOnCard = cardService.readBlockData(dataAddress);
        verifyWriteSuccessful(dataAddress, dataRow, dataOnCard);
    }

    private void verifyWriteSuccessful(
            DataAddress dataAddress, DataRow dataRow, byte[] dataOnCard)
    {
        if (!Arrays.equals(dataOnCard, dataRow.bytes)) {
            failWith("Write data verification failed for sector %s and block %s.",
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
