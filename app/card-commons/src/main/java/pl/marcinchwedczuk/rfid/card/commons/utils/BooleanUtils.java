package pl.marcinchwedczuk.rfid.card.commons.utils;

public class BooleanUtils {
    private BooleanUtils() { }

    public static boolean[] zeroOneStringToArray(String s) {
        boolean[] result = new boolean[s.length()];

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '0' && s.charAt(i) != '1') {
                throw new IllegalArgumentException("s");
            }

            result[i] = (s.charAt(i) == '1');
        }

        return result;
    }

    public static String arrayToZeroOneString(boolean[] arr) {
        StringBuilder sb = new StringBuilder(arr.length);
        for (boolean b: arr) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }
}
