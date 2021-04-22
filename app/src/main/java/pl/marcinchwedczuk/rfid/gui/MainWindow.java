package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.marcinchwedczuk.rfid.lib.*;

import javax.smartcardio.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow {
    @FXML public ComboBox<AcrTerminal> terminalsList;
    @FXML public Label infoScreen;

    private final Timer timer = new Timer(true);
    private Optional<Stage> cartWindow = Optional.empty();

    private void out(String format, Object... args) {
    }

    @FXML
    public void initialize() {
        refreshTerminalList(null);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    checkCardStateChanged();
                });
            }
        }, 1000, 500);
    }

    private void checkCardStateChanged() {
        currentTerminal().ifPresent(
                terminal -> {
                    if (!terminal.isCardPresent()) {
                        cartWindow.ifPresent(stage -> stage.close());
                        cartWindow = Optional.empty();
                    } else {
                        if (cartWindow.isEmpty()) {
                            cartWindow = Optional.of(showCardWindow());
                        }
                    }
                });
    }

    private Stage showCardWindow() {
        AcrCard card = currentTerminal().get().connect();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/pl/marcinchwedczuk/rfid/gui/CardWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("MIFARE 1K Tag Editor");
            childWindow.setScene(new Scene(loader.load(), 1500, 1024));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initOwner(terminalsList.getScene().getWindow());
            //childWindow.setMinWidth(1000);
            //childWindow.setMinHeight(640);

            childWindow.setOnCloseRequest(we -> card.disconnect());
            ((CardWindow)loader.getController()).setCard(card);

            childWindow.show();
            return childWindow;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshTerminalList(ActionEvent actionEvent) {
        infoScreen.setText("Select terminal to start...");

        terminalsList.getItems().clear();
        terminalsList.getItems().addAll(AcrTerminal.getTerminals());
        terminalsList.getSelectionModel().selectFirst();

        if (!terminalsList.getSelectionModel().isEmpty()) {
            infoScreen.setText("Put MIFARE 1K card on the terminal to start...");
        } else if (terminalsList.getItems().isEmpty()) {
            infoScreen.setText("No terminal found...");
        }
    }

    private Optional<AcrTerminal> currentTerminal() {
        return Optional.ofNullable(terminalsList.getSelectionModel().getSelectedItem());
    }

    public void closeMainWindow(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void showAboutDialog(ActionEvent actionEvent) {
        AboutWindow.show();
    }

    public void test(ActionEvent actionEvent) {
        try {
            String tmp = currentTerminal().get().getPiccOperatingParameter().toString();

            FxDialogBoxes.info(tmp);
        } catch (Exception e) {
            e.printStackTrace();
            FxDialogBoxes.error(
                    "FAILURE",
                    e.getMessage());
        }
    }
}
