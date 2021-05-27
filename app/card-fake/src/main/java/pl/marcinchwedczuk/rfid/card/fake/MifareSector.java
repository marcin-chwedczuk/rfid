package pl.marcinchwedczuk.rfid.card.fake;

import java.util.Arrays;

public class MifareSector {
    // Last block is "Sector Trailer"
    private byte[][] blocks = new byte[4][16];

    private byte[] getSectorTrailer() {
        return blocks[3];
    }

    private byte[] getKeyA() {
        return Arrays.copyOfRange(getSectorTrailer(), 0, 6);
    }

    private byte[] getKeyB() {
        return Arrays.copyOfRange(getSectorTrailer(), 10, 16);
    }
}
