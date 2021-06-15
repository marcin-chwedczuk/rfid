package pl.marcinchwedczuk.rfid.gui.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FxUtils {
    private FxUtils() { }

    public static Stage getStage(Node node) {
        Scene scene = node.getScene();
        if (scene == null) {
            return null;
        }

        Window window = scene.getWindow();
        if (window == null) {
            return null;
        }

        return (Stage) window;
    }

    public static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }
}
