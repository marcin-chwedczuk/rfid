package pl.marcinchwedczuk.rfid.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.marcinchwedczuk.rfid.acr122.AcrCard;
import pl.marcinchwedczuk.rfid.acr122.LedBuzzerSettings;
import pl.marcinchwedczuk.rfid.acr122.LedBuzzerSettings.Buzzer;
import pl.marcinchwedczuk.rfid.acr122.LedSettings;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.LedBlinkingMask;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.LedState;
import pl.marcinchwedczuk.rfid.acr122.LedSettings.StateMask;
import pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter;
import pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.PoolingInterval;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindow implements Initializable {
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
    @FXML private ChoiceBox<SkipDetect> isoTypeBCB;
    @FXML private ChoiceBox<SkipDetect> isoTypeACB;

    private AcrCard card;

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
        enumChoiceBox(isoTypeBCB, SkipDetect.values());
        enumChoiceBox(isoTypeACB, SkipDetect.values());
    }

    private static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }

    public void setCard(AcrCard card) {
        this.card = card;
    }

    public static SettingsWindow show(AcrCard card) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SettingsWindow.class.getResource("/pl/marcinchwedczuk/rfid/gui/SettingsWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("ACR122U Settings...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(false);

            SettingsWindow controller = (SettingsWindow)loader.getController();
            controller.setCard(card);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onSave(ActionEvent actionEvent) {

    }

    public void onCancel(ActionEvent actionEvent) {
        final Stage stage = (Stage) finalRedLedCB.getScene().getWindow();
        stage.close();
    }

    public enum EnableDisable { ENABLE, DISABLE }
    public enum SkipDetect { SKIP, DETECT }
}
