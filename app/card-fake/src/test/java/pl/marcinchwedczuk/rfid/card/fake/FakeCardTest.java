package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;

import javax.smartcardio.ATR;

public class FakeCardTest {
    FakeCard fake = new FakeCard();

    @Test
    void returns_proper_ATR() {
        byte[] atrBytes = fake.getATR().getBytes();
        byte[] expected = StringUtils.byteArrayFromHexString(
                "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A");


    }
}