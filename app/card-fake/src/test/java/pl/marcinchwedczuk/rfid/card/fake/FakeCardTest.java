package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.*;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;

import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_B;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_0;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_1;

// TODO: Refactor to test data builder
public class FakeCardTest {
    static final String _00_00_00_00_00_00 = "00 00 00 00 00 00";
    static final String FF_FF_FF_FF_FF_FF = "FF FF FF FF FF FF";
    static final String AA_BB_CC_DD_EE_FF = "AA BB CC DD EE FF";
    static final String AA_AA_AA_AA_AA_AA = "AA AA AA AA AA AA";

    static final String RESP_OK = "90 00";
    static final String RESP_ERR = "63 00";

    static final String BLOCK_DATA_ZEROS = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    static final String BLOCK_DATA_00_01_02___0F = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F";

    FakeCard card = new FakeCard();
    FakeMifare1kTestUtil cardWrapper = new FakeMifare1kTestUtil(card);

    @Test
    void returns_proper_ATR() {
        byte[] atrBytes = card.getATR().getBytes();

        // Example ATR for MIFARE 1K cards
        String expected = "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A";

        assertThatHexStringOf(atrBytes)
                .isEqualTo(expected);
    }

    @Test
    void returns_firmware_version() {
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 48 00 00");

        assertThat(response.getBytes())
                .asString()
                .isEqualTo("FAKE1.0");
    }

    @Test
    void returns_default_value_of_picc() {
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 50 00 00");

        // Default value of Picc is 0xff
        assertThatResponseBytes(response)
                .isEqualTo("90 FF");
    }

    @Test
    void can_update_picc() {
        // Change PICC to 0xCC
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 51 CC 00");
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");

        // Read PICC after change
        response = cardWrapper.executeCommand("FF 00 50 00 00");
        assertThatResponseBytes(response)
                .isEqualTo("90 CC");
    }

    @Test
    void returns_failure_for_led_and_buzzer_commands() {
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 40 0F 04 02 01 01 01");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);
    }

    @Test
    void can_set_timeout() {
        // Timeout is a no-op on fake card.
        // We only test that the command is being accepted.
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 41 08 00");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Test
    void can_set_buzz_during_card_detection() {
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 52 00 00");
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);
    }

    @Nested
    class write_data_to_card {
        // Card has the default transport configuration at test start

        // TODO: Authenticate without loading key?

        @Test
        void read_all_data_from_sector_0() {
            cardWrapper
                    .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(0, KEY_A, REGISTER_1);

            cardWrapper
                    .assertBlockOnCardContainsData(0, BLOCK_DATA_ZEROS)
                    .assertBlockOnCardContainsData(1, BLOCK_DATA_ZEROS)
                    .assertBlockOnCardContainsData(2, BLOCK_DATA_ZEROS);

            // Notice keyA is zero'ed, access bits are in the transport configuration
            String expectedTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";
            cardWrapper.assertBlockOnCardContainsData(3, expectedTrailerBlock);
        }

        @Test
        void cannot_write_data_to_manufacturer_block() {
            cardWrapper
                    .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(0, KEY_A, REGISTER_1);

            cardWrapper.assertCannotWriteData(0);
        }

        @Test
        void can_write_data_to_block1_of_sector0() {
            cardWrapper
                    .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(0, KEY_A, REGISTER_1);

            int blockNumber = cardWrapper.toBlockNumber(0, 1);
            cardWrapper.assertCanWriteData(blockNumber, BLOCK_DATA_00_01_02___0F);
        }

        @Test
        void cannot_write_data_to_block0_of_sector1_because_we_are_not_authenticated_to_it() {
            int blockNumber = cardWrapper.toBlockNumber(1, 0);
            cardWrapper.assertCannotWriteData(blockNumber);
        }

