package pl.marcinchwedczuk.rfid.card.commons;

public class ByteArrays {
    private ByteArrays() {}

    public static final byte[] EMPTY = new byte[] { };

    public static byte[] fromHexString(String s) {
        String sDigits = s.replaceAll("\\s+", "");
        if (sDigits.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string: " + s);
        }

        byte[] bytes = new byte[sDigits.length() / 2];

        for (int i = 0; i < sDigits.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(sDigits.substring(i, i + 2), 16);
        }

        return bytes;
    }

    public static String toHexString(byte[] arr) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                builder.append(" ");
            }

            String tmp = Integer
                    .toHexString(Byte.toUnsignedInt(arr[i]))
                    .toUpperCase();
            builder.append(tmp.length() == 1 ? "0" + tmp : tmp);
        }

        return builder.toString();
    }
}
