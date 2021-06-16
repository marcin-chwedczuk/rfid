package pl.marcinchwedczuk.rfid.gui.controls.banner;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Banner extends HBox implements Initializable {
    private static PseudoClass CSS_TYPE_INFO = PseudoClass.getPseudoClass("type-info");
    private static PseudoClass CSS_TYPE_WARNING = PseudoClass.getPseudoClass("type-warning");
    private static PseudoClass CSS_TYPE_ERROR = PseudoClass.getPseudoClass("type-error");

    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(this, "type", Type.INFO);
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    @FXML
    private Text msg;

    public Banner() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "Banner.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        // Hack for scene builder "Banner.fxml file not found" error.
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        type.addListener(observable -> {
            updateStyle();
        });
        updateStyle();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (msg == null) {
            // For some reason this field is not incjected in Scene builder
            for (Node child : this.getChildrenUnmodifiable()) {
                if (child instanceof Text) {
                    this.msg = (Text) child;
                    break;
                }
            }
        }

        msg.textProperty().bindBidirectional(text);
        msg.textProperty().set(text.get());
    }

    private void updateStyle() {
        Type t = type.getValue();
        pseudoClassStateChanged(CSS_TYPE_INFO, (t == Type.INFO));
        pseudoClassStateChanged(CSS_TYPE_WARNING, (t == Type.WARN));
        pseudoClassStateChanged(CSS_TYPE_ERROR, (t == Type.ERROR));
    }

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public enum Type {
        INFO, WARN, ERROR
    }
}
