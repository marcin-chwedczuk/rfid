package pl.marcinchwedczuk.rfid.gui.utils;

import javafx.scene.control.ChoiceBox;

public class FxUtils {
    private FxUtils() { }


    public static <E extends Enum<E>>
    void enumChoiceBox(ChoiceBox<E> cb, E[] values) {
        cb.getItems().setAll(values);
    }
}
