package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.BooleanUtils;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static pl.marcinchwedczuk.rfid.card.commons.AccessLevel.*;

public enum DataBlockAccess {
    // @formatter:off
    C000(KEY_A_OR_B,    KEY_A_OR_B,    KEY_A_OR_B, KEY_A_OR_B),
    C010(KEY_A_OR_B,    NEVER,         NEVER,      NEVER),
    C100(KEY_A_OR_B,    KEY_B,         NEVER,      NEVER),
    C110(KEY_A_OR_B,    KEY_B,         KEY_B,      KEY_A_OR_B),
    C001(KEY_A_OR_B,    NEVER,         NEVER,      KEY_A_OR_B),
    C011(KEY_B,         KEY_B,         NEVER,      NEVER),
    C101(KEY_B,         NEVER,         NEVER,      NEVER),
    C111(NEVER,         NEVER,         NEVER,      NEVER);
    // @formatter:on

    public final AccessLevel readAccess;
    public final AccessLevel writeAccess;

    public final AccessLevel incrementAccess;
    /**
     * Decrement, transfer and restore operations.
     */
    public final AccessLevel otherOperationsAccess;

    DataBlockAccess(AccessLevel readAccess,
                    AccessLevel writeAccess,
                    AccessLevel incrementAccess,
                    AccessLevel otherOperationsAccess) {
        this.readAccess = readAccess;
        this.writeAccess = writeAccess;
        this.incrementAccess = incrementAccess;
        this.otherOperationsAccess = otherOperationsAccess;
    }

    boolean[] toBits() {
        return BooleanUtils.zeroOneStringToArray(this.name().substring(1));
    }

    static DataBlockAccess fromBits(boolean[] bits) {
        if (bits.length != 3) {
            throw new IllegalArgumentException("bits");
        }

        String name = "C" + BooleanUtils.arrayToZeroOneString(bits);
        return DataBlockAccess.valueOf(name);
    }
}
