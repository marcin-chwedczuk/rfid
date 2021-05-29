package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeCardTest {
    FakeCard fake = new FakeCard();

    @Test
    void returns_proper_ATR() {
        byte[] atrBytes = fake.getATR().getBytes();
        byte[] expected = ByteArrays.fromHexString(
                // Example ATR for MIFARE 1K cards
                "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A");

        assertThat(atrBytes)
                .isEqualTo(expected);
    }

    @Test
    void returns_firmware_version() throws CardException {
        CommandAPDU getFirmwareCmd = cmd("FF 00 48 00 00");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(getFirmwareCmd);

        assertThat(new String(response.getBytes()))
                .isEqualTo("FAKE1.0");
    }

    @Test
    void returns_default_value_of_picc() throws CardException {
        CommandAPDU getPiccCmd = cmd("FF 00 50 00 00");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(getPiccCmd);

        // Default value of Picc is 0xff
        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x90, (byte)0xFF });
    }

    @Test
    void can_update_picc() throws CardException {
        // Change PICC to 0xCC
        CommandAPDU changePiccCmd = cmd("FF 00 51 CC 00");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(changePiccCmd);
        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x90, (byte)0xCC });

        // Read PICC after change
        CommandAPDU getPiccCmd = cmd("FF 00 50 00 00");

        response = fake
                .getBasicChannel()
                .transmit(getPiccCmd);

        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x90, (byte)0xCC });
    }

    @Test
    void returns_failure_for_led_and_buzzer_commands() throws CardException {
        CommandAPDU ledBuzzCmd = cmd("FF 00 40 0F 04 02 01 01 01");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(ledBuzzCmd);

        // 0x63 == failure
        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x63, (byte)0x00 });
    }

    @Test
    void can_set_timeout() throws CardException {
        CommandAPDU ledBuzzCmd = cmd("FF 00 41 08 00");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(ledBuzzCmd);

        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x90, (byte)0x00 });
    }

    @Test
    void can_set_buzz_during_card_detection() throws CardException {
        CommandAPDU ledBuzzCmd = cmd("FF 00 52 00 00");

        ResponseAPDU response = fake
                .getBasicChannel()
                .transmit(ledBuzzCmd);

        assertThat(response.getBytes())
                .isEqualTo(new byte[] { (byte)0x90, (byte)0x00 });
    }

    private static CommandAPDU cmd(String bytesHexString) {
        return new CommandAPDU(ByteArrays.fromHexString(bytesHexString));
    }
}