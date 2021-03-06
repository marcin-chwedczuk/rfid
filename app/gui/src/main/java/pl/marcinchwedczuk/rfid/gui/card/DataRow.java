package pl.marcinchwedczuk.rfid.gui.card;

import pl.marcinchwedczuk.rfid.card.acr122.Block;
import pl.marcinchwedczuk.rfid.card.acr122.Sector;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;
import java.util.Objects;

public class DataRow {
    public final int sector;
    public final int block;
    public final byte[] bytes;
    public final boolean isSectorTrailer;

    public DataRow(Sector sector, Block block, byte[] bytes) {
        this.sector = sector.index;
        this.block = block.index;
        this.bytes = bytes;
        this.isSectorTrailer = block.isTrailer();
    }

    public boolean isManufacturerDataBlock() {
        return sector == 0 && block == 0;
    }

    public boolean isSectorTrailer() {
        return Block.of(block).isTrailer();
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
                ByteArrays.toMacString(bytes), isSectorTrailer);
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
