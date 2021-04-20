package pl.marcinchwedczuk.rfid.lib;

import java.util.List;

public class Block {
    public final int index;

    private Block(int index) {
        if (index < 0) throw new IllegalArgumentException("index");

        if (index > 4) throw new IllegalArgumentException(
                "Mifare cards can have at most 4 blocks per sector.");

        this.index = index;
    }

    public boolean isTrailer() {
        return index == 3;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

    public static Block of(int number) {
        return new Block(number);
    }

    public static List<Block> allBlocksInSector() {
        return List.of(BLOCK_0, BLOCK_1, BLOCK_2, BLOCK_3);
    }

    public static List<Block> dataBlocksInSector() {
        return List.of(BLOCK_0, BLOCK_1, BLOCK_2);
    }

    public static final Block BLOCK_0 = new Block(0);
    public static final Block BLOCK_1 = new Block(1);
    public static final Block BLOCK_2 = new Block(2);
    public static final Block BLOCK_3 = new Block(3);

    public static final Block TRAILER = BLOCK_3;
}
