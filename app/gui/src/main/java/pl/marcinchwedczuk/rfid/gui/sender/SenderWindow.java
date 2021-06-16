package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.javafx.validation.extras.UiBindings;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import pl.marcinchwedczuk.rfid.gui.abstractions.TimeProvider;
import pl.marcinchwedczuk.rfid.gui.abstractions.impl.SystemTimeTimeProvider;
import pl.marcinchwedczuk.rfid.gui.controls.banner.Banner;
import pl.marcinchwedczuk.rfid.gui.utils.FxUtils;

public class SenderWindow implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SenderWindow.class);

    public static SenderWindow show(AcrTerminalCommands terminalCommands) {
        TimeProvider timeProvider = new SystemTimeTimeProvider();
        SenderViewModel viewModel = new SenderViewModel(timeProvider, terminalCommands);
        SenderWindow controller = new SenderWindow(viewModel);

        try {
            FXMLLoader loader = new FXMLLoader(
                    SenderWindow.class.getResource("SenderWindow.fxml"));
            loader.setController(controller);

            Stage childWindow = new Stage();
            childWindow.setTitle("Command Sender");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(true);

            childWindow.show();
            childWindow.sizeToScene();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TextArea outputTextArea;

    @FXML
    private TextArea inputTextArea;

    @FXML
    private CheckBox clearOnSendFlag;

    @FXML
    private Banner errorBanner;

    private final SenderViewModel viewModel;

    public SenderWindow(SenderViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        outputTextArea.textProperty()
                .bind(viewModel.outputTextProperty());
        UiBindings.biBind(inputTextArea, viewModel.commandText);

        errorBanner.textProperty()
                .bind(viewModel.errorMessageProperty());

        UiBindings.biBind(clearOnSendFlag, viewModel.clearOnSendFlag);

        errorBanner.visibleProperty()
                .bind(viewModel.showErrorProperty());
        errorBanner.managedProperty()
                .bind(viewModel.showErrorProperty());

        // Scroll output to end when new text appears
        outputTextArea.textProperty()
                .addListener((o, oldValue, newValue) -> {
                    Platform.runLater(() -> outputTextArea.end());
                });
    }

    @FXML
    private void send() {
        viewModel.send();
    }

    @FXML
    private void clear() {
        viewModel.clear();
    }

    @FXML
    private void close() {
        FxUtils.getStage(inputTextArea).close();
    }
}
