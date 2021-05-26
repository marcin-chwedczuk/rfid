package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.acr122.AccessBits;
import pl.marcinchwedczuk.rfid.card.acr122.TrailerBlock;

import static org.junit.jupiter.api.Assertions.*;

class TrailerBlockTest {
    private static final byte FF = (byte)0xFF;
    private static final byte[] SECTOR_TRAILER = {
        // key A
        FF, FF, FF, FF, FF, FF,

        // Access bits
        FF, 0x07, (byte)0x80, 0x69,

        // key B
        0x12, FF, FF, FF, FF, 0x12,
    };

    @Test
    void readAccessFromBytes() {
        TrailerBlock tb = new TrailerBlock(SECTOR_TRAILER.clone());

        assertArrayEquals(new byte[] { FF, FF, FF, FF, FF, FF }, tb.keyA);
        assertArrayEquals(new byte[] { 0x12, FF, FF, FF, FF, 0x12 }, tb.keyB);

        AccessBits ab = tb.accessBits;

        assertEquals("0, 0, 1", ab.sectorTrailerAccess.cbits);
        assertEquals("0, 0, 0", ab.dataBlockAccesses.get(0).cbits);
        assertEquals("0, 0, 0", ab.dataBlockAccesses.get(1).cbits);
        assertEquals("0, 0, 0", ab.dataBlockAccesses.get(2).cbits);
    }

    @Test
    void saveAccessToBytes() {
        TrailerBlock tb = new TrailerBlock(SECTOR_TRAILER.clone());
        byte[] actual = tb.toBytes();

        // Validate can be parsed again
        new TrailerBlock(actual);

        assertArrayEquals(SECTOR_TRAILER, actual);
    }
}