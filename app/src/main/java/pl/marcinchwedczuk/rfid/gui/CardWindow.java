package pl.marcinchwedczuk.rfid.gui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.lib.*;
import pl.marcinchwedczuk.rfid.xml.XmlCardData;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static pl.marcinchwedczuk.rfid.lib.KeyRegister.REGISTER_0;
import static pl.marcinchwedczuk.rfid.lib.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.lib.KeyType.KEY_B;

public class CardWindow {
    private static Logger logger = LogManager.getLogger(CardWindow.class);

    private final FileChooser fileChooser = new FileChooser();

    @FXML private TextField cardId;
    @FXML private TextField cardStandard;

    @FXML private TextField key;
    @FXML private RadioButton hexKey;
    @FXML private RadioButton useAsKeyA;

    private final SimpleBooleanProperty displayDataAsHex = new SimpleBooleanProperty(true);
    @FXML private RadioButton displayHex;

    @FXML private Spinner<Integer> fromSector;
    @FXML private Spinner<Integer> toSector;

    @FXML private TableView<DataRow> dataTable;

    private final TableColumn<DataRow, Integer> sectorColumn = new TableColumn<>("SECTOR");
    private final TableColumn<DataRow, Integer> blockColumn = new TableColumn<>("BLOCK");
    private final List<TableColumn<DataRow, Byte>> dataColumns = new ArrayList<>();

    private final ObservableList<DataRow> rows = FXCollections.observableArrayList();

    private AcrCard card;

