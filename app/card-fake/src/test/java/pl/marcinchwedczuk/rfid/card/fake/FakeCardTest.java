package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.*;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;

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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class write_data_to_card {
        // Card has default transport configuration
        FakeCard writableCard = new FakeCard();

        @Test
        @Order(10)
        void can_load_keyA_into_register_1() throws CardException {
            // Load key FF:::FF into register 1
            CommandAPDU loadKey0Cmd = cmd("FF 82 00 01 06 FF FF FF FF FF FF");

            ResponseAPDU response = fake
                    .getBasicChannel()
                    .transmit(loadKey0Cmd);

            assertThat(response.getBytes())
                    .isEqualTo(new byte[] { (byte)0x90, (byte)0x00 });
        }

        @Test
        @Order(20)
        void authenticate_using_keyA_in_reg_1_to_sector_0() throws CardException {
            // Authenticate to block 0
            CommandAPDU authBlock0Cmd = cmd("FF 86 00 00 05 01 00 00 60 01");

            ResponseAPDU response = fake
                    .getBasicChannel()
                    .transmit(authBlock0Cmd);

            assertThat(response.getBytes())
                    .isEqualTo(new byte[] { (byte)0x90, (byte)0x00 });
        }

        @Test
        @Order(30)
        void read_all_data_from_sector_0() throws CardException {
            String emptyBlock =
                    "00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00";

            assertCanRead("00", emptyBlock);
            assertCanRead("01", emptyBlock);
            assertCanRead("02", emptyBlock);

            // Notice keyA was zero'ed
            String defaultTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";

            assertCanRead("03", defaultTrailerBlock);
        }

        private void assertCanRead(String block, String expectedData) throws CardException {
            CommandAPDU authBlock0Cmd = cmd("FF B0 00 " + block + " 10");

            ResponseAPDU response = fake
                    .getBasicChannel()
                    .transmit(authBlock0Cmd);

            assertThat(response.getBytes())
                    .isEqualTo(ByteArrays.fromHexString(expectedData + " 90 00"));
        }

        @Test
        @Order(40)
        @Disabled("To verify on real card")
        void cannot_write_data_to_manufacturer_data_section() throws CardException {
            // Block 0 of sector 0 is manufacturer data (including card id)
            writeData("00", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");

            // Check data actually not changed
            // TODO: Verify on real card

            assertCanRead("00", "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        }

        @Test
        @Order(50)
        void can_write_data_to_card() throws CardException {
            writeData("01", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
            assertCanRead("01", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
        }

        private void writeData(String block, String data) throws CardException {
            CommandAPDU authBlock0Cmd = cmd("FF D6 00 " + block + " 10 " + data);

            ResponseAPDU response = fake
                    .getBasicChannel()
                    .transmit(authBlock0Cmd);

            assertThat(response.getBytes())
                    .isEqualTo(new byte[] { (byte)0x90, (byte)0x00 });

        }
    }

    private static CommandAPDU cmd(String bytesHexString) {
        return new CommandAPDU(ByteArrays.fromHexString(bytesHexString));
    }
}