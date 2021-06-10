package pl.marcinchwedczuk.rfid.card.commons;

import pl.marcinchwedczuk.rfid.card.commons.utils.ToStringBuilder;

import java.util.List;
import java.util.Objects;

public class AccessBits {
    private final List<DataBlockAccess> dataBlockAccesses;
    private final TrailerBlockAccess trailerBlockAccess;

    public AccessBits(
            DataBlockAccess dataBlock0Access,
            DataBlockAccess dataBlock1Access,
            DataBlockAccess dataBlock2Access,
            TrailerBlockAccess trailerBlockAccess) {
        this.dataBlockAccesses = List.of(
                Objects.requireNonNull(dataBlock0Access),
                Objects.requireNonNull(dataBlock1Access),
                Objects.requireNonNull(dataBlock2Access));
        this.trailerBlockAccess = Objects.requireNonNull(trailerBlockAccess);
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

    @Override
    public String toString() {
        return new ToStringBuilder(AccessBits.class)
                .appendField("dataBlockAccesses(0)", dataBlockAccesses.get(0))
                .appendField("dataBlockAccesses(1)", dataBlockAccesses.get(1))
                .appendField("dataBlockAccesses(2)", dataBlockAccesses.get(2))
                .toString();
    }
}
