package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.acr122.impl.RichByte;
import pl.marcinchwedczuk.rfid.card.commons.utils.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichByteTest {
    @Test
    public void can_convert_from_and_to_byte() {
        byte[] testValues = new byte[]{
                0, 1, (byte) 0xFF, (byte) 0xF0, 0x0F, (byte) 0xAC, 0x47
        };

        for (byte b : testValues) {
            RichByte richByte = new RichByte(b);
            byte returned = richByte.asByte();

            assertEquals(b, returned);
        }
    }

    @Test
    public void can_set_bits() {
        RichByte b = new RichByte(0);

        bitsEqual(1, b.withBit(0, true));
        bitsEqual(8, b.withBit(3, true));
        bitsEqual(128, b.withBit(7, true));

        RichByte result = b.withBitSet(0)
                .withBitSet(1)
                .withBitSet(2)
                .withBitSet(3)
                .withBitSet(4)
                .withBitSet(5)
                .withBitSet(6)
                .withBitSet(7);
        bitsEqual(0xFF, result);
    }

    @Test
    public void can_clear_bits() {
        RichByte b = new RichByte(0xFF);

        bitsEqual(0xFE, b.withBitCleared(0));
        bitsEqual(0xEF, b.withBitCleared(4));

        RichByte result = b.withBitCleared(0)
                .withBitCleared(1)
                .withBitCleared(2)
                .withBitCleared(3)
                .withBitCleared(4)
                .withBitCleared(5)
                .withBitCleared(6)
                .withBitCleared(7);

        bitsEqual(0, result);
    }

    private void bitsEqual(int expected, RichByte actual) {
        assertEquals(
                StringUtils.to8BitsString((byte) expected),
                StringUtils.to8BitsString(actual.asByte()));
    }
}