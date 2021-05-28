package pl.marcinchwedczuk.rfid.card.acr122;

public class DataAddress {
    public final Sector sector;
    public final Block block;

    public DataAddress(Sector sector, Block block) {
        this.sector = sector;
        this.block = block;
    }

    public int blockNumber() {
        return sector.index * 4 + block.index;
    }

    @Override
    public String toString() {
        return String.format("DataAddress(sector=%s, block=%s)", sector, block);
    }

    public static DataAddress of(Sector sector, Block block) {
        return new DataAddress(sector, block);
    }
}
