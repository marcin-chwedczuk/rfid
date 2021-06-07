package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import javax.smartcardio.CardNotPresentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcrTerminal_CardAbsent_Test extends BaseTerminalCommandsTest {
    AcrTerminal acrTerminal = new AcrTerminal(FakeCardTerminal.withCardAbsent());

    @Override
    protected AcrTerminalCommands terminal() {
        return acrTerminal;
    }

    @Test
    void isCardPresent_returns_false() {
        assertThat(acrTerminal.isCardPresent())
                .isFalse();
    }

    @Test
    void name_returns_terminal_name() {
        assertThat(acrTerminal.name())
                .isEqualTo("Fake Terminal (CARD_ABSENT)");
    }

    @Test
    void attempt_to_connect_ends_with_exception() {
        assertThatThrownBy(() -> acrTerminal.connect())
                .isInstanceOf(AcrException.class)
                .hasCauseInstanceOf(CardNotPresentException.class)
                .hasMessageContaining("Cannot connect");
    }
}