package pl.marcinchwedczuk.rfid.gui.sender;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class CommandTextParser {
    public byte[] parseToBytes(String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("String has wrong format.");
        }

        String bytesOnly = removeComments(s);
        String normalized = bytesOnly
                // Add space after each hex byte group
                .replaceAll("([a-fA-F0-9]{2})", "$1 ")
                .replaceAll("\\s+", " ");
        return ByteArrays.fromHexString(normalized);
    }

    public boolean isValid(String s) {
        String bytesOnly = removeComments(s);

        boolean isValid = Arrays.stream(bytesOnly.split("\n"))
                .allMatch(line -> line.matches("^(\\s*[a-fA-F0-9]{2})*\\s*$"));

        return isValid;
    }

    private static String removeComments(String s) {
        return Arrays.stream(s.split("\n"))
                // Comments are starting with # character.
                .map(line -> line.replaceAll("#.*$", ""))
                .collect(joining("\n"));
    }
}
