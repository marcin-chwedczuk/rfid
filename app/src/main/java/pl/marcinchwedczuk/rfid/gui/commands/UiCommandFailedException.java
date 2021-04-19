package pl.marcinchwedczuk.rfid.gui.commands;

public class UiCommandFailedException extends RuntimeException {
    public UiCommandFailedException(String message) {
        super(message);
    }
}