    public void setCard(AcrCard card) {
        this.card = card;

        cardId.setText(card.atrInfo().cardName.readableName());
        cardStandard.setText(card.atrInfo().cardStandard.readableName());

        initializeTableView();

        displayHex.selectedProperty().addListener((prop, oldValue, newValue) -> {
            displayDataAsHex.set(newValue);
            dataTable.refresh();
        });

        int maxSector = getCardMaxSector();
        fromSector.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxSector, 0));
        toSector.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxSector, 1));

        // Configure file dialog
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
    }

    private int getCardMaxSector() {
        return card.atrInfo().cardName == CardName.NXP_MIFARE_STANDARD_4K
                ? 64
                : 16;
    }

    private void initializeTableView() {
        sectorColumn.setCellValueFactory(new PropertyValueFactory<DataRow, Integer>("sector"));
        sectorColumn.setEditable(false);
        sectorColumn.setSortable(false);
        sectorColumn.setCellFactory(l -> new DataRowSectorBlockTableCell());

        blockColumn.setCellValueFactory(new PropertyValueFactory<DataRow, Integer>("block"));
        blockColumn.setEditable(false);
        blockColumn.setSortable(false);
        blockColumn.setCellFactory(l -> new DataRowSectorBlockTableCell());

        dataTable.getColumns().add(sectorColumn);
        dataTable.getColumns().add(blockColumn);

        for (int i = 0; i < 16; i++) {
            final int index = i;
            TableColumn<DataRow, Byte> dataColumn = new TableColumn<>("B" + i);

            dataColumn.setCellValueFactory((TableColumn.CellDataFeatures<DataRow, Byte> row) -> {
                DataRow dataRow = row.getValue();

                SimpleObjectProperty<Byte> byteProp = new SimpleObjectProperty<Byte>(dataRow.bytes[index]);
                byteProp.addListener((unused, oldValue, newValue) -> {
                    dataRow.bytes[index] = (byte) (int) newValue;
                });

                return byteProp;
            });

            // Editable
            dataColumn.setEditable(true);
            dataColumn.setSortable(false);
            dataColumn.setMaxWidth(60.0);
            dataColumn.setMinWidth(60.0);
            dataColumn.setCellFactory(l -> new DataRowByteTableCell(displayDataAsHex));

            dataColumns.add(dataColumn);
        }
        dataTable.getColumns().addAll(dataColumns);

        dataTable.setItems(rows);
    }

    public void readSectors(ActionEvent actionEvent) {
        byte[] keyBytes = getKeyBytes();
        if (keyBytes == null) {
            return;
        }

        try {
            card.loadKeyToRegister(keyBytes, REGISTER_0);
        } catch (AcrException e) {
            DialogBoxes.error("Cannot read data from card!", e.getMessage());
            return;
        }

        int from = fromSector.getValue();
        int to = toSector.getValue();

        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicReference<ProgressDialog> progressDialog = new AtomicReference<>();

        Runnable before = () -> {
            rows.clear();

            progressDialog.set(ProgressDialog.show(
                    hexKey.getScene(),
                    "Reading sectors from card...",
                    () -> cancel.set(true)));
        };

        Runnable after = () -> {
            progressDialog.get().done();
            dataTable.refresh();
        };

        Function<Integer, Boolean> work = (sector) -> {
            try {
                card.authenticate(SectorBlock.firstBlockOfSector(sector),
                        useAsKeyA.isSelected() ? KEY_A : KEY_B, REGISTER_0);

                for (int block = 0; block < 4; block++) {
                    byte[] data = card.readBinaryBlock(SectorBlock.fromSectorAndBlock(sector, block), 16);
                    DataRow dataRow = new DataRow(sector, block, data, block == 3);
                    logger.info("Read row: {}", dataRow.toString());
                    rows.add(dataRow);
                }
            } catch (Exception e) {
                DialogBoxes.error("Problem while reading sectors...", e.getMessage());
                return false;
            }

            progressDialog.get().setProgress((sector * 100) / to);
            return !cancel.get();
        };

        new PoorManBackgroundTask(from, to, work, before, after).start();
    }

    private byte[] getKeyBytes() {
        String keyString = key.getText();
        boolean isHex = hexKey.isSelected();

        try {
            byte[] keyBytes = isHex
                    ? ByteUtils.fromHexString(keyString, ":")
                    : keyString.getBytes(StandardCharsets.US_ASCII);

            if (keyBytes.length != 6) {
                DialogBoxes.error("Invalid key!", "Key must consists of 6 bytes!");
                return null;
            }

            return keyBytes;
        } catch (NumberFormatException e) {
            DialogBoxes.error("Invalid key!", e.getMessage());
            return null;
        }
    }

    public void selectAllSectors(ActionEvent actionEvent) {
        fromSector.getValueFactory().setValue(0);

        int maxSector = getCardMaxSector();
        toSector.getValueFactory().setValue(maxSector);
    }

    public void writeSectors(ActionEvent actionEvent) {
        byte[] keyBytes = getKeyBytes();
        if (keyBytes == null) {
            return;
        }

        try {
            card.loadKeyToRegister(keyBytes, REGISTER_0);
        } catch (AcrException e) {
            DialogBoxes.error("Cannot read data from card!", e.getMessage());
            return;
        }

        DataRow[] rows = this.rows.toArray(new DataRow[0]);
        int from = fromSector.getValue();
        int to = toSector.getValue();

        // Validate has data
        if (rows.length == 0) {
            DialogBoxes.error("Error", "No data to write!");
            return;
        }
        if (rows[0].sector != from || rows[rows.length-1].sector != to-1) {
            DialogBoxes.error("Error",
                    "Number of sectors in the data grid is different then " +
                    "number of sectors to be written to the card. " +
                    "Please read the requested number of sectors first, before writing them to the card.");
            return;
        }

        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicReference<ProgressDialog> progressDialog = new AtomicReference<>();

        Runnable before = () -> {
            progressDialog.set(ProgressDialog.show(
                    hexKey.getScene(),
                    "Writing sectors to card...",
                    () -> cancel.set(true)));
        };

        Runnable after = () -> {
            progressDialog.get().done();
        };

        Function<Integer, Boolean> work = (sector) -> {
            try {
                card.authenticate(SectorBlock.firstBlockOfSector(sector),
                        useAsKeyA.isSelected() ? KEY_A : KEY_B, REGISTER_0);

                // 4th block (which contains keys and permissions) is not writable by this method
                for (int block = 0; block < 3; block++) {
                    DataRow row = rows[(sector - from)*4 + block];
                    card.writeBinaryBlock(SectorBlock.fromSectorAndBlock(sector, block), row.bytes);

                    // Verify write
                    /*
                    byte[] data = card.readBinaryBlock(SectorBlock.fromSectorAndBlock(sector, block), 16);
                    if (!Arrays.equals(data, row.bytes)) {
                        throw new RuntimeException(String.format(
                                "Write failed for sector %d and block %d.", sector, block));
                    }

                     */
                }
            } catch (Exception e) {
                DialogBoxes.error("Problem while writing sectors to card...", e.getMessage());
                return false;
            }

            progressDialog.get().setProgress((sector * 100) / to);
            return !cancel.get();
        };

        new PoorManBackgroundTask(from, to, work, before, after).start();
    }

    public void loadDefaultFactoryKey(ActionEvent actionEvent) {
        hexKey.setSelected(true);
        key.setText("FF:FF:FF:FF:FF:FF");
    }

    public void exportToXml(ActionEvent actionEvent) {
        fileChooser.setTitle("Export cart data to XML file...");
        File target = fileChooser.showSaveDialog(hexKey.getScene().getWindow());
        if (target != null) {
            try {
                String xml = new XmlCardData(rows.stream()).toXml();
                // Writes using UTF-8
                Files.writeString(target.toPath(), xml,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                DialogBoxes.error("Exporting data failed.", e.getMessage());
            }
        }
    }

    public void importFromXml(ActionEvent actionEvent) {
        fileChooser.setTitle("Import cart data from XML file...");
        fileChooser.showOpenDialog(hexKey.getScene().getWindow());

        DialogBoxes.error("TODO", "Not implemented yet!");
    }
}
