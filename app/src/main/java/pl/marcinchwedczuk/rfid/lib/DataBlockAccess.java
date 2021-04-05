package pl.marcinchwedczuk.rfid.lib;

import java.util.HashMap;
import java.util.Map;

import static pl.marcinchwedczuk.rfid.lib.AccessCond.*;

public class DataBlockAccess {
    public final AccessCond read;
    public final AccessCond write;
    public final AccessCond increment;

    /**
     * Access condition for Decrement, Transfer, Resetore operations.
     */
    public final AccessCond other;

    private DataBlockAccess(AccessCond read, AccessCond write, AccessCond increment, AccessCond other) {
        this.read = read;
        this.write = write;
        this.increment = increment;
        this.other = other;
    }

    public String asTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("+--------------------------------------+").append('\n');
        sb.append("|  READ  |  WRITE  | INCREMENT | OTHER |").append('\n');
        sb.append("|  " + toString(read) + "  |  " + toString(write) +
                "   |    " + toString(increment) + "   |  "  + toString(other) + " |").append('\n');
        sb.append("+--------------------------------------+").append('\n');
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

    private static Map<Integer, DataBlockAccess> accessMap = new HashMap<>() {{
        // READ | WRITE | INCREMENT | OTHER

        // transport (think new card) configuration
        put(cbits(0, 0, 0), new DataBlockAccess(KEY_A_OR_B, KEY_A_OR_B, KEY_A_OR_B, KEY_A_OR_B));

        put(cbits(0, 1, 0), new DataBlockAccess(KEY_A_OR_B, NEVER, NEVER, NEVER));
        put(cbits(1, 0, 0), new DataBlockAccess(KEY_A_OR_B, KEY_B, NEVER, NEVER));
        put(cbits(1, 1, 0), new DataBlockAccess(KEY_A_OR_B, KEY_B, KEY_B, KEY_A_OR_B));
        put(cbits(0, 0, 1), new DataBlockAccess(KEY_A_OR_B, NEVER, NEVER, KEY_A_OR_B));
        put(cbits(0, 1, 1), new DataBlockAccess(KEY_B, KEY_B, NEVER, NEVER));
        put(cbits(1, 0, 1), new DataBlockAccess(KEY_B, NEVER, NEVER, NEVER));
        put(cbits(1, 1, 1), new DataBlockAccess(NEVER, NEVER, NEVER, NEVER));
    }};

    private static int cbits(int c1, int c2, int c3) {
        return c1*4 + c2*2 + c3;
    }

    public static DataBlockAccess fromBits(int c1, int c2, int c3) {
        DataBlockAccess dataBlockAccess = accessMap.get(cbits(c1, c2, c3));
        if (dataBlockAccess == null) {
            throw new IllegalArgumentException("Invalid C bits value: " + cbits(c1, c2, c3));
        }
        return dataBlockAccess;
    }
}
