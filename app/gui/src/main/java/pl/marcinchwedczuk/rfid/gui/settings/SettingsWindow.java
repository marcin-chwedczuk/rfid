package pl.marcinchwedczuk.rfid.gui.settings;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.gui.abstractions.DialogBoxes;
import pl.marcinchwedczuk.rfid.gui.utils.FxUtils;
import pl.marcinchwedczuk.rfid.gui.abstractions.impl.JavaFxDialogBoxes;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

// TODO: Split into window + two sub windows
public class SettingsWindow implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SettingsWindow.class);

    public static SettingsWindow show(Window owner, AcrTerminalCommands terminalCommands) {
        DialogBoxes dialogBoxes = new JavaFxDialogBoxes();

        PiccViewModel piccViewModel = new PiccViewModel(dialogBoxes, terminalCommands);
        LedBuzzViewModel ledBuzzViewModel = new LedBuzzViewModel(dialogBoxes, terminalCommands);

        try {
            PiccFragment piccFragment = PiccFragment.load(piccViewModel);
            LedBuzzFragment ledBuzzFragment = LedBuzzFragment.load(ledBuzzViewModel);

            SettingsWindow controller = new SettingsWindow(ledBuzzFragment, piccFragment);

            FXMLLoader loader = new FXMLLoader(
                    SettingsWindow.class.getResource("SettingsWindow.fxml"));
            loader.setController(controller);

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("Settings...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TitledPane remarksPane;

    @FXML
    private HBox childPanesContainer;

    private final LedBuzzFragment ledBuzzFragment;
    private final PiccFragment piccFragment;

    public SettingsWindow(LedBuzzFragment ledBuzzFragment, PiccFragment piccFragment) {
        this.ledBuzzFragment = Objects.requireNonNull(ledBuzzFragment);
        this.piccFragment = Objects.requireNonNull(piccFragment);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        remarksPane.expandedProperty().addListener((prop, oldValue, isAnimated) -> {
            Platform.runLater(() -> FxUtils.getStage(childPanesContainer).sizeToScene());
        });

        childPanesContainer.getChildren().addAll(
                ledBuzzFragment.getRoot(),
                piccFragment.getRoot());
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = FxUtils.getStage(childPanesContainer);
        if (stage != null) {
            stage.close();
        }
    }
}
