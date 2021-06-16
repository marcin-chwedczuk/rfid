package pl.marcinchwedczuk.rfid.gui.abstractions;

public interface DialogBoxes {
    void info(String message);

    void error(String message);
    void error(String title, String message);

    void exception(Throwable ex);
}
