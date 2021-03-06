package pl.marcinchwedczuk.rfid.card.fake.impl;

import pl.marcinchwedczuk.rfid.card.commons.*;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import javax.smartcardio.CardException;
import java.util.Arrays;

class MifareSector {
    // Last block is "Sector Trailer"
    private byte[][] blocks = new byte[4][16];

    public MifareSector() {
        // Default sector trailer aka transport configuration
        blocks[3] = ByteArrays.fromHexString(
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

    public boolean authenticate(boolean keyA, byte[] key) {
        // TODO: Check can authenticate
        byte[] internalKey = keyA ? getKeyA() : getKeyB();
        return Arrays.equals(internalKey, key);
    }

    public byte[] read(int blockIndex, byte nBytes, KeyType key) {
        // TODO: Disallow reading manufacturer block

        AccessBits accessBits = new AccessBitsParser().parse(blocks[3]);

        if (blockIndex < 3) {
            // Normal block
            DataBlockAccess access = accessBits.dataBlockAccessForBlock(blockIndex);
            if (!access.readAccess.allowedUsingKey(key)) {
                return null;
            }

            return Arrays.copyOf(blocks[blockIndex], nBytes);
        } else {
            // Sector trailer
            TrailerBlockAccess access = accessBits.trailerBlockAccess();
            byte[] trailerCopy = blocks[3].clone();

            if (!access.readAccessToKeyA.allowedUsingKey(key)) {
                // Zero key A
                Arrays.fill(trailerCopy, 0, 6, (byte) 0x00);
            }

            if (!access.readAccessToAccessBits.allowedUsingKey(key)) {
                Arrays.fill(trailerCopy, 7, 10, (byte) 0x00);
            }

            if (!access.readAccessToKeyB.allowedUsingKey(key)) {
                // Zero key B
                Arrays.fill(trailerCopy, 10, 16, (byte) 0x00);
            }

            return Arrays.copyOf(trailerCopy, nBytes);
        }
    }

    public boolean write(int blockIndex, byte[] blockData, KeyType key) throws CardException {
        AccessBits accessBits = new AccessBitsParser().parse(blocks[3]);

        if (blockIndex < 3) {
            // Write data to normal block
            DataBlockAccess access = accessBits.dataBlockAccessForBlock(blockIndex);
            if (!access.writeAccess.allowedUsingKey(key)) {
                return false;
            }

            System.arraycopy(blockData, 0, blocks[blockIndex], 0, blocks[blockIndex].length);
            return true;
        } else {
            // Change sector trailer (keys & access bits)
            if (!new AccessBitsValidator(blockData).isValid()) {
                return false;
            }

            TrailerBlockAccess access = accessBits.trailerBlockAccess();
            byte[] trailerCopy = blocks[3].clone();

            // TODO: Allow partial update? or fail?
            if (access.writeAccessToKeyA.allowedUsingKey(key)) {
                System.arraycopy(blockData, 0, trailerCopy, 0, 6);
            } else {
                return false;
            }

            if (access.writeAccessToAccessBits.allowedUsingKey(key)) {
                System.arraycopy(blockData, 6, trailerCopy, 6, 4);
            } else {
                // silently ignore failure
            }

            if (access.writeAccessToKeyB.allowedUsingKey(key)) {
                System.arraycopy(blockData, 10, trailerCopy, 10, 6);
            } else {
                return false;
            }

            blocks[3] = trailerCopy;
            return true;
        }
    }

    void setupManufacturerData() {
        // Take from real card, fixed card ID to AA BB CC DD
        blocks[0] = ByteArrays.fromHexString(
                "AA BB CC DD D7 08 04 00 01 6F 01 6D 45 68 F8 1D 90 00");
    }
}
