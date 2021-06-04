package pl.marcinchwedczuk.rfid.card.acr122.impl;

import pl.marcinchwedczuk.rfid.card.commons.utils.StringUtils;

public class RichByte {
    private final int b;

    public RichByte(int b) {
        this.b = (b & 0xFF);
    }

    public boolean isBitSet(int bitPosition) {
        checkBitPosition(bitPosition);

        return ((b & (1 << bitPosition)) != 0);
    }

    public RichByte withBit(int bitPosition, boolean value) {
        checkBitPosition(bitPosition);

        int newB = (b & ~(1 << bitPosition)) | (value ? (1 << bitPosition) : 0);
        return new RichByte(newB);
    }

    public RichByte withBitSet(int bitPosition) {
        return withBit(bitPosition, true);
    }

    public RichByte withBitCleared(int bitPosition) {
        return withBit(bitPosition, false);
    }

    public byte asByte() {
        return (byte) b;
    }

    @Override
    public String toString() {
        return StringUtils.takeLast(8,
                "00000000" + Integer.toBinaryString(b & 0xFF));
    }

    private static void checkBitPosition(int bitPosition) {
        if (bitPosition < 0 || bitPosition >= 8) {
            throw new IllegalArgumentException("bitPosition");
        }
    }
}