        @Test
        void after_authenticating_to_sector1_we_can_write_to_block0() {
            int blockNumber = cardWrapper.toBlockNumber(1, 0);

            cardWrapper
                    .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(blockNumber, KEY_A, REGISTER_1);

            cardWrapper.assertCanWriteData(blockNumber, BLOCK_DATA_00_01_02___0F);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class access_bits_and_keys_for_data_blocks {
        // TODO: Use stop at first failed test policy

        @Test
        void authenticate_with_wrong_and_right_key() {
            cardWrapper
                    .loadKeyToRegister(REGISTER_0, AA_AA_AA_AA_AA_AA)
                    .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF);

            // Authenticate to block 0 using AA key
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(0, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Authenticate to block 0 using FF key
            response = cardWrapper.execAuthenticateToBlockNumber(0, KEY_A, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Nested
        class data_readable_by_keyB {
            int blockNumber = cardWrapper.toBlockNumber(0, 1);

            @BeforeEach
            void before() {
                // Use default keys to change permissions:
                // Data in sector 1 readable by Key B only
                // and keyA = FF:::FF, keyB = AA:BB:CC:DD:EE:FF
                int trailerBlockNumber = cardWrapper.toBlockNumber(0, 3);
                cardWrapper
                        .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                        .authenticateToBlockNumber(trailerBlockNumber, KEY_A, REGISTER_0)
                        .writeBlock(trailerBlockNumber, "FF FF FF FF FF FF DF 05 A2 69 AA BB CC DD EE FF");

                // Load new keys into registers
                cardWrapper
                        .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                        .loadKeyToRegister(REGISTER_1, AA_BB_CC_DD_EE_FF);
            }

            @Test
            void cannot_read_data_from_sector0_using_keyA() {
                cardWrapper
                        .authenticateToBlockNumber(blockNumber, KEY_A, REGISTER_0)
                        .assertErrorWhenReadingBlock(blockNumber);
            }

            @Test
            void can_read_data_from_sector0_using_keyB() {
                cardWrapper
                        .authenticateToBlockNumber(blockNumber, KEY_B, REGISTER_1)
                        .assertBlockOnCardContainsData(blockNumber, BLOCK_DATA_ZEROS);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    class access_bits_and_keys_for_sector_trailer {
        int sectorTrailerBlockNumber = cardWrapper.toBlockNumber(1, 3);

        @Test
        @Order(10)
        void can_load_keys_into_registers() {
            ResponseAPDU response = cardWrapper.execLoadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            response = cardWrapper.execLoadKeyToRegister(REGISTER_1, AA_BB_CC_DD_EE_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(20)
        void authenticate_with_default_FF_key() {
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);
        }

        @Test
        @Order(25)
        void change_access_bits_trailer_writable_by_keyB() {
            // Set configuration C100 - keys writable by keyB, not readable.
            // Access bits readable by both keys, never writable again.
            ResponseAPDU response = cardWrapper.execWriteBlock(sectorTrailerBlockNumber,
                    FF_FF_FF_FF_FF_FF + " F7 8F 00 69 " + AA_BB_CC_DD_EE_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            // Notice keys are not readable
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        @Order(30)
        void can_read_trailer_via_keyA_but_keys_are_zeroed() {
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        @Order(35)
        void can_read_trailer_via_keyB_but_keys_are_zeroed() {
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        @Order(40)
        void cannot_change_keys_using_keyA() {
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            // attempt: change keyA = 00:::00 and keyB = FF:::FF
            response = cardWrapper.execWriteBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        @Order(50)
        void can_change_keys_using_keyB() {
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            response = cardWrapper.execWriteBlock(sectorTrailerBlockNumber,
                    AA_AA_AA_AA_AA_AA + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            // We need to re-authenticate now
            response = cardWrapper.execAuthenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            // In C100 configuration both keys are not readable
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        @Order(60)
        void cannot_change_access_bits_even_with_keyB() {
            // Change back to default, remember C100 configuration prevents
            // any changes to access bits.
            ResponseAPDU response = cardWrapper.execWriteBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " FF 07 80 69 " + FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            // Check access bits not changed, keys zero'ed as usual
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);

            // Info: experiments with real cards confirmed that even if bits are
            // invalid they get ignored.
        }

        @Test
        @Order(70)
        void writing_invalid_access_bits_returns_error() {
            // This may be not the real card behaviour - but it's useful
            // when testing GUI
            int otherTrailer = cardWrapper.toBlockNumber(8, 3);

            // Use default key
            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(otherTrailer, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_OK);

            cardWrapper.assertCannotWriteData(otherTrailer,
                    FF_FF_FF_FF_FF_FF + " 00 00 00 69 " + FF_FF_FF_FF_FF_FF);
        }
    }

    static AbstractStringAssert<?> assertThatResponseBytes(ResponseAPDU r) {
        return assertThatHexStringOf(r.getBytes());
    }

    static AbstractStringAssert<?> assertThatHexStringOf(byte[] bytes) {
        return assertThat(ByteArrays.toHexString(bytes));
    }
}