package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.marcinchwedczuk.rfid.acr122.*;
import pl.marcinchwedczuk.rfid.acr122.LedBuzzerSettings.Buzzer;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.LedBlinkingMask;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.LedState;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.StateMask;
import pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.EnableDisable;
import pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.PoolingInterval;
import pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.SkipDetect;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


public class SettingsWindow implements Initializable {
    private static Logger logger = LogManager.getLogger(SettingsWindow.class);

    // LED Settings
    @FXML private ChoiceBox<LedState> finalRedLedCB;
    @FXML private ChoiceBox<LedState> finalGreenLedCB;
    @FXML private ChoiceBox<StateMask> maskRedLedCB;
    @FXML private ChoiceBox<StateMask> maskGreenLedCB;
    @FXML private ChoiceBox<LedState> blinkingRedLedCB;
    @FXML private ChoiceBox<LedState> blinkingGreenLedCB;
    @FXML private ChoiceBox<LedBlinkingMask> blinkingMaskRedLedCB;
    @FXML private ChoiceBox<LedBlinkingMask> blinkingMaskGreenLedCB;

    // Buzzer Settings
    @FXML private Spinner<Integer> t1Spinner;
    @FXML private Spinner<Integer> t2Spinner;
    @FXML private Spinner<Integer> repetitionsSpinner;
    @FXML private ChoiceBox<Buzzer> linkToBuzzerCB;

    // PICC Parameter Settings
    @FXML private ChoiceBox<EnableDisable> autoPiccPoolingCB;
    @FXML private ChoiceBox<EnableDisable> autoAtsGenerationCB;
    @FXML private ChoiceBox<PoolingInterval> pollingIntervalCB;
    @FXML private ChoiceBox<SkipDetect> feliCa424KCB;
    @FXML private ChoiceBox<SkipDetect> feliCa212KCB;
    @FXML private ChoiceBox<SkipDetect> topazCB;
    @FXML private ChoiceBox<SkipDetect> isoTypeB_CB;
    @FXML private ChoiceBox<SkipDetect> isoTypeA_CB;

    @FXML private TitledPane remarksPane;

    private AcrTerminalCommands terminalCommands;

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

        enumChoiceBox(autoPiccPoolingCB, EnableDisable.values());
        enumChoiceBox(autoAtsGenerationCB, EnableDisable.values());
        enumChoiceBox(pollingIntervalCB, PoolingInterval.values());
        enumChoiceBox(feliCa424KCB, SkipDetect.values());
        enumChoiceBox(feliCa212KCB, SkipDetect.values());
        enumChoiceBox(topazCB, SkipDetect.values());
        enumChoiceBox(isoTypeB_CB, SkipDetect.values());
        enumChoiceBox(isoTypeA_CB, SkipDetect.values());

