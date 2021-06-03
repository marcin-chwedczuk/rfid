package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseFakeCardTest {
    static final String _00_00_00_00_00_00 = "00 00 00 00 00 00";
    static final String FF_FF_FF_FF_FF_FF = "FF FF FF FF FF FF";
    static final String AA_BB_CC_DD_EE_FF = "AA BB CC DD EE FF";
    static final String AA_AA_AA_AA_AA_AA = "AA AA AA AA AA AA";

    static final String RESP_OK = "90 00";
    static final String RESP_ERR = "63 00";

    static final String BLOCK_DATA_ZEROS = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    static final String BLOCK_DATA_00_01_02___0F = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F";

    Card card;
    FakeCardTestUtil testUtil;

    BaseFakeCardTest(Card card) {
        this.card = Objects.requireNonNull(card);
        this.testUtil = new FakeCardTestUtil(card);
    }

    protected abstract ResponseAPDU sendToCard(String commandBytes) throws CardException;

    @Test
    void returns_firmware_version() throws CardException {
        ResponseAPDU response = sendToCard("FF 00 48 00 00");

        assertThat(response.getBytes())
                .asString()
                .isEqualTo("FAKE1.0");
    }

    @Test
    void returns_current_picc() throws CardException {
        ResponseAPDU response = sendToCard("FF 00 50 00 00");

        // Default value of Picc is 0xff
        assertThatResponseBytes(response)
                .isEqualTo("90 FF");
    }

    @Test
    void can_change_picc() throws CardException {
        // Change PICC to 0xCC
        ResponseAPDU response = sendToCard("FF 00 51 CC 00");
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");

        // Read PICC after change
        response = sendToCard("FF 00 50 00 00");
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");
    }

    @Test
    void returns_failure_for_led_and_buzzer_commands() throws CardException {
        // This is a no-op operation on FakeCard.
        ResponseAPDU response = sendToCard("FF 00 40 0F 04 02 01 01 01");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Test
    void can_set_buzz_during_card_detection() throws CardException {
        // This is a no-op operation on FakeCard.
        ResponseAPDU response = sendToCard("FF 00 52 00 00");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Test
    void can_set_timeout() throws CardException {
        // Timeout is a no-op on fake card.
        // We only test that the command is being accepted.
        ResponseAPDU response = sendToCard("FF 00 41 08 00");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    static AbstractStringAssert<?> assertThatResponseBytes(ResponseAPDU r) {
        return assertThatHexStringOf(r.getBytes());
    }

    static AbstractStringAssert<?> assertThatHexStringOf(byte[] bytes) {
        return assertThat(ByteArrays.toHexString(bytes));
    }
}
