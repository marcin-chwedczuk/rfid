package pl.marcinchwedczuk.rfid.card.acr122;

/**
 * Duration expressed as number of 100ms units,
 * for example `new TDuration(5)` is 0.5 seconds or 500ms.
 */
public class TDuration {
    public static final TDuration ZERO = new TDuration(0);

    public static TDuration fromNumberOf100msUnits(int nUnits) {
        return new TDuration(nUnits);
    }

    private final int nUnits;

    TDuration(int nUnits) {
        if (nUnits < 0 || nUnits > 0xFF)
            throw new IllegalArgumentException("nUnits");

        this.nUnits = nUnits;
    }

    public int units() {
        return nUnits;
    }

    @Override
    public String toString() {
        return String.format("TDuration(100ms*%d)", nUnits);
    }
}
