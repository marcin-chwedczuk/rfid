package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.card.fake.CommandHistory;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseTerminalCommandsTest {
    protected abstract AcrTerminalCommands terminal();

    @Test
    void getting_and_setting_picc_parameter_works() {
        // Should return the default PICC
        PiccOperatingParameter picc = terminal().getPiccOperatingParameter();
        assertThat(picc)
                .isNotNull();

        picc.setTopaz(DetectionStatus.IGNORE);
        picc.setPollingInterval(PoolingInterval.INTERVAL_500_MILLIS);
        terminal().setPiccOperatingParameter(picc);

        picc = terminal().getPiccOperatingParameter();

        assertThat(picc.getTopaz())
                .isEqualTo(DetectionStatus.IGNORE);
        assertThat(picc.getPollingInterval())
                .isEqualTo(PoolingInterval.INTERVAL_500_MILLIS);
    }

    @Test
    void configuring_buzzer_works() {
        LedBuzzerSettings settings = LedBuzzerSettings.newDefaults();

        settings.setT1(TDuration.fromNumberOf100msUnits(1));
        settings.setT2(TDuration.fromNumberOf100msUnits(1));
        settings.setNumberOfRepetitions(3);

        settings.setLinkToBuzzer(Buzzer.BUZZER_DURING_T1);

        // Blink with Green LED
        LedSettings ledSettings = settings.getLedSettings();
        ledSettings.setInitialBlinkingGreenLED(LedState.OFF);
        ledSettings.setBlinkingMaskGreenLED(LedBlinkingMask.BLINK);
        // And let the Green LED ON after that
        ledSettings.setFinalGreenLED(LedState.ON);
        ledSettings.setMaskGreenLED(StateMask.UPDATE);

        terminal().configureLedAndBuzzer(settings);

        FakeCardTerminal fakeTerminal = (FakeCardTerminal)terminal().getUnderlyingTerminal();
        CommandHistory.CommandHistoryEntry lastCommand = fakeTerminal.getCommandHistory().getLastEntry();

        // This is the only way I can think of to test this.
        assertThat(ByteArrays.toHexString(lastCommand.commandBytes))
                .isEqualTo("FF 00 40 8A 04 01 01 03 01");
    }
}
