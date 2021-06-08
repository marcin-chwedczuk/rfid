package pl.marcinchwedczuk.rfid.card.fake.impl;

import pl.marcinchwedczuk.rfid.card.commons.Mifare;
import pl.marcinchwedczuk.rfid.card.fake.impl.Mifare1KSimulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class AccessBitsValidator {

    private final byte[] trailerSector;

    public AccessBitsValidator(byte[] trailerSector) {
        this.trailerSector = Objects.requireNonNull(trailerSector);
    }

    public boolean isValid() {
        Map<String, Boolean> bitValues = new HashMap<>();
        String[] bitsNames = Mifare.ACCESS_BITS_POSITIONS.split("\\s+");

        int byteIndex = 6;
        int bitIndex = 7;
        for (String bitName : bitsNames) {
            boolean bit = getBit(byteIndex, bitIndex);

            if (bitName.startsWith("C")) {
                bit = !bit;
            }

            String normalizedBitName = bitName.toLowerCase();

            if (bitValues.containsKey(normalizedBitName)) {
                boolean expected = bitValues.get(normalizedBitName);
                if (expected != bit) {
                    return false;
                }
            } else {
                bitValues.put(normalizedBitName, bit);
            }

            if (bitIndex == 0) {
                byteIndex++;
                bitIndex = 7;
            } else {
                bitIndex--;
            }
        }

        return true;
    }

    private boolean getBit(int byteIndex, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("bitIndex");

        byte byte_ = trailerSector[byteIndex];
        boolean bit = (byte_ & (1 << bitIndex)) != 0;
        return bit;
    }
}
