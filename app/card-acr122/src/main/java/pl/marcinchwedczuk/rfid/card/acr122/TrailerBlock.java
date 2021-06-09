package pl.marcinchwedczuk.rfid.card.acr122;

import pl.marcinchwedczuk.rfid.card.commons.AccessBits;
import pl.marcinchwedczuk.rfid.card.commons.AccessBitsParser;
import pl.marcinchwedczuk.rfid.card.commons.Mifare1K;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;

public class TrailerBlock {
    // TODO: Hide behind getters/setters
    public byte[] keyA;
    public byte[] keyB;
    public AccessBits accessBits;

    public TrailerBlock() {
        this(Mifare1K.defaultSectorTrailer());
    }

    public TrailerBlock(byte[] data) {
        if (data.length != 16) {
            throw new IllegalArgumentException("Sector trailer should be 16 bytes long");
        }

        keyA = Arrays.copyOfRange(data, 0, 6);
        keyB = Arrays.copyOfRange(data, 10, 16);
        accessBits = new AccessBitsParser().parse(data);
    }

    public String keyAHexString() {
        return ByteArrays.toMacString(keyA);
    }
    public String keyBHexString() {
        return ByteArrays.toMacString(keyB);
    }

    public void setKeyA(byte[] key) {
        validateKey(key);
        keyA = key.clone();
    }

    public void setKeyB(byte[] key) {
        validateKey(key);
        keyB = key.clone();
    }

    public byte[] toBytes() {
        byte[] result = new AccessBitsParser().unparse(accessBits);
        System.arraycopy(keyA, 0, result, 0, 6);
        System.arraycopy(keyB, 0, result, 10, 6);
        return result;
    }

    private void validateKey(byte[] key) {
        if (key == null || key.length != 6)
            throw new IllegalArgumentException("key");
    }
}
