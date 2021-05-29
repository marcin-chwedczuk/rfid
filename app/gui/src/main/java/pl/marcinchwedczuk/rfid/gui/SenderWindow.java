package pl.marcinchwedczuk.rfid.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;

public class SenderWindow implements Initializable {

    private AcrTerminalCommands card;

    @FXML
    private TextArea bytesToSend;

    @FXML
    private TextArea response;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void sendCommand() {
        byte[] command = ByteArrays.fromHexString(bytesToSend.getText());
        try {
            byte[] responseBytes = card.sendRawCommand(command);
            response.setText(ByteArrays.toHexString(responseBytes));
        } catch (Exception e) {
            response.clear();

            response.appendText(e.toString());
            response.appendText(System.lineSeparator());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response.appendText(sw.toString());
        }
    }

    public static SenderWindow show(AcrTerminalCommands card) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/SenderWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("Command Sender");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(true);

            SenderWindow controller = loader.getController();
            controller.card = card;

            childWindow.show();
            childWindow.sizeToScene();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
