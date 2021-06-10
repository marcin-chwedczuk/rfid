package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;
import java.util.BitSet;

public class AccessBitsParser {
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
            accessBits.dataBlockAccessForBlock(2).toBits(),
            accessBits.trailerBlockAccess().toBits()
        };

        BitSet sectorTrailer = new BitSet();

        Mifare1K.forEachAccessBit((byteIndex, bitIndex, isNegated, forBlock, cPosition) -> {
            boolean value = (map[forBlock][cPosition - 1] == '1');
            if (isNegated) {
                value = !value;
            }

            sectorTrailer.set(byteIndex*8 + bitIndex, value);
        });

        // BitSet is too smart (it grows and shrinks the storage space automatically),
        // but there is no byte[] backed implementation in the standard library.
        // TODO: Refactor to get rid of this crap.
        return ByteArrays.ensureLength(sectorTrailer.toByteArray(), 16);
    }
}
