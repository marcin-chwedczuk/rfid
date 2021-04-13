package pl.marcinchwedczuk.rfid.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KeyBox extends HBox implements Initializable {
    private static final String HEX = "HEX";
    private static final String ASCII = "ASCII";

    @FXML private MaskedTextField keyTextField;
    @FXML private MenuButton modeMenu;

    private Tooltip validationTooltip;

    public KeyBox() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("KeyBox.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        // Ugly hack!
        // https://stackoverflow.com/questions/24016229/cant-import-custom-components-with-custom-cell-factories/24039826#24039826
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        modeMenu.getItems().clear();
        modeMenu.getItems().addAll(
                new MenuItem(HEX),
                new MenuItem(ASCII));
        for (var item: modeMenu.getItems()) {
            String text = item.getText();
            item.setOnAction((e) -> modeChanged(e, text));
        }
        modeChanged(null, HEX);

        keyTextField.setTooltip(null);
    }

    private void modeChanged(ActionEvent e, String text) {
        // TODO: Attempt to convert text
        switch (text) {
            case HEX:
                keyTextField.setMask("HH:HH:HH:HH:HH:HH");
                break;

            case ASCII:
                keyTextField.setMask("LLLLLL");
                break;

            default:
                throw new RuntimeException("Invalid key mode: " + text);
        }

        //keyTextField.setPlainText("");
        modeMenu.setText(text);
    }
}
