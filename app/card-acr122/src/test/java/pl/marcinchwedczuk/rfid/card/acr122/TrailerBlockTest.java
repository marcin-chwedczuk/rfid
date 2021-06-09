package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.AccessBits;
import pl.marcinchwedczuk.rfid.card.commons.DataBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.TrailerBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrailerBlockTest {
    byte[] SECTOR_TRAILER = ByteArrays.of(
            // key A
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            // Access bits
            0xFF, 0x07, 0x80, 0x00,
            // key B
            0x12, 0xFF, 0xFF, 0xFF, 0xFF, 0x12
    );

    @Test
    void parses_sector_trailer_bytes() {
        TrailerBlock tb = new TrailerBlock(SECTOR_TRAILER.clone());

        assertArrayEquals(ByteArrays.fromHexString("FF FF FF FF FF FF"), tb.keyA);
        assertArrayEquals(ByteArrays.fromHexString("12 FF FF FF FF 12"), tb.keyB);

        AccessBits ab = tb.accessBits;

        assertEquals(DataBlockAccess.C000, ab.dataBlockAccessForBlock(0));
        assertEquals(DataBlockAccess.C000, ab.dataBlockAccessForBlock(1));
        assertEquals(DataBlockAccess.C000, ab.dataBlockAccessForBlock(2));
        assertEquals(TrailerBlockAccess.C001, ab.trailerBlockAccess());
    }

    @Test
    void round_trip_works() {
        TrailerBlock tb = new TrailerBlock(SECTOR_TRAILER);
        byte[] actual = tb.toBytes();

        // Validate can be parsed again
        new TrailerBlock(actual);

        assertThat(ByteArrays.toHexString(SECTOR_TRAILER))
                .isEqualTo(ByteArrays.toHexString(actual));
    }
}