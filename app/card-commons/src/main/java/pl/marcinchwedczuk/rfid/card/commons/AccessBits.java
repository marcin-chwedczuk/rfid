package pl.marcinchwedczuk.rfid.card.commons;

import java.util.List;

public class AccessBits {
    private final List<DataBlockAccess> dataBlockAccesses;
    private final TrailerBlockAccess trailerBlockAccess;

    public AccessBits(
            DataBlockAccess dataBlock0Access,
            DataBlockAccess dataBlock1Access,
            DataBlockAccess dataBlock2Access,
            TrailerBlockAccess trailerBlockAccess) {
        this.dataBlockAccesses = List.of(dataBlock0Access, dataBlock1Access, dataBlock2Access);
        this.trailerBlockAccess = trailerBlockAccess;
    }

    public DataBlockAccess dataBlockAccessForBlock(int block) {
        // TODO: Move block and sector to commons
        return dataBlockAccesses.get(block);
    }

    public List<DataBlockAccess> getDataBlockAccesses() {
        return dataBlockAccesses;
    }

    public TrailerBlockAccess trailerBlockAccess() {
        return trailerBlockAccess;
    }
}
