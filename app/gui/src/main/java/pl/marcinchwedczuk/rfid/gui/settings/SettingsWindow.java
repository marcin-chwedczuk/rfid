package pl.marcinchwedczuk.rfid.gui.settings;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TitledPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.javafx.validation.extras.UiBindings;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.gui.utils.DialogBoxes;
import pl.marcinchwedczuk.rfid.gui.utils.FxDialogBoxes;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// TODO: Split into window + two sub windows
public class SettingsWindow implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SettingsWindow.class);

    public static SettingsWindow show(Window owner, AcrTerminalCommands card) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SettingsWindow.class.getResource("SettingsWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("Settings...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);

            SettingsWindow controller = (SettingsWindow) loader.getController();
            controller.setTerminalCommands(card);
            childWindow.sizeToScene();

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // LED Settings
    @FXML
    private ChoiceBox<LedState> finalRedLedCB;
    @FXML
    private ChoiceBox<LedState> finalGreenLedCB;
    @FXML
    private ChoiceBox<StateMask> maskRedLedCB;
    @FXML
    private ChoiceBox<StateMask> maskGreenLedCB;
    @FXML
    private ChoiceBox<LedState> blinkingRedLedCB;
    @FXML
    private ChoiceBox<LedState> blinkingGreenLedCB;
    @FXML
    private ChoiceBox<LedBlinkingMask> blinkingMaskRedLedCB;
    @FXML
    private ChoiceBox<LedBlinkingMask> blinkingMaskGreenLedCB;

    // Buzzer Settings
    @FXML
    private Spinner<Integer> t1Spinner;
    @FXML
    private Spinner<Integer> t2Spinner;
    @FXML
    private Spinner<Integer> repetitionsSpinner;
    @FXML
    private ChoiceBox<Buzzer> linkToBuzzerCB;

    // PICC Parameter Settings
    @FXML
    private ChoiceBox<FeatureStatus> autoPiccPooling;
    @FXML
    private ChoiceBox<FeatureStatus> autoAtsGeneration;

    @FXML
    private ChoiceBox<PoolingInterval> pollingInterval;

    @FXML
    private ChoiceBox<DetectionStatus> feliCa212K;
    @FXML
    private ChoiceBox<DetectionStatus> feliCa424K;
    @FXML
    private ChoiceBox<DetectionStatus> topaz;
    @FXML
    private ChoiceBox<DetectionStatus> isoTypeA;
    @FXML
    private ChoiceBox<DetectionStatus> isoTypeB;

    @FXML
    private TitledPane remarksPane;

    private final DialogBoxes dialogBoxes = new FxDialogBoxes();

    private AcrTerminalCommands terminalCommands;
    private PiccViewModel piccViewModel;
    private LedBuzzViewModel ledBuzzViewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        enumChoiceBox(finalRedLedCB, LedState.values());
        enumChoiceBox(finalGreenLedCB, LedState.values());

        enumChoiceBox(maskRedLedCB, StateMask.values());
        enumChoiceBox(maskGreenLedCB, StateMask.values());

        enumChoiceBox(blinkingRedLedCB, LedState.values());
        enumChoiceBox(blinkingGreenLedCB, LedState.values());

        enumChoiceBox(blinkingMaskRedLedCB, LedBlinkingMask.values());
        enumChoiceBox(blinkingMaskGreenLedCB, LedBlinkingMask.values());

        t1Spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 10));
        t2Spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 10));
        repetitionsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 3));
        enumChoiceBox(linkToBuzzerCB, Buzzer.values());

        enumChoiceBox(autoPiccPooling, FeatureStatus.values());
        enumChoiceBox(autoAtsGeneration, FeatureStatus.values());
        enumChoiceBox(pollingInterval, PoolingInterval.values());
        enumChoiceBox(feliCa212K, DetectionStatus.values());
        enumChoiceBox(feliCa424K, DetectionStatus.values());
        enumChoiceBox(topaz, DetectionStatus.values());
        enumChoiceBox(isoTypeA, DetectionStatus.values());
        enumChoiceBox(isoTypeB, DetectionStatus.values());

        remarksPane.expandedProperty().addListener((prop, oldValue, isAnimated) -> {
            Platform.runLater(() -> getStage().sizeToScene());
        });
    }

    public void setTerminalCommands(AcrTerminalCommands terminalCommands) {
        this.terminalCommands = terminalCommands;

        setupPiccViewModel();
        setupLedBuzzViewModel(terminalCommands);
    }

    private void setupPiccViewModel() {
        this.piccViewModel = new PiccViewModel(dialogBoxes, terminalCommands);

        UiBindings.biBind(autoPiccPooling, piccViewModel.autoPiccPooling);
        UiBindings.biBind(autoAtsGeneration, piccViewModel.autoAtsGeneration);

        UiBindings.biBind(pollingInterval, piccViewModel.pollingInterval);

        UiBindings.biBind(feliCa212K, piccViewModel.feliCa212K);
        UiBindings.biBind(feliCa424K, piccViewModel.feliCa424K);
        UiBindings.biBind(topaz, piccViewModel.topaz);
        UiBindings.biBind(isoTypeA, piccViewModel.isoTypeA);
        UiBindings.biBind(isoTypeB, piccViewModel.isoTypeB);

        piccViewModel.refresh();
    }

    private void setupLedBuzzViewModel(AcrTerminalCommands terminalCommands) {
        this.ledBuzzViewModel = new LedBuzzViewModel(dialogBoxes, terminalCommands);

        UiBindings.biBind(finalRedLedCB, ledBuzzViewModel.finalRedLed);
        UiBindings.biBind(finalGreenLedCB, ledBuzzViewModel.finalGreenLed);

        UiBindings.biBind(maskRedLedCB, ledBuzzViewModel.maskRedLed);
        UiBindings.biBind(maskGreenLedCB, ledBuzzViewModel.maskGreenLed);

        UiBindings.biBind(blinkingRedLedCB, ledBuzzViewModel.blinkingRedLed);
        UiBindings.biBind(blinkingGreenLedCB, ledBuzzViewModel.blinkingGreenLed);

        UiBindings.biBind(blinkingMaskRedLedCB, ledBuzzViewModel.blinkingMaskRedLed);
        UiBindings.biBind(blinkingMaskGreenLedCB, ledBuzzViewModel.blinkingMaskGreenLed);

        UiBindings.biBind(t1Spinner, ledBuzzViewModel.t1);
        UiBindings.biBind(t2Spinner, ledBuzzViewModel.t2);
        UiBindings.biBind(repetitionsSpinner, ledBuzzViewModel.repetitions);
        UiBindings.biBind(linkToBuzzerCB, ledBuzzViewModel.linkToBuzzer);
    }

    @FXML
    private void refreshPICC() {
        piccViewModel.refresh();
    }

    @FXML
    private void savePICC() {
        piccViewModel.save();
    }

    @FXML
    public void sendBlinkBuzzCommand() {
        ledBuzzViewModel.sendCommand();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    public void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getStage() {
        Scene scene = finalRedLedCB.getScene();
        if (scene == null) {
            return null;
        }

        Window window = scene.getWindow();
        if (window == null) {
            return null;
        }

        return (Stage) window;
    }

    private static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }
}
