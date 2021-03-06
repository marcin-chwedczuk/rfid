package pl.marcinchwedczuk.rfid.gui.card;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.StringConverter;
import pl.marcinchwedczuk.rfid.gui.utils.KeyForm;

class DataRowByteStringConverter extends StringConverter<Byte> {
    private static final String NON_PRINTABLE_CHARACTER_PLACEHOLDER = "\uFFFD";

    private final ReadOnlyObjectProperty<KeyForm> encoding;

    DataRowByteStringConverter(ReadOnlyObjectProperty<KeyForm> encoding) {
        this.encoding = encoding;
    }

    @Override
    public String toString(Byte b) {
        if (b == null) {
            return "";
        }

        return encoding.get() == KeyForm.Hex
                ? byteToHex(b)
                : byteToPrintableAscii(b);
    }

    @Override
    public Byte fromString(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }

        return encoding.get() == KeyForm.Hex
                ? hexToByte(string)
                : characterToByte(string);
    }

    private Byte hexToByte(String string) {
        if (string == null || string.length() != 2) {
            return null;
        }

        try {
            return (byte) Integer.parseInt(string, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Byte characterToByte(String string) {
        if (string == null || string.length() != 1) {
            return null;
        }

        return isAsciiPrintable(string.charAt(0)) ? (Byte) (byte) string.charAt(0) : null;
    }

    private String byteToPrintableAscii(Byte b) {
        char c = (char) (byte) b;
        return isAsciiPrintable(c) ? Character.toString(c) : NON_PRINTABLE_CHARACTER_PLACEHOLDER;
    }

    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    private String byteToHex(Byte b) {
        return String.format("%02X", 0xFF & (int) (byte) b);
    }
}
