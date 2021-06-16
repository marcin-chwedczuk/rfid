package pl.marcinchwedczuk.rfid.gui.settings;

import pl.marcinchwedczuk.javafx.validation.Input;
import pl.marcinchwedczuk.javafx.validation.converters.Converters;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.gui.abstractions.DialogBoxes;

import java.util.Objects;

public class PiccViewModel {
    public final Input<FeatureStatus, FeatureStatus> autoPiccPooling = new Input<>(Converters.identityConverter());
    public final Input<FeatureStatus, FeatureStatus> autoAtsGeneration = new Input<>(Converters.identityConverter());
    public final Input<PoolingInterval, PoolingInterval> pollingInterval = new Input<>(Converters.identityConverter());
    public final Input<DetectionStatus, DetectionStatus> feliCa212K = new Input<>(Converters.identityConverter());
    public final Input<DetectionStatus, DetectionStatus> feliCa424K = new Input<>(Converters.identityConverter());
    public final Input<DetectionStatus, DetectionStatus> topaz = new Input<>(Converters.identityConverter());
    public final Input<DetectionStatus, DetectionStatus> isoTypeA = new Input<>(Converters.identityConverter());
    public final Input<DetectionStatus, DetectionStatus> isoTypeB = new Input<>(Converters.identityConverter());

    private final DialogBoxes dialogBoxes;
    private final AcrTerminalCommands terminalCommands;

    public PiccViewModel(DialogBoxes dialogBoxes, AcrTerminalCommands terminalCommands) {
        this.dialogBoxes = Objects.requireNonNull(dialogBoxes);
        this.terminalCommands = Objects.requireNonNull(terminalCommands);

        setPicc(PiccOperatingParameter.newDefault());
    }

    public void refresh() {
        try {
            PiccOperatingParameter picc = terminalCommands.getPiccOperatingParameter();
            setPicc(picc);
        } catch (Exception e) {
            dialogBoxes.error("Cannot read PICC parameter.", e.getMessage());
        }
    }

    public void save() {
        try {
            PiccOperatingParameter picc = getPicc();
            terminalCommands.setPiccOperatingParameter(picc);

            dialogBoxes.info("PICC parameters were changed!");
        } catch (Exception e) {
            dialogBoxes.exception(e);
        }
    }

    private void setPicc(PiccOperatingParameter picc) {
        autoPiccPooling.setModelValue(picc.getAutoPiccPolling());
        autoAtsGeneration.setModelValue(picc.getAutoAtsGeneration());

        pollingInterval.setModelValue(picc.getPollingInterval());

        feliCa212K.setModelValue(picc.getFeliCa212K());
        feliCa424K.setModelValue(picc.getFeliCa424K());
        topaz.setModelValue(picc.getTopaz());
        isoTypeA.setModelValue(picc.getIso14443TypeA());
        isoTypeB.setModelValue(picc.getIso14443TypeB());
    }

    private PiccOperatingParameter getPicc() {
        PiccOperatingParameter picc = PiccOperatingParameter.newDefault();

        picc.setAutoPiccPolling(autoPiccPooling.getModelValue());
        picc.setAutoAtsGeneration(autoAtsGeneration.getModelValue());

        picc.setPollingInterval(pollingInterval.getModelValue());

        picc.setFeliCa212K(feliCa212K.getModelValue());
        picc.setFeliCa424K(feliCa424K.getModelValue());
        picc.setTopaz(topaz.getModelValue());
        picc.setIso14443TypeA(isoTypeA.getModelValue());
        picc.setIso14443TypeB(isoTypeB.getModelValue());

        return picc;
    }
}
