package pl.marcinchwedczuk.rfid.acr122;

import java.util.Arrays;

public class TrailerBlock {
    private static final byte FF = (byte)0xff;

    public byte[] keyA;
    public byte[] keyB;
    public AccessBits accessBits;

    public TrailerBlock() {
        this(new byte[] {
                FF, FF, FF, FF, FF, FF,
                FF, 0x07, (byte)0x80, 0x69,
                FF, FF, FF, FF, FF, FF,
        });
    }

    public TrailerBlock(byte[] data) {
        if (data.length != 16) {
            throw new IllegalArgumentException("Invalid data size!");
        }

        keyA = Arrays.copyOfRange(data, 0, 6);
        keyB = Arrays.copyOfRange(data, 10, 16);

        accessBits = new AccessBits(Arrays.copyOfRange(data, 6, 10));
    }

    public String keyAHexString() {
        return ByteUtils.asHexString(keyA, ":");
    }

    public String keyBHexString() {
        return ByteUtils.asHexString(keyB, ":");
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
        byte[] result = new byte[16];

        System.arraycopy(keyA, 0, result, 0, 6);
        System.arraycopy(keyB, 0, result, 10, 6);

        byte[] accessBytes = accessBits.toBytes();
        System.arraycopy(accessBytes, 0, result, 6, 4);

        return result;
    }

    private void validateKey(byte[] key) {
        if (key == null || key.length != 6)
            throw new IllegalArgumentException("Invalid key!");
    }
}