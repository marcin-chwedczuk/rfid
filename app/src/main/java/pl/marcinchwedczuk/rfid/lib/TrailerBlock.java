package pl.marcinchwedczuk.rfid.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrailerBlock {
    public byte[] keyA;
    public byte[] keyB;
    public AccessBits accessBits;

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
}
