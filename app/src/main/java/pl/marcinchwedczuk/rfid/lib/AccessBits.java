package pl.marcinchwedczuk.rfid.lib;

import java.util.ArrayList;
import java.util.List;

public class AccessBits {
    public final List<DataBlockAccess> dataBlockAccesses = new ArrayList<>();
    public final SectorTrailerAccess sectorTrailerAccess;

    public AccessBits(byte[] accessBits) {
        // See docs/mifare_classic.pdf, page 11

        byte b6 = accessBits[0];
        byte b7 = accessBits[1];
        byte b8 = accessBits[2];

        int c1_neg = b6 & 0x0f;
        int c1 = (b7 & 0xf0) >> 4;
        if (c1 != (0x0f - c1_neg)) {
            throw new AcrException("Corrupted access bits in C1 range!");
        }

        int c2_neg = (b6 & 0xf0) >> 4;
        int c2 = b8 & 0x0f;
        if (c2 != (0x0f - c2_neg)) {
            throw new AcrException("Corrupted access bits in C2 range!");
        }

        int c3_neg = b7 & 0x0f;
        int c3 = (b8 & 0xf0) >> 4;
        if (c3 != (0x0f - c3_neg)) {
            throw new AcrException("Corrupted access bits in C3 range!");
        }

        SectorTrailerAccess tmp = null;
        for (int blockNo = 0; blockNo <= 3; blockNo++) {
            int block_c1 = (c1 & (1 << blockNo)) != 0 ? 1 : 0;
            int block_c2 = (c2 & (1 << blockNo)) != 0 ? 1 : 0;
            int block_c3 = (c3 & (1 << blockNo)) != 0 ? 1 : 0;

            if (blockNo < 3) {
                dataBlockAccesses.add(DataBlockAccess.fromBits(block_c1, block_c2, block_c3));
            }
            else {
                tmp = SectorTrailerAccess.fromBits(block_c1, block_c2, block_c3);
            }
        }

        this.sectorTrailerAccess = tmp;
    }

    public String asTable() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            sb.append("BLOCK ").append(i).append(":").append('\n');
            sb.append(dataBlockAccesses.get(i).asTable());
            sb.append('\n');
        }

        sb.append("ACCESS BITS:\n").append(sectorTrailerAccess.asTable()).append('\n');

        return sb.toString();
    }
}
