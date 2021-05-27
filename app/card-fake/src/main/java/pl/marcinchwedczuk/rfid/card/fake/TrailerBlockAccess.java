package pl.marcinchwedczuk.rfid.card.fake;

import static pl.marcinchwedczuk.rfid.card.fake.AccessLevel.*;

public enum TrailerBlockAccess {
    C000(NEVER, KEY_A, KEY_A, NEVER, KEY_A, KEY_A),
    C010(NEVER, NEVER, KEY_A, NEVER, KEY_A, NEVER),
    C100(NEVER, KEY_B, KEY_A_OR_B, NEVER, NEVER, KEY_B),
    C110(NEVER, NEVER, KEY_A_OR_B, NEVER, NEVER, NEVER),
    C001(NEVER, KEY_A, KEY_A, KEY_A, KEY_A, KEY_A),
    C011(NEVER, KEY_B, KEY_A_OR_B, KEY_B, NEVER, KEY_B),
    C101(NEVER, NEVER, KEY_A_OR_B, KEY_B, NEVER, NEVER),
    C111(NEVER, NEVER, KEY_A_OR_B, NEVER, NEVER, NEVER);

    public final AccessLevel readAccessKeyA;
    public final AccessLevel writeAccessKeyA;

    public final AccessLevel readAccessAccessBits;
    public final AccessLevel writeAccessAccessBits;

    public final AccessLevel readAccessKeyB;
    public final AccessLevel writeAccessKeyB;


    TrailerBlockAccess(AccessLevel readAccessKeyA,
                       AccessLevel writeAccessKeyA,
                       AccessLevel readAccessAccessBits,
                       AccessLevel writeAccessAccessBits,
                       AccessLevel readAccessKeyB,
                       AccessLevel writeAccessKeyB) {
        this.readAccessKeyA = readAccessKeyA;
        this.writeAccessKeyA = writeAccessKeyA;
        this.readAccessAccessBits = readAccessAccessBits;
        this.writeAccessAccessBits = writeAccessAccessBits;
        this.readAccessKeyB = readAccessKeyB;
        this.writeAccessKeyB = writeAccessKeyB;
    }

    public static TrailerBlockAccess fromBits(String bits) {
        return TrailerBlockAccess.valueOf("C" + bits);
    }
}
