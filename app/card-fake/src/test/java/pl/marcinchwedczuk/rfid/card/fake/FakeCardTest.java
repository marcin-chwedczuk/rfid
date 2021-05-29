package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.*;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_B;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_0;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_1;

public class FakeCardTest {
    static final String FF_FF_FF_FF_FF_FF = "FF FF FF FF FF FF";
    static final String AA_BB_CC_DD_EE_FF = "AA BB CC DD EE FF";
    static final String AA_AA_AA_AA_AA_AA = "AA AA AA AA AA AA";

    static final String RESP_OK = "90 00";
    static final String RESP_ERR = "63 00";

    static final String ZERO_BLOCK = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    FakeCard card = new FakeCard();

    @Test
    void returns_proper_ATR() {
        byte[] atrBytes = card.getATR().getBytes();
        byte[] expected = ByteArrays.fromHexString(
                // Example ATR for MIFARE 1K cards
                "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A");

        assertThat(atrBytes)
                .isEqualTo(expected);
    }

    @Test
    void returns_firmware_version() {
        CommandAPDU getFirmwareCmd = cmd("FF 00 48 00 00");

        ResponseAPDU response = execute(card, getFirmwareCmd);

        assertThat(response.getBytes())
                .asString()
                .isEqualTo("FAKE1.0");
    }

    @Test
    void returns_default_value_of_picc() {
        CommandAPDU getPiccCmd = cmd("FF 00 50 00 00");

        ResponseAPDU response = execute(card, getPiccCmd);

        // Default value of Picc is 0xff
        assertThatResponseBytes(response)
                .isEqualTo("90 FF");
    }

    @Test
    void can_update_picc() {
        // Change PICC to 0xCC
        CommandAPDU changePiccCmd = cmd("FF 00 51 CC 00");

        ResponseAPDU response = execute(card, changePiccCmd);
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");

        // Read PICC after change
        CommandAPDU getPiccCmd = cmd("FF 00 50 00 00");

        response = execute(card, getPiccCmd);
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");
    }

    @Test
    void returns_failure_for_led_and_buzzer_commands() {
        CommandAPDU ledBuzzCmd = cmd("FF 00 40 0F 04 02 01 01 01");

        ResponseAPDU response = execute(card, ledBuzzCmd);
        assertThatResponseBytes(response)
                // 0x63 == failure
                .isEqualTo(RESP_ERR);
    }

