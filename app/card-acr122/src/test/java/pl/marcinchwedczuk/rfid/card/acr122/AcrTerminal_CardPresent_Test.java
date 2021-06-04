package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import javax.smartcardio.CardNotPresentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcrTerminal_CardPresent_Test {
    AcrTerminal acrTerminal = new AcrTerminal(FakeCardTerminal.withCardAbsent());

    @Test
    void isCardPresent_returns_false() {
        assertThat(acrTerminal.isCardPresent())
                .isFalse();
    }

    @Test
    void returns_terminal_name() {
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