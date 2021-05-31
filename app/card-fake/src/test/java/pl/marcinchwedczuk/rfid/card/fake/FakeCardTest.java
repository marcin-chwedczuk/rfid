package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.*;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;

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

// TODO: Refactor to test data builder
public class FakeCardTest {
    static final String FF_FF_FF_FF_FF_FF = "FF FF FF FF FF FF";
    static final String AA_BB_CC_DD_EE_FF = "AA BB CC DD EE FF";
    static final String AA_AA_AA_AA_AA_AA = "AA AA AA AA AA AA";

    static final String RESP_OK = "90 00";
    static final String RESP_ERR = "63 00";

    static final String BLOCK_DATA_ZEROS = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    static final String BLOCK_DATA_00_01_02___0F = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F";

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
        // Card has the default transport configuration at test start

        @Test
        @Order(10)
        void can_load_keyA_into_register_1() {
            ResponseAPDU response = execLoadKeyToRegister(card, REGISTER_1, FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(20)
        void authenticate_using_keyA_in_reg_1_to_sector_0() {
            ResponseAPDU response = execAuthenticateToBlockNumber(card, 0, KEY_A, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(30)
        void read_all_data_from_sector_0() {
            assertBlockOnCardContainsData(card, 0, BLOCK_DATA_ZEROS);
            assertBlockOnCardContainsData(card, 1, BLOCK_DATA_ZEROS);
            assertBlockOnCardContainsData(card, 2, BLOCK_DATA_ZEROS);

            // Notice keyA is zero'ed, access bits are in the transport configuration
            String expectedTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";
            assertBlockOnCardContainsData(card, 3, expectedTrailerBlock);
        }

        @Test
        @Order(40)
        void cannot_write_data_to_manufacturer_block() {
            assertCannotWriteData(card, 0);
        }

        @Test
        @Order(50)
        void can_write_data_to_block1_of_sector0() throws CardException {
            int blockNumber = sectorBlockToBlockNumber(0, 1);
            assertCanWriteData(card, blockNumber, BLOCK_DATA_00_01_02___0F);
        }

        @Test
        @Order(60)
        void cannot_write_data_to_block0_of_sector1_because_we_are_not_authenticated_to_it() {
            int blockNumber = sectorBlockToBlockNumber(1, 0);
            assertCannotWriteData(card, blockNumber);
        }

        @Test
        @Order(70)
        void after_authenticating_to_sector1_we_can_write_to_block0() {
            int blockNumber = sectorBlockToBlockNumber(1, 0);

            ResponseAPDU response = execAuthenticateToBlockNumber(card, blockNumber, KEY_A, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            assertCanWriteData(card, blockNumber, BLOCK_DATA_00_01_02___0F);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class access_bits_and_keys_for_data_blocks {
        @Test
        @Order(10)
        void can_load_keys_into_registers() throws CardException {
            ResponseAPDU response = execLoadKeyToRegister(card, REGISTER_0, AA_AA_AA_AA_AA_AA);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            response = execLoadKeyToRegister(card, REGISTER_1, FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(20)
        void authenticate_with_wrong_and_right_key() {
            // Authenticate to block 0 using AA key
            ResponseAPDU response = execAuthenticateToBlockNumber(card, 0, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Authenticate to block 0 using FF key
            response = execAuthenticateToBlockNumber(card, 0, KEY_A, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(30)
        void change_permissions_to_block0_to__rw_by_keyB__and_change_keyB() throws CardException {
            // Data in sector 1 readable by Key B only
            // and keyA = FF:::FF, keyB = AA:BB:CC:DD:EE:FF
            ResponseAPDU response = execWriteBlock(
                    card,
                    sectorBlockToBlockNumber(0, 3),
                    "FF FF FF FF FF FF DF 05 A2 69 AA BB CC DD EE FF");

            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(35)
        void load_new_keys_into_registers() throws CardException {
            ResponseAPDU response =  execLoadKeyToRegister(card, REGISTER_0, AA_AA_AA_AA_AA_AA);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            response = execLoadKeyToRegister(card, REGISTER_1, AA_BB_CC_DD_EE_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(40)
        void cannot_read_data_using_keyA_from_sector0() throws CardException {
            int blockNumber = sectorBlockToBlockNumber(0, 1);

            // Authenticate to block 0 using FF key
            ResponseAPDU response = execAuthenticateToBlockNumber(card, blockNumber, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            assertErrorWhenReadingBlock(card, blockNumber);
        }

        @Test
        @Order(50)
        void can_read_data_using_keyB_from_sector0() {
            int blockNumber = sectorBlockToBlockNumber(0, 1);

            ResponseAPDU response = execAuthenticateToBlockNumber(card, blockNumber, KEY_B, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            assertBlockOnCardContainsData(card, blockNumber, BLOCK_DATA_ZEROS);
        }
    }

    // TODO: Auth tests for sector trailer

    static void assertBlockOnCardContainsData(Card card, int blockNumber, String expectedData) {
        ResponseAPDU response = execReadBlock(card, blockNumber);
        assertThatResponseBytes(response)
                .isEqualTo(expectedData + " 90 00");
    }

    static void assertErrorWhenReadingBlock(Card card, int blockNumber) {
        ResponseAPDU response = execReadBlock(card, blockNumber);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);
    }

    static void assertCanWriteData(Card card, int blockNumber, String data) {
        ResponseAPDU response = execWriteBlock(card, blockNumber, data);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);

        // Check data was actually written
        assertBlockOnCardContainsData(card, blockNumber, data);
    }

    static void assertCannotWriteData(Card card, int blockNumber) {
        ResponseAPDU dataBeforeWrite = execReadBlock(card, blockNumber);

        // Write unique "random" bytes
        ResponseAPDU response = execWriteBlock(card, blockNumber,
                "DE AD BE EF FF FF FF FF 01 02 03 04 CA FE BA BE");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);

        ResponseAPDU dataAfterFailedWrite = execReadBlock(card, blockNumber);

        // Check data not changed
        assertThatResponseBytes(dataAfterFailedWrite)
                .isEqualTo(ByteArrays.toHexString(dataBeforeWrite.getBytes()));
    }

    static CommandAPDU cmd(String bytesHexString) {
        return new CommandAPDU(ByteArrays.fromHexString(bytesHexString));
    }

    static CommandAPDU cmdLoadKeyToReg(Register reg, String hexKey) {
        return cmd(String.format("FF 82 00 %02X 06 %s", (reg == Register.REGISTER_0 ? 0 : 1), hexKey));
    }

    static ResponseAPDU execLoadKeyToRegister(Card card, Register reg, String key) {
        CommandAPDU cmd = cmd(String.format("FF 82 00 %02X 06 %s", (reg == Register.REGISTER_0 ? 0 : 1), key));
        ResponseAPDU response = execute(card, cmd);
        return response;
    }

    static CommandAPDU cmdAuthenticateToBlockNumber(int blockNumber, KeyType keyType, Register reg) {
        return cmd(String.format(
                "FF 86 00 00 05 01 00 %02X %02X %02X",
                blockNumber,
                (keyType == KEY_A) ? 0x60 : 0x61,
                (reg == Register.REGISTER_0 ? 0 : 1)));
    }

    static ResponseAPDU execAuthenticateToBlockNumber(Card card, int blockNumber, KeyType keyType, Register reg) {
        CommandAPDU cmd = cmd(String.format(
                "FF 86 00 00 05 01 00 %02X %02X %02X",
                blockNumber,
                (keyType == KEY_A) ? 0x60 : 0x61,
                (reg == Register.REGISTER_0 ? 0 : 1)));

        ResponseAPDU response = execute(card, cmd);
        return response;
    }

    static CommandAPDU cmdWriteToBlockNumber(int blockNumber, String hexData) {
        return cmd(String.format("FF D6 00 %02X 10 %s", blockNumber, hexData));
    }

    static ResponseAPDU execReadBlock(Card card, int blockNumber) {
        CommandAPDU readCmd = cmd(String.format("FF B0 00 %02X 10", blockNumber));
        ResponseAPDU response = execute(card, readCmd);
        return response;
    }

    static ResponseAPDU execWriteBlock(Card card, int blockNumber, String hexData) {
        CommandAPDU writeCmd = cmd(String.format("FF D6 00 %02X 10 %s", blockNumber, hexData));
        ResponseAPDU response = execute(card, writeCmd);
        return response;
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

    static int sectorBlockToBlockNumber(int sector, int block) {
        return 4*sector + block;
    }
}