package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.CardAbsentFakeCardTerminal;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import javax.smartcardio.CardException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcrTerminalTest {
    @Nested
    class card_is_absent {
        AcrTerminal acrTerminal = new AcrTerminal(new CardAbsentFakeCardTerminal());

        @Test
        void isCardPresent_returns_false() {
            assertThat(acrTerminal.isCardPresent())
                    .isFalse();
        }

        @Test
        void attempt_to_connect_ends_with_exception() {
            assertThatThrownBy(() -> acrTerminal.connect())
                    .isInstanceOf(AcrException.class)
                    .hasCauseInstanceOf(CardException.class)
                    .hasMessageContaining("Cannot connect");
        }
    }

    @Nested
    class card_is_present {
        AcrTerminal acrTerminal = new AcrTerminal(new FakeCardTerminal());

        @Test
        void returns_terminal_name() {
            assertThat(acrTerminal.name())
                    .isEqualTo("Fake Terminal");
        }

        @Test
        void isCardPresent_returns_true() {
            assertThat(acrTerminal.isCardPresent())
                    .isTrue();
        }

        @Test
        void connect_returns_working_AcrCard() {
            AcrCard card = acrTerminal.connect();

            assertThat(card)
                    .isNotNull();

            // Check card works by calling a method
            card.atrInfo();
        }
    }

}