package pl.marcinchwedczuk.rfid.gui.card;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.card.commons.AccessBits;
import pl.marcinchwedczuk.rfid.card.commons.DataBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.KeyType;
import pl.marcinchwedczuk.rfid.card.commons.TrailerBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.gui.about.AboutWindow;
import pl.marcinchwedczuk.rfid.gui.commands.*;
import pl.marcinchwedczuk.rfid.gui.controls.keybox.KeyBox;
import pl.marcinchwedczuk.rfid.gui.controls.maskedtext.MaskedTextField;
import pl.marcinchwedczuk.rfid.gui.sender.SenderWindow;
import pl.marcinchwedczuk.rfid.gui.settings.SettingsWindow;
import pl.marcinchwedczuk.rfid.gui.utils.FxDialogBoxes;
import pl.marcinchwedczuk.rfid.gui.utils.KeyForm;
import pl.marcinchwedczuk.rfid.xml.XmlCardData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static pl.marcinchwedczuk.rfid.card.commons.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.commons.Register.REGISTER_0;

public class CardWindow implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(CardWindow.class);

    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private TextField cardId;
    @FXML
    private TextField cardStandard;

    @FXML
    private KeyBox key;
    @FXML
    private ChoiceBox<KeyType> useAsKeyChoiceBox;

    private final SimpleObjectProperty<KeyForm> displayDataEncoding = new SimpleObjectProperty<>(KeyForm.Hex);
    @FXML
    private ToggleButton displayDataAsHex;

    @FXML
    private Spinner<Integer> fromSector;
    @FXML
    private Spinner<Integer> toSector;

    @FXML
    private TableView<DataRow> dataTable;

    @FXML
    private TableView<DataAccessInfoTable.DataAccessInfoBean> dataAccessInfo;
    @FXML
    private TableView<SectorTrailerAccessInfoTable.SectorTrailerAccessInfoBean> trailerAccessInfo;

    private final TableColumn<DataRow, Integer> sectorColumn = new TableColumn<>("SECTOR");
    private final TableColumn<DataRow, Integer> blockColumn = new TableColumn<>("BLOCK");
    private final List<TableColumn<DataRow, Byte>> dataColumns = new ArrayList<>();

    private final ObservableList<DataRow> rows = FXCollections.observableArrayList();

    @FXML
    private ChoiceBox<DataBlockAccess> secBlock0Perms;
    @FXML
    private ChoiceBox<DataBlockAccess> secBlock1Perms;
    @FXML
    private ChoiceBox<DataBlockAccess> secBlock2Perms;
    @FXML
    private ChoiceBox<TrailerBlockAccess> secTrailerPerms;

    @FXML
    private MaskedTextField secSector;

    @FXML
    private KeyBox secKeyA;
    @FXML
    private KeyBox secKeyB;

    private AcrCard card;
    private UiServices uiServices;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        useAsKeyChoiceBox.getItems().setAll(KeyType.values());
        useAsKeyChoiceBox.getSelectionModel().select(KEY_A);

        key.loadKey("FF:FF:FF:FF:FF:FF", KeyForm.Hex);
        secSector.setPlainText("0");
    }

    private Scene getScene() {
        return cardId.getScene();
    }

    public void setCard(AcrCard card) {
        uiServices = new UiServices(getScene());
        this.card = card;

        cardId.setText(card.atrInfo().cardName.readableName());
        cardStandard.setText(card.atrInfo().cardStandard.readableName());

        initializeTableView();

        displayDataAsHex.selectedProperty().addListener((prop, oldValue, newValue) -> {
            KeyForm current = displayDataAsHex.isSelected() ? KeyForm.Hex : KeyForm.Ascii;
            displayDataEncoding.setValue(current);
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

        setupChoiceBox(secBlock0Perms, DataBlockAccess.values());
        setupChoiceBox(secBlock1Perms, DataBlockAccess.values());
        setupChoiceBox(secBlock2Perms, DataBlockAccess.values());
        setupChoiceBox(secTrailerPerms, TrailerBlockAccess.values());

        new DataAccessInfoTable(dataAccessInfo)
                .setup()
                .addRowMenuEntry("Select for Block 0", info -> selectAccessForBlock(List.of(0), info.getCBits()))
                .addRowMenuEntry("Select for Block 1", info -> selectAccessForBlock(List.of(1), info.getCBits()))
                .addRowMenuEntry("Select for Block 2", info -> selectAccessForBlock(List.of(2), info.getCBits()))
                .addRowMenuEntry("Select for all Blocks", info -> selectAccessForBlock(List.of(0, 1, 2), info.getCBits()));

        new SectorTrailerAccessInfoTable(trailerAccessInfo)
                .setup()
                .addRowMenuEntry("Select", info -> {
                    String cbits = info.getCBits();
                    secTrailerPerms.getSelectionModel().select(TrailerBlockAccess.valueOf(cbits));
                });
    }

    private void selectAccessForBlock(List<Integer> blocks, String cbits) {
        if (blocks.contains(0)) {
            secBlock0Perms.getSelectionModel().select(DataBlockAccess.valueOf(cbits));
        }

        if (blocks.contains(1)) {
            secBlock1Perms.getSelectionModel().select(DataBlockAccess.valueOf(cbits));
        }

        if (blocks.contains(2)) {
            secBlock2Perms.getSelectionModel().select(DataBlockAccess.valueOf(cbits));
        }
    }

    private <T> void setupChoiceBox(ChoiceBox<T> choiceBox, T[] values) {
        choiceBox.setValue(null);
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(values);
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
            dataColumn.setMinWidth(20.0);
            dataColumn.setCellFactory(l -> new DataRowByteTableCell(displayDataEncoding));

            dataColumns.add(dataColumn);
        }
        dataTable.getColumns().addAll(dataColumns);

        dataTable.setItems(rows);
    }

    public void readSectors(ActionEvent unused) {
        byte[] keyBytes = getKeyBytes();
        if (keyBytes == null) {
            return;
        }

        int from = fromSector.getValue();
        int to = toSector.getValue();

        new ReadDataCommand(uiServices,
                card,
                keyBytes, useAsKeyChoiceBox.getValue(),
                from, to,
                rows
        ).runCommandAsync();
    }

    private byte[] getKeyBytes() {
        String keyString = key.getKey();
        KeyForm keyForm = key.getEncoding();

        try {
            byte[] keyBytes = keyForm == KeyForm.Hex
                    ? ByteArrays.fromHexString(keyString, ":")
                    : keyString.getBytes(StandardCharsets.US_ASCII);

            if (keyBytes.length != 6) {
                FxDialogBoxes.error("Invalid key!", "Key must consists of 6 bytes!");
                return null;
            }

            return keyBytes;
        } catch (NumberFormatException e) {
            FxDialogBoxes.error("Invalid key!", e.getMessage());
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
            card.loadKeyIntoRegister(keyBytes, REGISTER_0);
        } catch (AcrException e) {
            FxDialogBoxes.error("Cannot read data from card!", e.getMessage());
            return;
        }

        DataRow[] rows = this.rows.toArray(new DataRow[0]);
        int from = fromSector.getValue();
        int to = toSector.getValue();

        new WriteDataCommand(uiServices,
                card,
                keyBytes, useAsKeyChoiceBox.getValue(),
                from, to,
                rows
        ).runCommandAsync();
    }

    public void loadDefaultFactoryKey(ActionEvent actionEvent) {
        key.loadKey("FF:FF:FF:FF:FF:FF", KeyForm.Hex);
        useAsKeyChoiceBox.setValue(KEY_A);
    }

    public void exportToXml(ActionEvent actionEvent) {
        fileChooser.setTitle("Export cart data to XML file...");
        File target = fileChooser.showSaveDialog(cardId.getScene().getWindow());
        if (target != null) {
            try {
                String xml = new XmlCardData(rows.stream()).toXml();
                // Writes using UTF-8
                Files.writeString(target.toPath(), xml,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                FxDialogBoxes.error("Exporting data failed.", e.getMessage());
            }
        }
    }

    public void importFromXml(ActionEvent actionEvent) {
        fileChooser.setTitle("Import cart data from XML file...");
        fileChooser.showOpenDialog(cardId.getScene().getWindow());

        FxDialogBoxes.error("TODO", "Not implemented yet!");
    }

    public void secReadPermissions(ActionEvent unused) {
        Sector sector = Sector.of(Integer.parseInt(secSector.getPlainText()));

        new ReadPermissionsCommand(uiServices, card,
                key.getKeyBytes(),
                useAsKeyChoiceBox.getValue(),
                sector,
                this::permissionsReadCallback)
                .runCommandAsync();
    }

    private void permissionsReadCallback(TrailerBlock trailerBlock) {
        AccessBits accessBits = trailerBlock.accessBits;
        secBlock0Perms.setValue(accessBits.dataBlockAccessForBlock(0));
        secBlock1Perms.setValue(accessBits.dataBlockAccessForBlock(1));
        secBlock2Perms.setValue(accessBits.dataBlockAccessForBlock(2));
        secTrailerPerms.setValue(accessBits.trailerBlockAccess());

        if (useAsKeyChoiceBox.getValue() == KEY_A) {
            secKeyA.loadKey(key.getKey(), key.getEncoding());
            secKeyB.loadKey(trailerBlock.keyBHexString(), KeyForm.Hex);
        } else {
            secKeyA.loadKey(trailerBlock.keyAHexString(), KeyForm.Hex);
            secKeyB.loadKey(key.getKey(), key.getEncoding());
        }
    }

    public void secWriteForSector(ActionEvent unused) {
        int sectorIndex = Integer.parseInt(secSector.getPlainText());

        TrailerBlock trailerBlock = new TrailerBlock();

        byte[] keyA = secKeyA.getKeyBytes();
        byte[] keyB = secKeyB.getKeyBytes();
        if (keyA == null || keyB == null) return;

        trailerBlock.setKeyA(keyA);
        trailerBlock.setKeyB(keyB);

        trailerBlock.accessBits = new AccessBits(
                secBlock0Perms.getValue(),
                secBlock1Perms.getValue(),
                secBlock2Perms.getValue(),
                secTrailerPerms.getValue());

        new WritePermissionsCommand(
                uiServices,
                card,
                key.getKeyBytes(), useAsKeyChoiceBox.getValue(),
                sectorIndex, sectorIndex + 1,
                trailerBlock
        ).runCommandAsync();
    }

    public void secWriteForEntireCard(ActionEvent actionEvent) {

    }

    public void showAboutDialog(ActionEvent unused) {
        AboutWindow.show();
    }

    public void displayFirmwareVersion(ActionEvent unused) {
        try {
            String terminalName = card.terminal().name();
            String firmware = card.getTerminalFirmwareVersion();

            FxDialogBoxes.info(String.format(
                    "Terminal name: %s" + System.lineSeparator() +
                            "Firmware version: %s.",
                    terminalName, firmware));
        } catch (Exception e) {
            FxDialogBoxes.error(
                    "Reading firmware version failed.",
                    e.getMessage());
        }
    }

    @FXML
    private void showSettingsDialog() {
        SettingsWindow.show(card);
    }

    @FXML
    public void closeWindow() {
        ((Stage) getScene().getWindow()).close();
    }

    @FXML
    private void showSenderDialog() {
        SenderWindow.show(card);
    }

    public static CardWindow show(AcrCard card, Window owner) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    CardWindow.class.getResource("/pl/marcinchwedczuk/rfid/gui/card/CardWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("MIFARE 1K Tag Editor");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initOwner(owner);
            childWindow.setMinWidth(640);
            childWindow.setMinHeight(480);
            childWindow.setResizable(true);

            childWindow.setOnCloseRequest(we -> card.disconnect());

            CardWindow controller = (CardWindow) loader.getController();
            controller.setCard(card);

            childWindow.sizeToScene();
            childWindow.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
