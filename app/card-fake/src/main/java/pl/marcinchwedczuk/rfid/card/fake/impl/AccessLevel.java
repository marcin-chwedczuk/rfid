package pl.marcinchwedczuk.rfid.card.fake.impl;

enum AccessLevel {
    KEY_A,
    KEY_B,
    KEY_A_OR_B,
    NEVER;

    public boolean allowedUsingKey(KeyType keyType) {
        switch (this) {
            case KEY_A:
                return (keyType == KeyType.KEY_A);
            case KEY_B:
                return (keyType == KeyType.KEY_B);
            case KEY_A_OR_B:
                return true;
            case NEVER:
                return false;
            default:
                throw new AssertionError();
        }
    }
}