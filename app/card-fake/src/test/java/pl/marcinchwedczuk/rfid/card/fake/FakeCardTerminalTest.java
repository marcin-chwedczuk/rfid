package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FakeCardTerminalTest {
    @Nested
    class when_card_is_absent {
        CardTerminal fakeCardTerminal = FakeCardTerminal.withCardAbsent();

        @Test
        void getName_returns_terminal_name() {
            assertThat(fakeCardTerminal.getName())
                    .isNotBlank();
        }

        @Test
        void can_connect_to_terminal_using_DIRECT_protocol() throws CardException {
            Card card = fakeCardTerminal.connect("DIRECT");

            assertThat(card)
                    .isNotNull();

            assertThat(card.getProtocol())
                    .isEqualTo("DIRECT");
        }

        @Test
        void isCardPresent_returns_false() throws CardException {
            assertThat(fakeCardTerminal.isCardPresent())
                    .isFalse();
        }

        @Test
        void waitForCardPresent_returns_false() throws CardException {
            boolean result = fakeCardTerminal.waitForCardPresent(0);

            assertThat(result)
                    .isFalse();
        }

        @Test
        void waitForCardAbsent_returns_true() throws CardException {
            boolean result = fakeCardTerminal.waitForCardAbsent(0);

            assertThat(result)
                    .isTrue();
        }
    }

    @Nested
    class when_card_is_present {
        CardTerminal fakeCardTerminal = FakeCardTerminal.withCardPresent();

        @Test
        void getName_returns_terminal_name() {
            assertThat(fakeCardTerminal.getName())
                    .isNotBlank();
        }

        @Test
        void can_connect_to_terminal_using_T0_protocol() throws CardException {
            Card card = fakeCardTerminal.connect("T=0");

            assertThat(card)
                    .isNotNull();

            assertThat(card.getProtocol())
                    .isEqualTo("T=0");
        }

        @Test
        void isCardPresent_returns_true() throws CardException {
            assertThat(fakeCardTerminal.isCardPresent())
                    .isTrue();
        }

        @Test
        void waitForCardPresent_returns_true() throws CardException {
            boolean result = fakeCardTerminal.waitForCardPresent(0);

            assertThat(result).isTrue();
        }

        @Test
        void waitForCardAbsent_returns_false() throws CardException {
            boolean result = fakeCardTerminal.waitForCardAbsent(0);

            assertThat(result).isFalse();
        }
    }
}