package pl.marcinchwedczuk.rfid.card.commons;

public enum Register {
    REGISTER_0(0),
    REGISTER_1(1);

    private final int index;

    Register(int index) {
        this.index = index;
    }

    public int index() { return index; }
}
