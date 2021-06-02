package pl.marcinchwedczuk.rfid.card.fake.impl;

import pl.marcinchwedczuk.rfid.card.fake.impl.Mifare1KSimulator;

import java.util.Arrays;

class AccessBitsParser {
    /**
     * @param bytes Trailer sector bytes. Should be `byte[16]` array.
     * @return Returns array which at index `i` contains access bits for sector `i`.
     * For example array may contain value `"010"` at index `1`.
     * That means that we have access control bits for sector `1`:
     * `C1_1 ... C3_1` set to values `0`, `1` and `0`.
     */
    public String[] parse(byte[] bytes) {
        char[][] map = new char[4][3];

        for (char[] chars : map) {
            Arrays.fill(chars, '?');
        }

        String[] bitsNames = Mifare1KSimulator.ACCESS_BITS_POSITIONS.split("\\s+");

        int byteIndex = 6;
        int bitIndex = 7;
        for (String bitName : bitsNames) {
            if (bitName.startsWith("c")) {

                int bitPosition = charValue(bitName.charAt(1)) - 1; // Make it zero-based
                int blockIndex = charValue(bitName.charAt(2));

                map[blockIndex][bitPosition] = getBit(bytes, byteIndex, bitIndex) ? '1' : '0';
            }

            if (bitIndex == 0) {
                byteIndex++;
                bitIndex = 7;
            } else {
                bitIndex--;
            }
        }

        return Arrays.stream(map)
                .map(String::new)
                .toArray(String[]::new);
    }

    private boolean getBit(byte[] trailerSector, int byteIndex, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("bitIndex");

        byte byte_ = trailerSector[byteIndex];
        boolean bit = (byte_ & (1 << bitIndex)) != 0;
        return bit;
    }

    private int charValue(char c) {
        return Integer.parseInt(Character.toString(c));
    }
}
