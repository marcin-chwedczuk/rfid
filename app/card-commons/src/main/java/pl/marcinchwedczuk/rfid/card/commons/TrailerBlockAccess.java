package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.BooleanUtils;

import static pl.marcinchwedczuk.rfid.card.commons.AccessLevel.*;

public enum TrailerBlockAccess {
    // @formatter:off
    C000(NEVER, KEY_A, KEY_A,       NEVER, KEY_A, KEY_A),
    C010(NEVER, NEVER, KEY_A,       NEVER, KEY_A, NEVER),
    C100(NEVER, KEY_B, KEY_A_OR_B,  NEVER, NEVER, KEY_B),
    C110(NEVER, NEVER, KEY_A_OR_B,  NEVER, NEVER, NEVER),
    C001(NEVER, KEY_A, KEY_A,       KEY_A, KEY_A, KEY_A),
    C011(NEVER, KEY_B, KEY_A_OR_B,  KEY_B, NEVER, KEY_B),
    C101(NEVER, NEVER, KEY_A_OR_B,  KEY_B, NEVER, NEVER),
    C111(NEVER, NEVER, KEY_A_OR_B,  NEVER, NEVER, NEVER);
    // @formatter:on

    public final AccessLevel readAccessToKeyA;
    public final AccessLevel writeAccessToKeyA;

    public final AccessLevel readAccessToAccessBits;
    public final AccessLevel writeAccessToAccessBits;

    public final AccessLevel readAccessToKeyB;
    public final AccessLevel writeAccessToKeyB;

    TrailerBlockAccess(AccessLevel readAccessToKeyA,
                       AccessLevel writeAccessToKeyA,
                       AccessLevel readAccessToAccessBits,
                       AccessLevel writeAccessAccessBits,
                       AccessLevel readAccessToKeyB,
                       AccessLevel writeAccessKeyB) {
        this.readAccessToKeyA = readAccessToKeyA;
        this.writeAccessToKeyA = writeAccessToKeyA;
        this.readAccessToAccessBits = readAccessToAccessBits;
        this.writeAccessToAccessBits = writeAccessAccessBits;
        this.readAccessToKeyB = readAccessToKeyB;
        this.writeAccessToKeyB = writeAccessKeyB;
    }

    boolean[] toBits() {
        return BooleanUtils.zeroOneStringToArray(this.name().substring(1));
    }

    static TrailerBlockAccess fromBits(boolean[] bits) {
        if (bits.length != 3) {
            throw new IllegalArgumentException("bits");
        }

        String name = "C" + BooleanUtils.arrayToZeroOneString(bits);
        return TrailerBlockAccess.valueOf(name);
    }
}
