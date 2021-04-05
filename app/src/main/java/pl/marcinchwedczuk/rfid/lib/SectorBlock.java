package pl.marcinchwedczuk.rfid.lib;

public class SectorBlock {
    public final int sector;
    public final int block;

    private SectorBlock(int sector, int block) {
        if (sector < 0 || sector > 16*4) {
            throw new IllegalArgumentException("Invalid sector number for Mifare 1K/4K card.");
        }
        if (block < 0 || block >= 4) {
            throw new IllegalArgumentException("Invalid block number for Mifare 1K/4K card.");
        }

        this.sector = sector;
        this.block = block;
    }

    public int blockNumber() {
        return sector*4 + block;
    }

    public static SectorBlock fromSectorAndBlock(int sector, int block) {
        return new SectorBlock(sector, block);
    }

    public static SectorBlock firstBlockOfSector(int sector) {
        return fromSectorAndBlock(sector, 0);
    }
}
