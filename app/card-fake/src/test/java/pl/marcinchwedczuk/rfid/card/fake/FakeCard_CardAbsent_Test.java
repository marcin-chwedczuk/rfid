package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;
import pl.marcinchwedczuk.rfid.card.fake.impl.CardState;

import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

public class FakeCard_CardAbsent_Test extends BaseFakeCardTest {
    FakeCard_CardAbsent_Test() {
        super(new FakeCard("DIRECT", new Acr122Simulator()));
    }

    @Override
    protected ResponseAPDU sendToCard(String commandBytes) throws CardException {
        return new ResponseAPDU(
                card.transmitControlCommand(
                    // Escape code is different for each OS.
                    // For simplicity fake-card simply ignores it.
                    0xAABBCCDD,
                    ByteArrays.fromHexString(commandBytes)));
    }

    @Test
    void returns_empty_ATR() {
        byte[] atrBytes = card.getATR().getBytes();

        assertThatHexStringOf(atrBytes)
                .isEqualTo("");
    }

}
