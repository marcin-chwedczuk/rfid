package pl.marcinchwedczuk.rfid.card.fake.impl;

public enum CardPresence {
    CARD_PRESENT(true),
    CARD_ABSENT(false);

    boolean isCardPresent;

    CardPresence(boolean isCardPresent) {
        this.isCardPresent = isCardPresent;
    }

    public boolean isCardPresent() {
        return isCardPresent;
    }
}
