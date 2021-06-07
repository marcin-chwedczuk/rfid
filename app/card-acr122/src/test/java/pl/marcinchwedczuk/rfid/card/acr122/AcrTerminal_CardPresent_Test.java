package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import javax.smartcardio.CardNotPresentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcrTerminal_CardPresent_Test /*extends BaseTerminalCommandsTest */ {
    AcrTerminal acrTerminal = new AcrTerminal(FakeCardTerminal.withCardPresent());

    // TODO: Uncomment after fixing issue with terminal commands
    // and present card.
    /*
    @Override
    protected AcrTerminalCommands terminal() {
        return acrTerminal;
    }
     */

    @Test
    void isCardPresent_returns_true() {
        assertThat(acrTerminal.isCardPresent())
                .isTrue();
    }

    @Test
    void name_returns_terminal_name() {
        assertThat(acrTerminal.name())
                .isEqualTo("Fake Terminal (CARD_PRESENT)");
    }

    @Test
    void can_connect_to_card() {
        AcrCard card = acrTerminal.connect();
        assertThat(card)
                .isNotNull();

        // Check the returned object is alive
        card.atrInfo();
    }
}