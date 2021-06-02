package pl.marcinchwedczuk.rfid.card.fake.impl;

import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;

import javax.smartcardio.*;
import java.nio.charset.StandardCharsets;

/**
 * Fake card that represents ACR122 terminal itself.
 * The card may be present on the terminal or not.
 * Even if the card is not present terminal can accept
 * some commands like `getFirmwareVersion`.
 */
public class Acr122Simulator {
    private final Mifare1KSimulator mifare1K;

    private int piccOperatingParameter = 0xff;

    public Acr122Simulator(Mifare1KSimulator mifare1K) {
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
        // Escape commands
        return tryExecuteTerminalCommand(new CommandAPDU(command)).getBytes();
    }

    private ResponseAPDU tryExecuteTerminalCommand(CommandAPDU cmd) {
        if (matchesPattern(cmd, "FF 00 50 00 00")) {
            // Get PICC operating parameter
            return new ResponseAPDU(new byte[]{(byte) 0x90, (byte) piccOperatingParameter});
        }

        if (matchesPattern(cmd, "FF 00 51 .. 00")) {
            // Save PICC operating parameter
            byte newPicc = cmd.getBytes()[3];
            this.piccOperatingParameter = newPicc & 0xFF;
            return new ResponseAPDU(new byte[]{(byte) 0x90, (byte) piccOperatingParameter});
        }

        if (matchesPattern(cmd, "FF 00 40 .. 04 .. .. .. ..")) {
            // Let & Buzzer settings - return failure, fake card does not supports it
            return new ResponseAPDU(new byte[]{(byte) 0x63, (byte) 0x00});
        }

        if (matchesPattern(cmd, "FF 00 41 .. 00")) {
            // Set timeout
            return new ResponseAPDU(new byte[]{(byte) 0x90, (byte) 0x00});
        }

        if (matchesPattern(cmd, "FF 00 48 00 00")) {
            // Get firmware version
            return new ResponseAPDU("FAKE1.0".getBytes(StandardCharsets.US_ASCII));
        }

        if (matchesPattern(cmd, "FF 00 52 .. 00")) {
            // set enabled buzzer during card detection
            boolean valid = (
                    cmd.getBytes()[3] == 0x00 ||
                            cmd.getBytes()[3] == (byte)0xff);

            if (valid)
                return new ResponseAPDU(new byte[]{(byte) 0x90, (byte) 0x00});

            return new ResponseAPDU(new byte[]{(byte) 0x63, (byte) 0x00});
        }

        return null;
    }

    private boolean matchesPattern(CommandAPDU cmd, String regex) {
        String bytes = ByteArrays.toHexString(cmd.getBytes());
        return bytes.matches(regex);
    }


}
