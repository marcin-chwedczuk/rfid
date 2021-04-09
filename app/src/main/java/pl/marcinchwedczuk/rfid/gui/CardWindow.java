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
import pl.marcinchwedczuk.rfid.lib.AcrCard;
import pl.marcinchwedczuk.rfid.lib.DataBlock;

import java.util.ArrayList;
import java.util.List;

public class CardWindow {
    @FXML public TextField cardId;
    @FXML public TextField cardStandard;

    @FXML public TextField key;
    @FXML public RadioButton hexKey;
    @FXML public RadioButton useAsKeyA;

    @FXML public Spinner<Integer> fromSector;
    @FXML public Spinner<Integer> toSector;

    @FXML public TableView<DataRow> dataTable;

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
    }

    private void initializeTableView() {
        //sectorColumn.setCellValueFactory((TableColumn.CellDataFeatures<DataRow, String> row) ->
        //        new ReadOnlyObjectWrapper<String>(Integer.toString(row.getValue().sector)));

        sectorColumn.setCellValueFactory(new PropertyValueFactory<DataRow, String>("sector"));
        sectorColumn.setEditable(false);
        sectorColumn.setSortable(false);

        blockColumn.setCellValueFactory(new PropertyValueFactory<DataRow, String>("block"));
        blockColumn.setEditable(false);
        blockColumn.setSortable(false);

        dataTable.getColumns().add(sectorColumn);
        dataTable.getColumns().add(blockColumn);

        for (int i = 0; i < 16; i++) {
            int index = i;
            TableColumn<DataRow, Byte> dataColumn = new TableColumn<>("B" + i);

            dataColumn.setCellValueFactory((TableColumn.CellDataFeatures<DataRow, Byte> row) -> {
                DataRow dataRow = row.getValue();

                SimpleObjectProperty<Byte> byteProp = new SimpleObjectProperty<Byte>(dataRow.bytes[index]);
                byteProp.addListener((unused, oldValue, newValue) -> {
                    dataRow.bytes[index] = (byte)(int)newValue;
                });

                return byteProp;
            });

            // Editable
            dataColumn.setEditable(true);
            dataColumn.setSortable(false);
            dataColumn.setMaxWidth(40.0);
            dataColumn.setMinWidth(40.0);
            dataColumn.setCellFactory(l -> new DataRowByteTableCell(new SimpleBooleanProperty(true)));

            dataColumns.add(dataColumn);
        }
        dataTable.getColumns().addAll(dataColumns);

        dataTable.setItems(rows);
    }

    public void readSectors(ActionEvent actionEvent) {
        DataRow example = new DataRow(0, 1, new byte[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15 }, true);
        DataRow example2 = new DataRow(0, 1, new byte[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15 }, false);
        DataRow example3 = new DataRow(0, 1, new byte[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15 }, false);

        rows.clear();
        rows.add(example);
        rows.add(example2);
        rows.add(example3);

        dataTable.refresh();
    }

    public void selectAllSectors(ActionEvent actionEvent) {

    }

    public void writeSectors(ActionEvent actionEvent) {

    }

    public void loadDefaultFactoryKey(ActionEvent actionEvent) {

    }
}
