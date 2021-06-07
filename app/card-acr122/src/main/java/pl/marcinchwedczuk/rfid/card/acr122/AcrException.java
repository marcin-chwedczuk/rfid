package pl.marcinchwedczuk.rfid.card.acr122;

import javax.smartcardio.CardException;

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

    static AcrException ofCardException(CardException e) {
        return ofCardException(e, "%s", e.getMessage());
    }

    static AcrException ofCardException(CardException e, String format, Object... args) {
        return new AcrException(String.format(format, args), e);
    }
}
