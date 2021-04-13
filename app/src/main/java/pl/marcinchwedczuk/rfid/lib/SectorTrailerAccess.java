package pl.marcinchwedczuk.rfid.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.marcinchwedczuk.rfid.lib.AccessCond.*;

/**
 * Readable keys cannot be used for authentication.
 * This means if keyB is readable then it cannot be used for authentication purposes,
 * instead you may use keyB to store extra 6 bytes of data.
 *
 * See docs/mifare_classic.pdf, page 12 for details.
 */
public class SectorTrailerAccess {
    public final String cbits;

    /**
     * This is always {@code NEVER}.
     */
    public final AccessCond readKeyA;
    public final AccessCond writeKeyA;

    public final AccessCond accessBitsRead;
    public final AccessCond accessBitsWrite;

    public final AccessCond readKeyB;
    public final AccessCond writeKeyB;

    private SectorTrailerAccess(String cbits,
                               AccessCond readKeyA, AccessCond writeKeyA,
                               AccessCond accessBitsRead, AccessCond accessBitsWrite,
                               AccessCond readKeyB, AccessCond writeKeyB) {
        this.cbits = cbits;
        this.readKeyA = readKeyA;
        this.writeKeyA = writeKeyA;
        this.accessBitsRead = accessBitsRead;
        this.accessBitsWrite = accessBitsWrite;
        this.readKeyB = readKeyB;
        this.writeKeyB = writeKeyB;
    }

    public String asTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("+------------------------------------------------+").append('\n');
        sb.append("|    KEY A     |   ACCESS BITS    |    KEY B     |").append('\n');
        sb.append("| READ | WRITE |   READ | WRITE   | READ | WRITE |").append('\n');
        sb.append("| "  + toString(readKeyA) + " | " + toString(writeKeyA) +
                "  |   "  + toString(accessBitsRead) +  " | " + toString(accessBitsWrite) +
                "    | " + toString(readKeyB) + " | " + toString(writeKeyB) + "  |").append('\n');
        sb.append("+------------------------------------------------+").append('\n');
        return sb.toString();
    }

    private String toString(AccessCond accessCond) {
        switch (accessCond) {
            case NEVER: return "----";
            case KEY_A: return "AAAA";
            case KEY_B: return "BBBB";
            case KEY_A_OR_B: return "ABAB";
        }

        return "????";
    }

    private static Map<String, SectorTrailerAccess> accessMap = createMap(
        // |  CBITS   |    KEY A     |   ACCESS BITS    |    KEY B     |
        // | C1 C2 C3 | READ | WRITE |   READ | WRITE   | READ | WRITE |
        new SectorTrailerAccess(cbits(0, 0, 0), NEVER, KEY_A, KEY_A, NEVER, KEY_A, KEY_A),
        new SectorTrailerAccess(cbits(0, 1, 0), NEVER, NEVER, KEY_A, NEVER, KEY_A, NEVER),
        new SectorTrailerAccess(cbits(1, 0, 0), NEVER, KEY_B, KEY_A_OR_B, NEVER, NEVER, KEY_B),
        new SectorTrailerAccess(cbits(1, 1, 0), NEVER, NEVER, KEY_A_OR_B, NEVER, NEVER, NEVER),
        // transport configuration
        new SectorTrailerAccess(cbits(0, 0, 1), NEVER, KEY_A, KEY_A, KEY_A, KEY_A, KEY_A),
        new SectorTrailerAccess(cbits(0, 1, 1), NEVER, KEY_B, KEY_A_OR_B, KEY_B, NEVER, KEY_B),
        new SectorTrailerAccess(cbits(1, 0, 1), NEVER, NEVER, KEY_A_OR_B, KEY_B, NEVER, NEVER),
        new SectorTrailerAccess(cbits(1, 1, 1), NEVER, NEVER, KEY_A_OR_B, NEVER, NEVER, NEVER));

    private static Map<String, SectorTrailerAccess> createMap(SectorTrailerAccess... accesses) {
        Map<String, SectorTrailerAccess> map = new HashMap<>();
        for (SectorTrailerAccess dba: accesses) {
            map.put(dba.cbits, dba);
        }
        return map;
    }

    private static String cbits(int c1, int c2, int c3) {
        return String.format("%d, %d, %d", c1, c2, c3);
    }

    public static List<String> validCBits() {
        return new ArrayList<>(accessMap.keySet());
    }

    public static SectorTrailerAccess fromCBits(int c1, int c2, int c3) {
        SectorTrailerAccess dataBlockAccess = accessMap.get(cbits(c1, c2, c3));
        if (dataBlockAccess == null) {
            throw new IllegalArgumentException("Invalid C bits value: " + cbits(c1, c2, c3));
        }
        return dataBlockAccess;
    }
}