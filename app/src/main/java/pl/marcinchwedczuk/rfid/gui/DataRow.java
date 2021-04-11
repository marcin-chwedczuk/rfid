package pl.marcinchwedczuk.rfid.gui;

import pl.marcinchwedczuk.rfid.lib.ByteUtils;

public class DataRow {
    public final int sector;
    public final int block;
    public final byte[] bytes;
    public final boolean isSectorTrailer;

    public DataRow(int sector, int block, byte[] bytes, boolean isSectorTrailer) {
        this.sector = sector;
        this.block = block;
        this.bytes = bytes;
        this.isSectorTrailer = isSectorTrailer;
    }

    public boolean isManufacturerDataBlock() {
        return sector == 0 && block == 0;
    }

    public int getSector() {
        return sector;
    }

    public int getBlock() {
        return block;
    }

    public String toDebugString() {
        return String.format("S: %d, B: %d, %s, trailer? %s", sector, block,
                ByteUtils.asHexString(bytes, ":"), isSectorTrailer);
    }
}