        remarksPane.expandedProperty().addListener((prop, oldValue, isAnimated) -> {
            Platform.runLater(() -> getStage().sizeToScene());
        });
    }

    public void setTerminalCommands(AcrTerminalCommands terminalCommands) {
        this.terminalCommands = terminalCommands;

        refreshPICC();
        loadDefaultsForLedSettings();
        loadDefaultsForBuzzerSettings();
    }

    @FXML
    private void refreshPICC() {
        PiccOperatingParameter picc;
        try {
            picc = terminalCommands.readPiccOperatingParameter();
        } catch (Exception e) {
            FxDialogBoxes.error("Cannot read PICC parameter.", e.getMessage());
            return;
        }

        setPiccOperatingParameter(picc);
    }

    @FXML
    private void savePICC() {
        PiccOperatingParameter picc = new PiccOperatingParameter();
        picc.setAutoPiccPolling(this.autoPiccPoolingCB.getValue());
        picc.setAutoAtsGeneration(this.autoAtsGenerationCB.getValue());
        picc.setPollingInterval(this.pollingIntervalCB.getValue());
        picc.setFeliCa424K(this.feliCa424KCB.getValue());
        picc.setFeliCa212K(this.feliCa212KCB.getValue());
        picc.setTopaz(this.topazCB.getValue());
        picc.setIso14443TypeB(this.isoTypeB_CB.getValue());
        picc.setIso14443TypeA(this.isoTypeA_CB.getValue());

        try {
            terminalCommands.savePiccOperatingParameter(picc);
            FxDialogBoxes.info("PICC parameters were changed!");
        } catch (Exception e) {
            FxDialogBoxes.exception(e);
        }
    }


    private void setPiccOperatingParameter(PiccOperatingParameter parameter) {
        this.autoPiccPoolingCB.setValue(parameter.getAutoPiccPolling());
        this.autoAtsGenerationCB.setValue(parameter.getAutoAtsGeneration());

        this.pollingIntervalCB.setValue(parameter.getPollingInterval());

        this.feliCa424KCB.setValue(parameter.getFeliCa424K());
        this.feliCa212KCB.setValue(parameter.getFeliCa212K());
        this.topazCB.setValue(parameter.getTopaz());
        this.isoTypeB_CB.setValue(parameter.getIso14443TypeB());
        this.isoTypeA_CB.setValue(parameter.getIso14443TypeA());
    }

    private void loadDefaultsForLedSettings() {
        this.finalRedLedCB.setValue(LedState.OFF);
        this.finalGreenLedCB.setValue(LedState.OFF);

        this.maskRedLedCB.setValue(StateMask.UPDATE);
        this.maskGreenLedCB.setValue(StateMask.UPDATE);

        this.blinkingRedLedCB.setValue(LedState.OFF);
        this.blinkingGreenLedCB.setValue(LedState.OFF);

        this.blinkingMaskRedLedCB.setValue(LedBlinkingMask.NOT_BLINK);
        this.blinkingMaskGreenLedCB.setValue(LedBlinkingMask.NOT_BLINK);
    }

    private void loadDefaultsForBuzzerSettings() {
        this.t1Spinner.getValueFactory().setValue(3);
        this.t2Spinner.getValueFactory().setValue(3);

        this.repetitionsSpinner.getValueFactory().setValue(1);
        this.linkToBuzzerCB.setValue(Buzzer.BUZZER_DURING_T1);
    }


    @FXML
    public void sendBlinkBuzzCommand() {
        // Save Led & Buzzer
        LedBuzzerSettings newSettings = new LedBuzzerSettings();
        newSettings.setT1(t1Spinner.getValue());
        newSettings.setT2(t2Spinner.getValue());
        newSettings.setNumberOfRepetitions(repetitionsSpinner.getValue());
        newSettings.setLinkToBuzzer(linkToBuzzerCB.getValue());

        // LED Stuff
        LedSettings ledSettings = newSettings.getLedSettings();
        ledSettings.setFinalRedLED(this.finalRedLedCB.getValue());
        ledSettings.setFinalGreenLED(this.finalGreenLedCB.getValue());
        ledSettings.setMaskRedLED(this.maskRedLedCB.getValue());
        ledSettings.setMaskGreenLED(this.maskGreenLedCB.getValue());
        ledSettings.setInitialBlinkingRedLED(this.blinkingRedLedCB.getValue());
        ledSettings.setInitialBlinkingGreenLED(this.blinkingGreenLedCB.getValue());
        ledSettings.setBlinkingMaskRedLED(this.blinkingMaskRedLedCB.getValue());
        ledSettings.setBlinkingMaskGreenLED(this.blinkingMaskGreenLedCB.getValue());

        try {
            terminalCommands.configureLedAndBuzzer(newSettings);
        } catch (Exception e) {
            FxDialogBoxes.exception(e);
        }
    }

    @FXML
    private void onCancel(ActionEvent actionEvent) {
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

        return (Stage)window;
    }

    public static SettingsWindow show(AcrTerminalCommands card) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SettingsWindow.class.getResource("/pl/marcinchwedczuk/rfid/gui/SettingsWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("ACR122U Settings...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(false);

            SettingsWindow controller = (SettingsWindow)loader.getController();
            controller.setTerminalCommands(card);
            childWindow.sizeToScene();

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }
}
