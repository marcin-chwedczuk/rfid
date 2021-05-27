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

    public byte[] read(int blockIndex, byte nBytes, KeyType key) {
        String[] accessBits = new AccessBitsParser().parse(blocks[3]);

        if (blockIndex < 3) {
            // Normal block
            DataBlockAccess access = DataBlockAccess.fromBits(accessBits[blockIndex]);
            if (!access.readAccess().allowedUsingKey(key)) {
                return null;
            }

            return Arrays.copyOf(blocks[blockIndex], nBytes);
        }
        else {
            // Sector trailer
            TrailerBlockAccess access = TrailerBlockAccess.fromBits(accessBits[3]);
            byte[] trailerCopy = blocks[3].clone();

            if (!access.readAccessKeyA.allowedUsingKey(key)) {
                // Zero key A
                Arrays.fill(trailerCopy, 0, 6, (byte)0x00);
            }

            if (!access.readAccessAccessBits.allowedUsingKey(key)) {
                Arrays.fill(trailerCopy, 7, 10, (byte)0x00);
            }

            if (!access.readAccessKeyB.allowedUsingKey(key)) {
                // Zero key B
                Arrays.fill(trailerCopy, 10, 16, (byte)0x00);
            }

            return Arrays.copyOf(trailerCopy, nBytes);
        }
    }

    public boolean write(int blockIndex, byte[] blockData, KeyType key) {
        String[] accessBits = new AccessBitsParser().parse(blocks[3]);

        if (blockIndex < 3) {
            // Normal block
            DataBlockAccess access = DataBlockAccess.fromBits(accessBits[blockIndex]);
            if (!access.writeAccess().allowedUsingKey(key)) {
                return false;
            }

            System.arraycopy(blockData, 0, blocks[blockIndex], 0, blocks[blockIndex].length);
            return true;
        }
        else {
            // Sector trailer
            TrailerBlockAccess access = TrailerBlockAccess.fromBits(accessBits[3]);
            byte[] trailerCopy = blocks[3].clone();

            // TODO: Allow partial update? or fail?
            if (access.writeAccessKeyA.allowedUsingKey(key)) {
                System.arraycopy(blockData, 0, trailerCopy, 0, 6);
            }

            if (access.writeAccessAccessBits.allowedUsingKey(key)) {
                System.arraycopy(blockData, 6, trailerCopy, 6, 4);
            }

            if (access.writeAccessKeyB.allowedUsingKey(key)) {
                System.arraycopy(blockData, 10, trailerCopy, 10, 6);
            }

            blocks[3] = trailerCopy;
            return true;
        }
    }
}
