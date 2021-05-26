package pl.marcinchwedczuk.rfid.acr122;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ByteUtils {
    public static final String DEFAULT_SEPARATOR = ":";

    public static byte[] fromHexString(String s) throws NumberFormatException {
        return fromHexString(s, DEFAULT_SEPARATOR);
    }

    public static byte[] fromHexString(String s, String separator) throws NumberFormatException {
        // Remove separators
        String withoutSeparator = s.replaceAll(Pattern.quote(separator), "");

        if ((withoutSeparator.length() % 2) != 0) {
            throw new NumberFormatException("Cannot parse hex string to array of bytes.");
        }

        List<Byte> bytes = new ArrayList<>();

        for (int i = 0; i <= withoutSeparator.length() - 2; i += 2) {
            byte b = (byte)Integer.parseInt(withoutSeparator.substring(i, i + 2), 16);
            bytes.add(b);
        }

        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    public static String asHexString(byte[] bytes) {
        return asHexString(bytes, DEFAULT_SEPARATOR);
    }

    public static String asHexString(byte[] bytes, String separator) {
        if (bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(
                // XX<separator>XX<separator>XX
                bytes.length*2 + (bytes.length - 1) * separator.length()
        );

        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }

            String hex = Integer.toHexString(0xFF & bytes[i]).toUpperCase();
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }

        return sb.toString();
    }
}
