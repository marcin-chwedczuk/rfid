package pl.marcinchwedczuk.rfid.gui;

import javafx.beans.property.*;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.lib.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static pl.marcinchwedczuk.rfid.lib.KeyRegister.REGISTER_0;
import static pl.marcinchwedczuk.rfid.lib.KeyType.KEY_A;

public class CardWindow {
    private static Logger logger = LogManager.getLogger(CardWindow.class);

    @FXML
    public TextField cardId;
    @FXML
    public TextField cardStandard;

    @FXML
    public TextField key;
    @FXML
    public RadioButton hexKey;
    @FXML
    public RadioButton useAsKeyA;

    private final SimpleBooleanProperty displayDataAsHex = new SimpleBooleanProperty(true);
    @FXML
    public RadioButton displayHex;

    @FXML
    public Spinner<Integer> fromSector;
    @FXML
    public Spinner<Integer> toSector;

    @FXML
    public TableView<DataRow> dataTable;

    private final TableColumn<DataRow, String> sectorColumn = new TableColumn<>("SECTOR");
    private final TableColumn<DataRow, String> blockColumn = new TableColumn<>("BLOCK");
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
    }

    private int getCardMaxSector() {
        return card.atrInfo().cardName == CardName.NXP_MIFARE_STANDARD_4K
                ? 64
                : 16;
    }

    private void initializeTableView() {
        sectorColumn.setCellValueFactory(new PropertyValueFactory<DataRow, String>("sector"));
        sectorColumn.setEditable(false);
        sectorColumn.setSortable(false);

        blockColumn.setCellValueFactory(new PropertyValueFactory<DataRow, String>("block"));
        blockColumn.setEditable(false);
        blockColumn.setSortable(false);

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
                card.authenticate(SectorBlock.firstBlockOfSector(sector), KEY_A, REGISTER_0);

                for (int block = 0; block < 4; block++) {
                    byte[] data = card.readBinaryBlock(SectorBlock.fromSectorAndBlock(sector, block), 16);
                    DataRow dataRow = new DataRow(sector, block, data, block == 3);
                    logger.info("Read row: {}", dataRow.toDebugString());
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
        StringBuilder sb = new StringBuilder();

        for (var row : rows) {
            sb.append(row.toDebugString()).append(System.lineSeparator());
        }

        DialogBoxes.info(sb.toString());
    }

    public void loadDefaultFactoryKey(ActionEvent actionEvent) {
        hexKey.setSelected(true);
        key.setText("FF:FF:FF:FF:FF:FF");
    }
}
