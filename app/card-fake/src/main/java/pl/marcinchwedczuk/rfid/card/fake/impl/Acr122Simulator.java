package pl.marcinchwedczuk.rfid.card.fake.impl;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.card.fake.CommandHistory;

import javax.smartcardio.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Fake card that represents ACR122 terminal itself.
 * The card may be present on the terminal or not.
 * Even if the card is not present terminal can accept
 * some commands like `getFirmwareVersion`.
 */
public class Acr122Simulator {
    private final CommandHistory commandHistory;
    private final Mifare1KSimulator mifare1K;

    private int piccOperatingParameter = 0xff;

    public Acr122Simulator(CommandHistory commandHistory) {
        this(commandHistory, null);
    }

    public Acr122Simulator(CommandHistory commandHistory,
                           Mifare1KSimulator mifare1K) {
        this.commandHistory = Objects.requireNonNull(commandHistory);
        this.mifare1K = mifare1K;
    }

    public ATR getATR() {
        if (mifare1K != null) {
            return mifare1K.getATR();
        }
        else {
            // Tested on real hardware.
            return new ATR(new byte[] { });
        }
    }

    public ResponseAPDU executeCommand(CommandAPDU cmd) throws CardException {
        commandHistory.addCommand(cmd.getBytes(), "received command");

        ResponseAPDU response = tryExecuteTerminalCommand(cmd);
        if (response != null) {
            return response;
        }

        if (mifare1K != null) {
            // If card is present then relay the command to it.
            return mifare1K.execute(cmd);
        }
        else {
            // This part is tricky - this code will throw an IllegalArgumentException.
            // This reproduces the actual behaviour of the JavaCardIO.
            return new ResponseAPDU(new byte[] { });
        }
    }

    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        commandHistory.addCommand(command, "received escape command");
        return tryExecuteTerminalCommand(new CommandAPDU(command)).getBytes();
    }

    private ResponseAPDU tryExecuteTerminalCommand(CommandAPDU cmd) {
        if (matchesPattern(cmd, "FF 00 50 00 00")) {
            // Get PICC operating parameter
            return new ResponseAPDU(ByteArrays.of(0x90, piccOperatingParameter));
        }

        if (matchesPattern(cmd, "FF 00 51 .. 00")) {
            // Set PICC operating parameter
            byte newPicc = cmd.getBytes()[3];
            this.piccOperatingParameter = newPicc & 0xFF;
            return new ResponseAPDU(ByteArrays.of(0x90, piccOperatingParameter));
        }

        if (matchesPattern(cmd, "FF 00 40 .. 04 .. .. .. ..")) {
            // Let & Buzzer settings - no-op
            return new ResponseAPDU(ByteArrays.of(0x90, 0x00));
        }

        if (matchesPattern(cmd, "FF 00 41 .. 00")) {
            // Set timeout - no-op
            return new ResponseAPDU(ByteArrays.of(0x90, 0x00));
        }

        if (matchesPattern(cmd, "FF 00 48 00 00")) {
            // Get firmware version
            return new ResponseAPDU("FAKE1.0".getBytes(StandardCharsets.US_ASCII));
        }

        if (matchesPattern(cmd, "FF 00 52 .. 00")) {
            // set enabled buzzer during card detection
            boolean valid = (
                    cmd.getBytes()[3] == 0x00 ||
                    cmd.getBytes()[3] == (byte)0xff
            );

            if (valid)
                return new ResponseAPDU(ByteArrays.of(0x90, 0x00));

            return new ResponseAPDU(ByteArrays.of(0x63, 0x00));
        }

        return null;
    }

    private boolean matchesPattern(CommandAPDU cmd, String regex) {
        String bytes = ByteArrays.toHexString(cmd.getBytes());
        return bytes.matches(regex);
    }
}
