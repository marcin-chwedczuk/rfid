package pl.marcinchwedczuk.rfid.gui.settings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TitledPane;
import pl.marcinchwedczuk.javafx.validation.extras.UiBindings;
import pl.marcinchwedczuk.rfid.card.acr122.Buzzer;
import pl.marcinchwedczuk.rfid.card.acr122.LedBlinkingMask;
import pl.marcinchwedczuk.rfid.card.acr122.LedState;
import pl.marcinchwedczuk.rfid.card.acr122.StateMask;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LedBuzzFragment implements Initializable {
    public static LedBuzzFragment load(LedBuzzViewModel viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(PiccFragment.class.getResource("LedBuzzFragment.fxml"));
            LedBuzzFragment controller = new LedBuzzFragment(viewModel);

            loader.setController(controller);
            loader.load();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TitledPane ledBuzzFragment;

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

    @FXML
    private Spinner<Integer> t1Spinner;
    @FXML
    private Spinner<Integer> t2Spinner;
    @FXML
    private Spinner<Integer> repetitionsSpinner;
    @FXML
    private ChoiceBox<Buzzer> linkToBuzzerCB;

    private final LedBuzzViewModel ledBuzzViewModel;

    public LedBuzzFragment(LedBuzzViewModel ledBuzzViewModel) {
        this.ledBuzzViewModel = Objects.requireNonNull(ledBuzzViewModel);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        // Setup ViewModel
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
    private void sendBlinkBuzzCommand() {
        ledBuzzViewModel.sendCommand();
    }

    public Node getRoot() {
        return ledBuzzFragment;
    }

    private static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }
}
