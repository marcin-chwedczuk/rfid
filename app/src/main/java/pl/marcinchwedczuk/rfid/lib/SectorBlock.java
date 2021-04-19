package pl.marcinchwedczuk.rfid.lib;

public class SectorBlock {
    public final Sector sector;
    public final Block block;

    public SectorBlock(Sector sector, Block block) {
        this.sector = sector;
        this.block = block;
    }

    public int blockNumber() {
        return sector.index *4 + block.index;
    }

    public static SectorBlock of(Sector sector, Block block) {
        return new SectorBlock(sector, block);
    }
}
