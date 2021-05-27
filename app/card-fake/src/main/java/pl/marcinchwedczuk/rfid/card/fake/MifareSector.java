package pl.marcinchwedczuk.rfid.card.fake;

import java.util.Arrays;

public class MifareSector {
    // Last block is "Sector Trailer"
    private byte[][] blocks = new byte[4][16];

    public MifareSector() {
        // TODO: Set transport configuration

        // Default sector trailer
        blocks[3] = StringUtils.byteArrayFromHexString(
                "FF FF FF FF FF FF FF 07 80 69 FF FF FF FF FF FF");
    }

    private byte[] getSectorTrailer() {
        return blocks[3];
    }

    private byte[] getKeyA() {
        return Arrays.copyOfRange(getSectorTrailer(), 0, 6);
    }

    private byte[] getKeyB() {
        return Arrays.copyOfRange(getSectorTrailer(), 10, 16);
    }

    private void parseAccessBits() {

    }

    public boolean authenticate(boolean keyA, byte[] key) {
        // TODO: Check can authenticate
        byte[] internalKey = keyA ? getKeyA() : getKeyB();
        return Arrays.equals(internalKey, key);
    }

    public byte[] read(int blockIndex, byte nBytes, boolean keyA) {
        // TODO: Check can read
        return Arrays.copyOf(blocks[blockIndex], nBytes);
    }
}
