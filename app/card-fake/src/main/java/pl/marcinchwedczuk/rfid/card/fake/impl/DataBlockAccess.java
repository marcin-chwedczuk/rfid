package pl.marcinchwedczuk.rfid.card.fake.impl;

import static pl.marcinchwedczuk.rfid.card.fake.impl.AccessLevel.*;

enum DataBlockAccess {
    C000(KEY_A_OR_B, KEY_A_OR_B),
    C010(KEY_A_OR_B, NEVER),
    C100(KEY_A_OR_B, KEY_B),
    C110(KEY_A_OR_B, KEY_B),
    C001(KEY_A_OR_B, NEVER),
    C011(KEY_B,      KEY_B),
    C101(KEY_B,      NEVER),
    C111(NEVER,      NEVER);

    public final AccessLevel readAccess;
    public final AccessLevel writeAccess;

    DataBlockAccess(AccessLevel readAccess, AccessLevel writeAccess) {
        this.readAccess = readAccess;
        this.writeAccess = writeAccess;
    }


    public static DataBlockAccess fromBits(String bits) {
        return DataBlockAccess.valueOf("C" + bits);
    }
}
