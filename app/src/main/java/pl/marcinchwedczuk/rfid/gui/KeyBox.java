package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import pl.marcinchwedczuk.rfid.lib.ByteUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KeyBox extends HBox implements Initializable {
    private final PseudoClass cssInvalid = PseudoClass.getPseudoClass("invalid");

    @FXML private MaskedTextField keyTextField;
    @FXML private ChoiceBox<Encoding> modeMenu;
    @FXML private Tooltip validationTooltip;

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
        modeMenu.getItems().setAll(Encoding.values());

        modeMenu.setOnAction(event -> {
            Encoding current = modeMenu.getValue();
            modeChanged(current);
        });

        modeMenu.getSelectionModel().select(0);

        keyTextField.setTooltip(null);
        keyTextField.focusedProperty().addListener((prop, oldValue, hasFocus) -> {
            if (!hasFocus) {
                Platform.runLater(this::validate);
            }
        });
    }

    public boolean validate() {
        boolean isValid = !keyTextField.getText().contains("_");
        keyTextField.pseudoClassStateChanged(cssInvalid, !isValid);
        if (!isValid) {
            validationTooltip.setText("Provide proper value for this field!");
            keyTextField.setTooltip(validationTooltip);
        } else {
            keyTextField.setTooltip(null);
        }
        return isValid;
    }

    public String getKey() {
        if (!validate()) return null;
        return keyTextField.getPlainText();
    }

    public byte[] getKeyBytes() {
        String key = getKey();
        if (key == null) return null;
        return ByteUtils.fromHexString(key, ":");
    }

    public Encoding getEncoding() {
        return Encoding.Hex;
    }

    public void loadKey(String key, Encoding encoding) {
        modeMenu.getSelectionModel().select(encoding);
        keyTextField.setPlainText(key);
        Platform.runLater(this::validate);
    }

    private void modeChanged(Encoding text) {
        // TODO: Attempt to convert text
        switch (text) {
            case Hex:
                keyTextField.setMask("HH:HH:HH:HH:HH:HH");
                break;

            case Ascii:
                keyTextField.setMask("LLLLLL");
                break;

            default:
                throw new RuntimeException("Invalid key mode: " + text);
        }
    }
}
