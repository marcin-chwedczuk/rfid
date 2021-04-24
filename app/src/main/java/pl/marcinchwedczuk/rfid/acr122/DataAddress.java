package pl.marcinchwedczuk.rfid.acr122;

public class DataAddress {
    public final Sector sector;
    public final Block block;

    public DataAddress(Sector sector, Block block) {
        this.sector = sector;
        this.block = block;
    }

    public int blockNumber() {
        return sector.index *4 + block.index;
    }

    public static DataAddress of(Sector sector, Block block) {
        return new DataAddress(sector, block);
    }
}
