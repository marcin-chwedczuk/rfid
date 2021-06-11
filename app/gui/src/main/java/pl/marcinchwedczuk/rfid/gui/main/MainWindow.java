package pl.marcinchwedczuk.rfid.gui.main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.*;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;
import pl.marcinchwedczuk.rfid.gui.about.AboutWindow;
import pl.marcinchwedczuk.rfid.gui.card.CardWindow;
import pl.marcinchwedczuk.rfid.gui.settings.SettingsWindow;

import javax.smartcardio.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    @FXML
    private ComboBox<AcrTerminal> terminalsList;

    @FXML
    private Label infoScreen;

    @FXML
    private MenuItem settingsMenuItem;

    private final Timer timer = new Timer(true);
    private CardWindow cartWindow = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsMenuItem.disableProperty().bind(Bindings.createBooleanBinding(
                () -> terminalsList.getValue() == null,
                terminalsList.valueProperty()));

        refreshTerminalList();

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
        AcrTerminal terminal = currentTerminal();
        if (terminal == null || !terminal.isCardPresent()) {
            if (cartWindow != null) {
                cartWindow.closeWindow();
                cartWindow = null;
            }
        } else {
            if (cartWindow == null) {
                cartWindow = showCardWindow(terminal);
            }
        }
    }

    @FXML
    private void refreshTerminalList() {
        infoScreen.setText("Select terminal to start...");

        AcrTerminalProvider.setFakeTerminal(FakeCardTerminal.withCardPresent());

        terminalsList.getItems().clear();
        terminalsList.getItems().addAll(AcrTerminalProvider.INSTANCE.findTerminals());
        terminalsList.getSelectionModel().selectFirst();

        if (!terminalsList.getSelectionModel().isEmpty()) {
            infoScreen.setText("Put MIFARE 1K card on the terminal to start...");
        } else if (terminalsList.getItems().isEmpty()) {
            infoScreen.setText("No terminal found...");
        }
    }

    @FXML
    private void closeMainWindow() {
        timer.cancel();
        Stage s = (Stage)this.infoScreen.getScene().getWindow();
        s.close();
    }

    @FXML
    private void showAboutDialog() {
        AboutWindow.showModal(getWindow());
    }

    @FXML
    private void showSettings() {
        // TODO: Check terminal not present disable menu
        SettingsWindow.show(getWindow(), currentTerminal());
    }

    private AcrTerminal currentTerminal() {
        return terminalsList.getSelectionModel().getSelectedItem();
    }

    private CardWindow showCardWindow(AcrTerminal terminal) {
        AcrCard card = terminal.connect();
        return CardWindow.show(card, getWindow());
    }

    private Window getWindow() {
        return terminalsList.getScene().getWindow();
    }

    public void testAction(ActionEvent actionEvent) {
        try {
            /*
            Card direct = currentTerminal().getUnderlyingTerminal()
                    .connect("DIRECT");

            ATR atr = direct.getATR();
            logger.info("ATR = {}", atr);
             */

            Card card = currentTerminal().getUnderlyingTerminal()
                    .connect("T=0");
            try {
                card.disconnect(false);

                card.openLogicalChannel().getChannelNumber();
            } catch (Exception e) {
                logger.error("After disconnect!", e);
            }

            // Have to be closed
        }
        catch (Exception e) {
            logger.error("err", e);
        }
    }
}
