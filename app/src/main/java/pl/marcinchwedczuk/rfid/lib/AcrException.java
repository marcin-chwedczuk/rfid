package pl.marcinchwedczuk.rfid.lib;

public class AcrException extends RuntimeException {
    public AcrException() {
    }

    public AcrException(String message) {
        super(message);
    }

    public AcrException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcrException(Throwable cause) {
        super(cause);
    }

    public AcrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
