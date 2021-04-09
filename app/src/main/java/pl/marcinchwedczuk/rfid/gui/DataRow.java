package pl.marcinchwedczuk.rfid.gui;

public class DataRow {
    public final int sector;
    public final int block;
    public final byte[] bytes;

    public DataRow(int sector, int block, byte[] bytes) {
        this.sector = sector;
        this.block = block;
        this.bytes = bytes;
    }

    public int getSector() {
        return sector;
    }

    public int getBlock() {
        return block;
    }
}
