package pl.marcinchwedczuk.rfid.gui;

import pl.marcinchwedczuk.rfid.lib.ByteUtils;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public String toString() {
        return String.format("S: %d, B: %d, %s, trailer? %s", sector, block,
                ByteUtils.asHexString(bytes, ":"), isSectorTrailer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRow dataRow = (DataRow) o;
        return sector == dataRow.sector &&
                block == dataRow.block &&
                isSectorTrailer == dataRow.isSectorTrailer &&
                Arrays.equals(bytes, dataRow.bytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sector, block, isSectorTrailer);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }
}
