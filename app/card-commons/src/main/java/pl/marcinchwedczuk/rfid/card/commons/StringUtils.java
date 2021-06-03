package pl.marcinchwedczuk.rfid.card.commons;

public class StringUtils {
    private StringUtils() { }

    public static String takeLast(int nCharacters, String s) {
        if (s.length() < nCharacters) {
            return s;
        }

        return s.substring(s.length() - nCharacters);
    }

    public static String to8BitsString(byte b) {
        return StringUtils.takeLast(8,
                "00000000" + Integer.toBinaryString((int) b & 0xFF));
    }
}
