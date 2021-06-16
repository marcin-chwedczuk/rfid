package pl.marcinchwedczuk.rfid.gui.sender;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexEditorFormatter {
    public String formatAsString(byte[] bytes) {
        List<String> lines = format(bytes);
        return String.join("\n", lines);
    }

    public List<String> format(byte[] bytes) {
        List<String> result = new ArrayList<>();

        final int GROUP_SIZE = 16;
        for (int i = 0; i < bytes.length; i += GROUP_SIZE) {
            byte[] row = Arrays.copyOfRange(bytes, i, Math.min(i + GROUP_SIZE, bytes.length));
            result.add(formatRow(row));
        }

        return result;
    }

    private String formatRow(byte[] row) {
        String bytesPart = ByteArrays.toHexString(row);
        if (row.length > 8) {
            // Insert extra space after 8th byte
            bytesPart = bytesPart.substring(0, 8 * 2 + 7) + " " +
                    bytesPart.substring(8 * 2 + 7);
        }

        String asciiPart = ByteArrays.toPrintableAscii(row);
        return String.format("%-48s %-8s", bytesPart, asciiPart);
    }
}
