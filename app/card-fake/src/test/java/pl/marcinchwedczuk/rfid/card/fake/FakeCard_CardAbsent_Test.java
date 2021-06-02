package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.Test;

public class FakeCard_CardAbsent_Test extends BaseFakeCardTest {
    FakeCard_CardAbsent_Test() {
        super(new FakeCard("DIRECT", CardState.CARD_ABSENT));
    }

    @Test
    void returns_empty_ATR() {
        byte[] atrBytes = card.getATR().getBytes();

        assertThatHexStringOf(atrBytes)
                .isEqualTo("");
    }
}
