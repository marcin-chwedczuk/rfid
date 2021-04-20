package pl.marcinchwedczuk.rfid.gui.commands;

public class OperationFailedException extends RuntimeException {
    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
