package pl.marcinchwedczuk.rfid.card.fake;

import static pl.marcinchwedczuk.rfid.card.fake.AccessLevel.*;

enum DataBlockAccess {
    C000 {
        @Override
        AccessLevel readAccess() {
            return KEY_A_OR_B;
        }

        @Override
        AccessLevel writeAccess() {
            return KEY_A_OR_B;
        }
    },
    C010 {
        @Override
        AccessLevel readAccess() {
            return KEY_A_OR_B;
        }

        @Override
        AccessLevel writeAccess() {
            return NEVER;
        }
    },
    C100 {
        @Override
        AccessLevel readAccess() {
            return KEY_A_OR_B;
        }

        @Override
        AccessLevel writeAccess() {
            return KEY_B;
        }
    },
    C110 {
        @Override
        AccessLevel readAccess() {
            return KEY_A_OR_B;
        }

        @Override
        AccessLevel writeAccess() {
            return KEY_B;
        }
    },
    C001 {
        @Override
        AccessLevel readAccess() {
            return KEY_A_OR_B;
        }

        @Override
        AccessLevel writeAccess() {
            return NEVER;
        }
    },
    C011 {
        @Override
        AccessLevel readAccess() {
            return KEY_B;
        }

        @Override
        AccessLevel writeAccess() {
            return KEY_B;
        }
    },
    C101 {
        @Override
        AccessLevel readAccess() {
            return KEY_B;
        }

        @Override
        AccessLevel writeAccess() {
            return NEVER;
        }
    },
    C111 {
        @Override
        AccessLevel readAccess() {
            return NEVER;
        }

        @Override
        AccessLevel writeAccess() {
            return NEVER;
        }
    };

    abstract AccessLevel readAccess();

    abstract AccessLevel writeAccess();


    public static DataBlockAccess fromBits(String bits) {
        return DataBlockAccess.valueOf("C" + bits);
    }
}
