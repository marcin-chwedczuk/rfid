package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.CardException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class AccessBitsValidator {

    private final byte[] trailerSector;

    public AccessBitsValidator(byte[] trailerSector) {
        this.trailerSector = Objects.requireNonNull(trailerSector);
    }

    public void throwIfInvalid() throws CardException {
        Map<String, Boolean> bitValues = new HashMap<>();
        String[] bitsNames = FakeMifare1K.ACCESS_BITS_POSITIONS.split("\\s+");

        int byteIndex = 6;
        int bitIndex = 7;
        for (String bitName : bitsNames) {
            boolean bit = getBit(byteIndex, bitIndex);

            if (bitName.startsWith("C")) {
                bit = !bit;
            }

            if (bitValues.containsKey(bitName)) {
                boolean expected = bitValues.get(bitName);
                if (expected != bit) {
                    throw new CardException(
                            "Sector trailer's access bits contain invalid pattern on bit: " + bitName);
                }
            } else {
                bitValues.put(bitName, bit);
            }

            if (bitIndex == 0) {
                byteIndex++;
                bitIndex = 7;
            } else {
                bitIndex--;
            }
        }
    }

    private boolean getBit(int byteIndex, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("bitIndex");

        byte byte_ = trailerSector[byteIndex];
        boolean bit = (byte_ & (1 << bitIndex)) != 0;
        return bit;
    }
}
