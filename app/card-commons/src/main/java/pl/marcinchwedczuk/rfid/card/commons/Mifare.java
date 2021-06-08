package pl.marcinchwedczuk.rfid.card.commons;

public class Mifare {
    private Mifare() { }

    /**
     * Capital letter 'C' - means negation of bit.
     */
    public static final String ACCESS_BITS_POSITIONS =
            "C23 C22 C21 C20 C13 C12 C11 C10 " +
            "c13 c12 c11 c10 C33 C32 C31 C30 " +
            "c33 c32 c31 c30 c23 c22 c21 c20";
}
