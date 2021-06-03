package pl.marcinchwedczuk.rfid.card.acr122.impl;

import pl.marcinchwedczuk.rfid.card.acr122.AcrException;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

public class AcrStandardErrors {
    private AcrStandardErrors() {
    }

    public static AcrException unexpectedResponseCode(int swCode) {
        return new AcrException(String.format("Unexpected response SW code 0x%04x.", swCode));
    }

    public static AcrException unexpectedResponseBytes(byte[] bytes) {
        return new AcrException(String.format(
                "Terminal returned unexpected response bytes: %s.",
                ByteArrays.toMacString(bytes)));
    }

    public static AcrException functionNotSupported() {
        return new AcrException("Function not supported.");
    }

    public static AcrException operationFailed() {
        return new AcrException("The operation failed.");
    }

    public static AcrException escapeCommandNotEnabled() {
        return new AcrException("PC/SC Escape command is not allowed.");
    }
}
