package pl.marcinchwedczuk.rfid.card.acr122;

import java.util.ArrayList;
import java.util.List;

public class AccessBits {
    public List<DataBlockAccess> dataBlockAccesses = new ArrayList<>();
    public SectorTrailerAccess sectorTrailerAccess;

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
                tmp = SectorTrailerAccess.fromCBits(block_c1, block_c2, block_c3);
            }
        }

        this.sectorTrailerAccess = tmp;
    }

    public byte[] toBytes() {
        int[] block0 = dataBlockAccesses.get(0).getCBits();
        int[] block0_neg = negate(block0);

        int[] block1 = dataBlockAccesses.get(1).getCBits();
        int[] block1_neg = negate(block1);

        int[] block2 = dataBlockAccesses.get(2).getCBits();
        int[] block2_neg = negate(block2);

        int[] block3 = sectorTrailerAccess.getCBits();
        int[] block3_neg = negate(block3);

        // see: docs/mifare_classic.pdf, Figure 10, page 11.
        byte byte_6 = fromBits(block3_neg[2], block2_neg[2], block1_neg[2], block0_neg[2],
                block3_neg[1], block2_neg[1], block1_neg[1], block0_neg[1]);

        byte byte_7 = fromBits(block3[1], block2[1], block1[1], block0[1],
                block3_neg[3], block2_neg[3], block1_neg[3], block0_neg[3]);

        byte byte_8 = fromBits(block3[3], block2[3], block1[3], block0[3],
                block3[2], block2[2], block1[2], block0[2]);

        byte byte_9 = 0x69; // Lol

        return new byte[] { byte_6, byte_7, byte_8, byte_9 };
    }

    private byte fromBits(int msb, int b6, int b5, int b4, int b3, int b2, int b1, int lsb) {
        return (byte)(
            ((msb & 1) << 7) |
            ((b6 & 1) << 6) |
            ((b5 & 1) << 5) |
            ((b4 & 1) << 4) |
            ((b3 & 1) << 3) |
            ((b2 & 1) << 2) |
            ((b1 & 1) << 1) |
            ((lsb & 1) << 0)
        );
    }

    private int[] negate(int[] bytes) {
        int[] result = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = 1 - (bytes[i] & 1);
        }
        return result;
    }

    public void setSectorTrailerAccess(String cbits) {
        this.sectorTrailerAccess = SectorTrailerAccess.fromCBits(cbits);
    }

    public void setDataBlockAccess(int block, String cbits) {
        this.dataBlockAccesses.set(block, DataBlockAccess.fromCBits(cbits));
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
