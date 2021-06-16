package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.marcinchwedczuk.javafx.validation.extras.UiBindings;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.gui.controls.banner.Banner;
import pl.marcinchwedczuk.rfid.gui.progress.FxProgressDialog;
import pl.marcinchwedczuk.rfid.gui.utils.FxUtils;

public class SenderWindow implements Initializable {

    public static SenderWindow show(AcrTerminalCommands terminalCommands) {
        SenderViewModel viewModel = new SenderViewModel(terminalCommands);
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

        errorBanner.visibleProperty()
                .bind(viewModel.showErrorProperty());
        errorBanner.managedProperty()
                .bind(viewModel.showErrorProperty());
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
