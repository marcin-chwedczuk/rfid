package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;

public abstract class BaseTerminalCommandsTest {
    private final AcrTerminalCommands terminal;

    protected BaseTerminalCommandsTest(AcrTerminalCommands terminal) {
        this.terminal = terminal;
    }

    @Test
    void readPiccOperatingParameter() {
        PiccOperatingParameter picc = terminal.readPiccOperatingParameter();

        // We read default PICC parameter

    }
}
