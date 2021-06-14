package pl.marcinchwedczuk.rfid.gui.utils;

public interface DialogBoxes {
    void info(String message);

    void error(String message);
    void error(String title, String message);

    void exception(Throwable ex);
}
