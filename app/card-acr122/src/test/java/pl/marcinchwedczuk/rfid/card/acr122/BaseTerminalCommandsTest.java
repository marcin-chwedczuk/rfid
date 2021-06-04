package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;

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
        // TODO: Fake card currently only accept the command.
        // TODO: Make it possible to inspect values send to fake card

        LedBuzzerSettings settings = new LedBuzzerSettings();

        terminal().configureLedAndBuzzer(settings);
    }
}
