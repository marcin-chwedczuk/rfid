package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

public class Mifare1K {
    private Mifare1K() { }

    public static byte[] defaultSectorTrailer() {
        return ByteArrays.fromHexString(
                "FF FF FF FF FF FF FF 07 80 69 FF FF FF FF FF FF");
    }

    /**
     * Capital letter 'C' - means negation of bit.
     * Format is:
     * C/c (C is for negation) [c bit position 0, 1 or 2][block number 0..3]
     *
     * See page 11 of `docs/mifare_classic.pdf` for details.
     */
    public static final String ACCESS_BITS_POSITIONS =
            "C23 C22 C21 C20 C13 C12 C11 C10 " +
            "c13 c12 c11 c10 C33 C32 C31 C30 " +
            "c33 c32 c31 c30 c23 c22 c21 c20";

    public static void forEachAccessBit(AccessBitsConsumer consumer) {
        String[] bitsNames = Mifare1K.ACCESS_BITS_POSITIONS.split("\\s+");

        int byteIndex = 6;
        int bitIndex = 7;
        for (String bitName : bitsNames) {
            int cPosition = digitValue(bitName.charAt(1));
            int block = digitValue(bitName.charAt(2));

            boolean isNegated = bitName.startsWith("C");

            consumer.accept(byteIndex, bitIndex, isNegated, block, cPosition);

            if (bitIndex == 0) {
                byteIndex++;
                bitIndex = 7;
            } else {
                bitIndex--;
            }
        }
    }

    private static int digitValue(char c) {
        return Character.digit(c, 10);
    }

    @FunctionalInterface
    public interface AccessBitsConsumer {
        void accept(int byteIndex, int bitIndex,
                    boolean isNegated,
                    int forBlock, int cPosition);
    }
}
