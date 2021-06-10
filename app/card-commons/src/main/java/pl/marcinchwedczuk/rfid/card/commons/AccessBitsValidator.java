package pl.marcinchwedczuk.rfid.card.commons;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccessBitsValidator {
    private final byte[] sectorTrailerBytes;

    public AccessBitsValidator(byte[] sectorTrailerBytes) {
        this.sectorTrailerBytes = Objects.requireNonNull(sectorTrailerBytes);
    }

    public boolean isValid() {
        BitSet sectorTrailer = BitSet.valueOf(sectorTrailerBytes);
        AtomicBoolean isValid = new AtomicBoolean(true);

        Map<String, Boolean> bitValues = new HashMap<>();
        Mifare1K.forEachAccessBit((byteIndex, bitIndex, isNegated, forBlock, cPosition) -> {
            boolean bitValue = sectorTrailer.get(byteIndex*8 + bitIndex);
            if (isNegated) {
                bitValue = !bitValue;
            }

            String bitName = String.format("C%d_%d", cPosition, forBlock);

            if (bitValues.containsKey(bitName)) {
                boolean expected = bitValues.get(bitName);
                if (expected != bitValue) {
                    // There is only 3*8 bits to check, just iterate instead
                    // of using exception to break from the lambda.
                    isValid.set(false);
                }
            } else {
                bitValues.put(bitName, bitValue);
            }
        });

        return isValid.get();
    }
}
