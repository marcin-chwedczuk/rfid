package pl.marcinchwedczuk.rfid.card.commons;

import java.util.Arrays;

public class AccessBitsParser {
    /**
     * @param bytes Trailer sector bytes. Should be `byte[16]` array.
     * @return Returns array which at index `i` contains access bits for sector `i`.
     * For example array may contain value `"010"` at index `1`.
     * That means that we have access control bits for sector `1`:
     * `C1_1 ... C3_1` set to values `0`, `1` and `0`.
     */
    public AccessBits parse(byte[] bytes) {
        char[][] map = new char[4][3];

        for (char[] chars : map) {
            Arrays.fill(chars, '?');
        }

        String[] bitsNames = Mifare.ACCESS_BITS_POSITIONS.split("\\s+");

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

        return new AccessBits(
            DataBlockAccess.fromBits(map[0]),
            DataBlockAccess.fromBits(map[1]),
            DataBlockAccess.fromBits(map[2]),
            TrailerBlockAccess.fromBits(map[3]));
    }

    public byte[] unparse(AccessBits accessBits) {
        char[][] map = new char[][] {
            accessBits.dataBlockAccessForBlock(0).toBits(),
            accessBits.dataBlockAccessForBlock(1).toBits(),
            accessBits.dataBlockAccessForBlock(1).toBits(),
            accessBits.trailerBlockAccess().toBits()
        };

        byte[] sectorTrailer = new byte[16];

        String[] bitsNames = Mifare.ACCESS_BITS_POSITIONS.split("\\s+");

        int byteIndex = 6;
        int bitIndex = 7;
        for (String bitName : bitsNames) {
            int bitPosition = charValue(bitName.charAt(1)) - 1; // Make it zero-based
            int blockIndex = charValue(bitName.charAt(2));

            // TODO: char[] to boolean[]
            boolean value = (map[blockIndex][bitPosition] == '1');
            if (bitName.startsWith("C")) {
                value = !value;
            }

            setBit(sectorTrailer, byteIndex, bitIndex, value);

            if (bitIndex == 0) {
                byteIndex++;
                bitIndex = 7;
            } else {
                bitIndex--;
            }
        }

        return sectorTrailer;
    }

    private boolean getBit(byte[] trailerSector, int byteIndex, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("bitIndex");

        byte byte_ = trailerSector[byteIndex];
        boolean bit = (byte_ & (1 << bitIndex)) != 0;
        return bit;
    }

    private void setBit(byte[] sectorTrailer, int byteIndex, int bitIndex, boolean value) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("bitIndex");

        byte byte_ = sectorTrailer[byteIndex];

        byte bit = (byte)(1 << bitIndex);
        byte_ = (byte)( (byte_ & ~bit) | (value ? bit : 0) );

        sectorTrailer[byteIndex] = byte_;
    }

    private int charValue(char c) {
        return Integer.parseInt(Character.toString(c));
    }
}
