package pl.marcinchwedczuk.rfid.acr122;

public class AcrStandardErrors {
    private AcrStandardErrors() { }

    public static AcrException unexpectedResponseCode(int swCode) {
        return new AcrException(String.format("Unexpected response SW code 0x%04x.", swCode));
    }

    public static AcrException functionNotSupported() {
        return new AcrException("Function not supported.");
    }

    public static AcrException operationFailed() {
        return new AcrException("The operation failed.");
    }
}
