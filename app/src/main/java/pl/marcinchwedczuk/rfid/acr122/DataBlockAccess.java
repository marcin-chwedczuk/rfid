package pl.marcinchwedczuk.rfid.acr122;

import java.util.*;
import java.util.regex.Pattern;

import static pl.marcinchwedczuk.rfid.acr122.AccessCond.*;

public class DataBlockAccess {
    public final String cbits;
    public final AccessCond read;
    public final AccessCond write;
    public final AccessCond increment;

    /**
     * Access condition for Decrement, Transfer, Resetore operations.
     */
    public final AccessCond other;

    private DataBlockAccess(String cbits,
                            AccessCond read, AccessCond write, AccessCond increment, AccessCond other) {
        this.cbits = cbits;
        this.read = read;
        this.write = write;
        this.increment = increment;
        this.other = other;
    }

    /**
     * @return Array { 0, c1, c2, c3 }
     */
    public int[] getCBits() {
        String cbitsWithFakeFirstElement = "0, " + cbits;
        return Arrays.stream(cbitsWithFakeFirstElement.split(Pattern.quote(", ")))
                .mapToInt(Integer::parseInt)
                .toArray();
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

    private static Map<String, DataBlockAccess> accessMap = createMap(
        //                  CBITS |         READ |      WRITE |     INCREMENT | OTHER
        new DataBlockAccess(cbits(0, 0, 0), KEY_A_OR_B, KEY_A_OR_B, KEY_A_OR_B, KEY_A_OR_B),
        new DataBlockAccess(cbits(0, 1, 0), KEY_A_OR_B, NEVER, NEVER, NEVER),
        new DataBlockAccess(cbits(1, 0, 0), KEY_A_OR_B, KEY_B, NEVER, NEVER),
        new DataBlockAccess(cbits(1, 1, 0), KEY_A_OR_B, KEY_B, KEY_B, KEY_A_OR_B),
        new DataBlockAccess(cbits(0, 0, 1), KEY_A_OR_B, NEVER, NEVER, KEY_A_OR_B),
        new DataBlockAccess(cbits(0, 1, 1), KEY_B, KEY_B, NEVER, NEVER),
        new DataBlockAccess(cbits(1, 0, 1), KEY_B, NEVER, NEVER, NEVER),
        new DataBlockAccess(cbits(1, 1, 1), NEVER, NEVER, NEVER, NEVER)
    );

    private static Map<String, DataBlockAccess> createMap(DataBlockAccess... accesses) {
        Map<String, DataBlockAccess> map = new HashMap<>();
        for (DataBlockAccess dba: accesses) {
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

    public static DataBlockAccess fromBits(int c1, int c2, int c3) {
        DataBlockAccess dataBlockAccess = accessMap.get(cbits(c1, c2, c3));
        if (dataBlockAccess == null) {
            throw new IllegalArgumentException("Invalid C bits value: " + cbits(c1, c2, c3));
        }
        return dataBlockAccess;
    }

    public static DataBlockAccess fromCBits(String cbits) {
        DataBlockAccess tmp = accessMap.get(cbits);
        if (tmp == null) {
            throw new IllegalArgumentException("cbits");
        }
        return tmp;
    }
}
