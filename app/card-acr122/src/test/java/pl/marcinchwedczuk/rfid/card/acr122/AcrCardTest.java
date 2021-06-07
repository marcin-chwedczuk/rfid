package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class AcrCardTest {
    AcrTerminal acrTerminal;
    AcrCard acrCard;

    @BeforeEach
    void before() {
        this.acrTerminal = new AcrTerminal(FakeCardTerminal.withCardPresent());
        this.acrCard = acrTerminal.connect();
    }

    @AfterEach
    void after() {
        this.acrCard.disconnect();
        this.acrCard = null;
    }

    @Test
    void terminal_returns_terminal_associated_with_card() {
        AcrTerminal returned = acrCard.terminal();
        assertThat(returned)
                .isSameAs(acrTerminal);
    }

    @Test
    void atrInfo_returns_atr_information() {
        AtrInfo info = acrCard.atrInfo();
        assertThat(info)
                .isNotNull();

        assertThat(info.cardName)
                .isEqualTo(CardName.NXP_MIFARE_STANDARD_1K);

        assertThat(info.cardStandard)
                .isEqualTo(CardStandard.ISO_14443A_LEVEL34);
    }

    @Test
    void getCardUID_returns_card_UID() {
        String cardID = acrCard.getCardUID();
        assertThat(cardID)
                .isEqualTo("AA:BB:CC:DD");
    }
}