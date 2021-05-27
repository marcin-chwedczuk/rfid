package pl.marcinchwedczuk.rfid.card.fake;

class StringUtils {
    private StringUtils() {}

    public static byte[] byteArrayFromHexString(String s) {
        String sDigits = s.replaceAll("\\s+", "");
        if (sDigits.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string: " + s);
        }

        byte[] bytes = new byte[sDigits.length() / 2];

        for (int i = 0; i < sDigits.length(); i += 2) {
            bytes[i / 2] = (byte)Integer.parseInt(sDigits.substring(i, i + 2), 16);
        }

        return bytes;
    }

    public static String byteArrayToHexString(byte[] arr) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                builder.append(" ");
            }

            builder.append(Integer.toString(Byte.toUnsignedInt(arr[i]), 16));
        }

        return builder.toString();
    }
}
