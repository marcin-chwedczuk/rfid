package pl.marcinchwedczuk.rfid.gui.settings;

import javafx.fxml.FXML;
import pl.marcinchwedczuk.javafx.validation.Input;
import pl.marcinchwedczuk.javafx.validation.converters.Converters;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.gui.utils.DialogBoxes;

import java.util.Objects;

public class LedBuzzViewModel {
    public final Input<LedState, LedState> finalRedLed =
            new Input<LedState, LedState>(Converters.identityConverter());

    public final Input<LedState, LedState> finalGreenLed =
            new Input<LedState, LedState>(Converters.identityConverter());

    public final Input<StateMask, StateMask> maskRedLed =
            new Input<StateMask, StateMask>(Converters.identityConverter());

    public final Input<StateMask, StateMask> maskGreenLed =
            new Input<StateMask, StateMask>(Converters.identityConverter());

    public final Input<LedState, LedState> blinkingRedLed =
            new Input<LedState, LedState>(Converters.identityConverter());

    public final Input<LedState, LedState> blinkingGreenLed =
            new Input<LedState, LedState>(Converters.identityConverter());

    public final Input<LedBlinkingMask, LedBlinkingMask> blinkingMaskRedLed =
            new Input<LedBlinkingMask, LedBlinkingMask>(Converters.identityConverter());

    public final Input<LedBlinkingMask, LedBlinkingMask> blinkingMaskGreenLed =
            new Input<LedBlinkingMask, LedBlinkingMask>(Converters.identityConverter());

    // Buzzer Settings
    public final Input<Integer, Integer> t1 =
            new Input<Integer, Integer>(Converters.identityConverter());

    public final Input<Integer, Integer> t2 =
            new Input<Integer, Integer>(Converters.identityConverter());

    public final Input<Integer, Integer> repetitions =
            new Input<Integer, Integer>(Converters.identityConverter());

    public final Input<Buzzer, Buzzer> linkToBuzzer =
            new Input<Buzzer, Buzzer>(Converters.identityConverter());

    private final DialogBoxes dialogBoxes;
    private final AcrTerminalCommands terminalCommands;

    public LedBuzzViewModel(DialogBoxes dialogBoxes, AcrTerminalCommands terminalCommands) {
        this.dialogBoxes = Objects.requireNonNull(dialogBoxes);
        this.terminalCommands = Objects.requireNonNull(terminalCommands);

        mapToViewModel(LedBuzzerSettings.newDefaults());
    }

    @FXML
    public void sendCommand() {
        LedBuzzerSettings newSettings = mapViewModelToDomain();

        try {
            terminalCommands.configureLedAndBuzzer(newSettings);
        } catch (Exception e) {
            dialogBoxes.exception(e);
        }
    }

    private void mapToViewModel(LedBuzzerSettings settings) {
        LedSettings ledSettings = settings.getLedSettings();

        finalRedLed.setModelValue(ledSettings.getFinalRedLED());
        finalGreenLed.setModelValue(ledSettings.getFinalGreenLED());

        maskRedLed.setModelValue(ledSettings.getMaskRedLED());
        maskGreenLed.setModelValue(ledSettings.getMaskGreenLED());

        blinkingRedLed.setModelValue(ledSettings.getInitialBlinkingRedLED());
        blinkingGreenLed.setModelValue(ledSettings.getInitialBlinkingGreenLED());

        blinkingMaskRedLed.setModelValue(ledSettings.getBlinkingMaskRedLED());
        blinkingMaskGreenLed.setModelValue(ledSettings.getBlinkingMaskGreenLED());

        t1.setModelValue(settings.getT1().units());
        t2.setModelValue(settings.getT2().units());
        repetitions.setModelValue(settings.getNumberOfRepetitions());
        linkToBuzzer.setModelValue(settings.getLinkToBuzzer());
    }

    private LedBuzzerSettings mapViewModelToDomain() {
        LedBuzzerSettings settings = LedBuzzerSettings.newDefaults();
        LedSettings ledSettings = settings.getLedSettings();

        ledSettings.setFinalRedLED(finalRedLed.getModelValue());
        ledSettings.setFinalGreenLED(finalGreenLed.getModelValue());

        ledSettings.setMaskRedLED(maskRedLed.getModelValue());
        ledSettings.setMaskGreenLED(maskGreenLed.getModelValue());

        ledSettings.setInitialBlinkingRedLED(blinkingRedLed.getModelValue());
        ledSettings.setInitialBlinkingGreenLED(blinkingGreenLed.getModelValue());

        ledSettings.setBlinkingMaskRedLED(blinkingMaskRedLed.getModelValue());
        ledSettings.setBlinkingMaskGreenLED(blinkingMaskGreenLed.getModelValue());

        settings.setT1(TDuration.fromNumberOf100msUnits(t1.getModelValue()));
        settings.setT2(TDuration.fromNumberOf100msUnits(t2.getModelValue()));
        settings.setNumberOfRepetitions(repetitions.getModelValue());
        settings.setLinkToBuzzer(linkToBuzzer.getModelValue());

        return settings;
    }
}