    @Test
    void can_set_timeout() {
        CommandAPDU setTimeoutCmd = cmd("FF 00 41 08 00");

        ResponseAPDU response = execute(card, setTimeoutCmd);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Test
    void can_set_buzz_during_card_detection() {
        CommandAPDU ledBuzzCmd = cmd("FF 00 52 00 00");

        ResponseAPDU response = execute(card, ledBuzzCmd);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class write_data_to_card {
        // Card has default transport configuration
        FakeCard writableCard = new FakeCard();

        @Test
        @Order(10)
        void can_load_keyA_into_register_1() {
            CommandAPDU loadKey0Cmd = cmdLoadKeyToReg(REGISTER_1, FF_FF_FF_FF_FF_FF);

            ResponseAPDU response = execute(card, loadKey0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(20)
        void authenticate_using_keyA_in_reg_1_to_sector_0() {
            // Authenticate to block 0
            CommandAPDU authBlock0Cmd = cmdAuthenticateToBlockNumber(0, KEY_A, REGISTER_1);

            ResponseAPDU response = execute(card, authBlock0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(30)
        void read_all_data_from_sector_0() {
            String expectedBlockData =
                    "00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00";

            assertCanReadBlock(card, 0, expectedBlockData);
            assertCanReadBlock(card, 1, expectedBlockData);
            assertCanReadBlock(card, 2, expectedBlockData);

            // Notice keyA is zero'ed, access bits in transport configuration
            String expectedTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";
            assertCanReadBlock(card, 3, expectedTrailerBlock);
        }

        @Test
        @Order(40)
        void cannot_write_data_to_manufacturer_data_section() {
            CommandAPDU writeCmd = cmdWriteToBlockNumber(0,
                    "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");

            ResponseAPDU response = execute(card, writeCmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Check data not changed
            assertCanReadBlock(card, 0, ZERO_BLOCK);
        }

        @Test
        @Order(50)
        void can_write_data_to_card() throws CardException {
            writeData(card, 1, "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
            assertCanReadBlock(card, 1, "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class block_authentication_flow {
        // Card has default transport configuration
        FakeCard card = new FakeCard(); // Shadow parent, ugly meh...

        @Test
        @Order(10)
        void can_load_keys_into_registers() throws CardException {
            CommandAPDU loadKeyCmd = cmdLoadKeyToReg(REGISTER_0, AA_AA_AA_AA_AA_AA);
            ResponseAPDU response = execute(card, loadKeyCmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            loadKeyCmd = cmdLoadKeyToReg(REGISTER_1, FF_FF_FF_FF_FF_FF);
            response = execute(card, loadKeyCmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(20)
        void authenticate_with_wrong_and_right_key() {
            // Authenticate to block 0 using AA key
            CommandAPDU authBlock0Cmd = cmdAuthenticateToBlockNumber(0, KEY_A, REGISTER_0);

            ResponseAPDU response = execute(card, authBlock0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Authenticate to block 0 using FF key
            authBlock0Cmd = cmdAuthenticateToBlockNumber(0, KEY_A, REGISTER_1);
            response = execute(card, authBlock0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(30)
        void change_permissions_to_data_row_to_rw_by_keyB_and_set_keyB() throws CardException {
            // Data in sector 1 readable by Key B only
            // and keyA = FF:::FF, keyB = AA:BB:CC:DD:EE:FF
            CommandAPDU setSectorTrailer = cmdWriteToBlockNumber(3, "FF FF FF FF FF FF DF 05 A2 69 AA BB CC DD EE FF");

            ResponseAPDU response = execute(card, setSectorTrailer);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(35)
        void load_new_keys_into_registers() throws CardException {
            CommandAPDU loadKeyCmd = cmdLoadKeyToReg(REGISTER_0, AA_AA_AA_AA_AA_AA);
            ResponseAPDU response = execute(card, loadKeyCmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            loadKeyCmd = cmdLoadKeyToReg(REGISTER_1, AA_BB_CC_DD_EE_FF);
            response = execute(card, loadKeyCmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(40)
        void data_cannot_be_read_using_keyA() throws CardException {
            // Authenticate to block 0 using FF key
            CommandAPDU authBlock0Cmd = cmdAuthenticateToBlockNumber(0, KEY_A, REGISTER_0);
            ResponseAPDU response = execute(card, authBlock0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            assertErrorWhenReadingBlock(card, 1);
        }

        @Test
        @Order(50)
        void data_can_be_read_using_keyB() {
            CommandAPDU authBlock0Cmd = cmdAuthenticateToBlockNumber(0, KEY_B, REGISTER_1);
            ResponseAPDU response = execute(card, authBlock0Cmd);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            assertCanReadBlock(card, 1, ZERO_BLOCK);
        }
    }

    static void assertCanReadBlock(Card card, int blockNumber, String expectedData) {
        CommandAPDU readCmd = cmd(String.format("FF B0 00 %02X 10", blockNumber));

        ResponseAPDU response = execute(card, readCmd);
        assertThatResponseBytes(response)
                .isEqualTo(expectedData + " 90 00");
    }

    static void assertErrorWhenReadingBlock(Card card, int blockNumber) {
        CommandAPDU readCmd = cmd(String.format("FF B0 00 %02X 10", blockNumber));

        ResponseAPDU response = execute(card, readCmd);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);
    }

    static void writeData(Card card, int blockNumber, String data) {
        CommandAPDU writeCmd = cmdWriteToBlockNumber(blockNumber, data);

        ResponseAPDU response = execute(card, writeCmd);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    static CommandAPDU cmd(String bytesHexString) {
        return new CommandAPDU(ByteArrays.fromHexString(bytesHexString));
    }

    static CommandAPDU cmdLoadKeyToReg(Register reg, String hexKey) {
        return cmd(String.format("FF 82 00 %02X 06 %s", (reg == Register.REGISTER_0 ? 0 : 1), hexKey));
    }

    static CommandAPDU cmdAuthenticateToBlockNumber(int blockNumber, KeyType keyType, Register reg) {
        return cmd(String.format(
                "FF 86 00 00 05 01 00 %02X %02X %02X",
                blockNumber,
                (keyType == KEY_A) ? 0x60 : 0x61,
                (reg == Register.REGISTER_0 ? 0 : 1)));
    }

    static CommandAPDU cmdWriteToBlockNumber(int blockNumber, String hexData) {
        return cmd(String.format("FF D6 00 %02X 10 %s", blockNumber, hexData));
    }

    static ResponseAPDU execute(Card card, CommandAPDU cmd) {
        try {
            return card
                    .getBasicChannel()
                    .transmit(cmd);
        } catch (CardException e) {
            throw new RuntimeException(e);
        }
    }

    static AbstractStringAssert<?> assertThatResponseBytes(ResponseAPDU r) {
        return assertThat(ByteArrays.toHexString(r.getBytes()));
    }
}