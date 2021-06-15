package pl.marcinchwedczuk.rfid.gui.settings;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import pl.marcinchwedczuk.javafx.validation.extras.UiBindings;
import pl.marcinchwedczuk.rfid.card.acr122.DetectionStatus;
import pl.marcinchwedczuk.rfid.card.acr122.FeatureStatus;
import pl.marcinchwedczuk.rfid.card.acr122.PoolingInterval;
import pl.marcinchwedczuk.rfid.gui.utils.FxUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class PiccFragment implements Initializable {
    public static PiccFragment load(PiccViewModel viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(PiccFragment.class.getResource("PiccFragment.fxml"));
            PiccFragment controller = new PiccFragment(viewModel);

            loader.setController(controller);
            loader.load();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TitledPane piccFragment;

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

    private final PiccViewModel piccViewModel;

    public PiccFragment(PiccViewModel piccViewModel) {
        this.piccViewModel = Objects.requireNonNull(piccViewModel);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup view
        FxUtils.enumChoiceBox(autoPiccPooling, FeatureStatus.values());
        FxUtils.enumChoiceBox(autoAtsGeneration, FeatureStatus.values());
        FxUtils.enumChoiceBox(pollingInterval, PoolingInterval.values());
        FxUtils.enumChoiceBox(feliCa212K, DetectionStatus.values());
        FxUtils.enumChoiceBox(feliCa424K, DetectionStatus.values());
        FxUtils.enumChoiceBox(topaz, DetectionStatus.values());
        FxUtils.enumChoiceBox(isoTypeA, DetectionStatus.values());
        FxUtils.enumChoiceBox(isoTypeB, DetectionStatus.values());

        // Bind to ViewModel
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

    @FXML
    private void refreshPICC() {
        piccViewModel.refresh();
    }

    @FXML
    private void savePICC() {
        piccViewModel.save();
    }

    public Node getRoot() {
        return piccFragment;
    }
}
