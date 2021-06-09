package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;
import java.util.BitSet;

public class AccessBitsParser {
    /**
     * @param bytes Trailer sector bytes. Should be `byte[16]` array.
     * @return Returns array which at index `i` contains access bits for sector `i`.
     * For example array may contain value `"010"` at index `1`.
     * That means that we have access control bits for sector `1`:
     * `C1_1 ... C3_1` set to values `0`, `1` and `0`.
     */
    public AccessBits parse(byte[] sectorTrailerBytes) {
        char[][] map = new char[4][3];
        for (char[] chars : map) {
            Arrays.fill(chars, '?');
        }

        BitSet sectorTrailer = BitSet.valueOf(sectorTrailerBytes);

        Mifare1K.forEachAccessBit((byteIndex, bitIndex, isNegated, forBlock, cPosition) -> {
            boolean bitValue = sectorTrailer.get(byteIndex*8 + bitIndex);
            if (isNegated) {
                bitValue = !bitValue;
            }

            // cPosition is 1 based
            map[forBlock][cPosition - 1] = bitValue ? '1' : '0';
        });

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

        BitSet sectorTrailer = new BitSet(16*8);

        Mifare1K.forEachAccessBit((byteIndex, bitIndex, isNegated, forBlock, cPosition) -> {
            boolean value = (map[forBlock][cPosition - 1] == '1');
            if (isNegated) {
                value = !value;
            }

            sectorTrailer.set(byteIndex*8 + bitIndex, value);
        });

        return sectorTrailer.toByteArray();
    }
}
