package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;

import javax.smartcardio.ResponseAPDU;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_B;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_0;
import static pl.marcinchwedczuk.rfid.card.fake.Register.REGISTER_1;

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
    void returns_current_picc() {
        ResponseAPDU response = cardWrapper.executeCommand("FF 00 50 00 00");

        // Default value of Picc is 0xff
        assertThatResponseBytes(response)
                .isEqualTo("90 FF");
    }

    @Test
    void can_change_picc() {
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
    class card_data {
        @Nested
        class without_authenticating {
            int blockNumber = cardWrapper.toBlockNumber(1, 2);

            @Test
            void cannot_read_data() {
                cardWrapper.assertErrorWhenReadingBlock(blockNumber);
            }

            @Test
            void cannot_write_data() {
                cardWrapper.assertCannotWriteDataToBlock(blockNumber);
            }
        }

        @Nested
        class after_authenticating_with_default_key_to_sector0 {
            @BeforeEach
            void before() {
                // Card has the default transport configuration in the beginning.
                cardWrapper
                        .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                        .authenticateToBlockNumber(0, KEY_A, REGISTER_1);
            }

            @Test
            void can_read_data_from_all_blocks_of_sector_0() {
                cardWrapper
                        .assertBlockOnCardContainsData(0, BLOCK_DATA_ZEROS)
                        .assertBlockOnCardContainsData(1, BLOCK_DATA_ZEROS)
                        .assertBlockOnCardContainsData(2, BLOCK_DATA_ZEROS);

                // Notice keyA is zero'ed, access bits are in the transport configuration
                String expectedTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";
                cardWrapper.assertBlockOnCardContainsData(3, expectedTrailerBlock);
            }

            @Test
            void can_write_data_to_block1_of_sector0() {
                int blockNumber = cardWrapper.toBlockNumber(0, 1);
                cardWrapper.assertCanWriteDataToBlock(blockNumber, BLOCK_DATA_00_01_02___0F);
            }

            @Test
            void manufacturer_block_data_cannot_be_changed() {
                // Sector 0, block 0 is special
                cardWrapper.assertCannotWriteDataToBlock(0);
            }

            @Test
            void cannot_read_data_from_other_sectors() {
                int blockNumber = cardWrapper.toBlockNumber(7, 0);
                cardWrapper.assertErrorWhenReadingBlock(blockNumber);
            }

            @Test
            void cannot_write_data_to_other_sectors() {
                int blockNumber = cardWrapper.toBlockNumber(7, 0);
                cardWrapper.assertCannotWriteDataToBlock(blockNumber);
            }
        }
    }

    @Nested
    class card_data_and_custom_access_bits {
        // Card is in default configuration at the beginning.

        @Test
        void authenticate_with_wrong_key_fails() {
            cardWrapper.loadKeyToRegister(REGISTER_0, AA_AA_AA_AA_AA_AA);

            ResponseAPDU response = cardWrapper.execAuthenticateToBlockNumber(0, KEY_A, REGISTER_0);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);
        }

        @Nested
        class data_readable_by_keyB_only {
            final int blockNumber = cardWrapper.toBlockNumber(1, 1);
            final int trailerBlockNumber = cardWrapper.toBlockNumber(1, 3);

            @BeforeEach
            void before() {
                // Use default keys to change permissions:
                // Data in sector 1 readable by Key B only
                // and keyA = FF:::FF, keyB = AA:BB:CC:DD:EE:FF
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
            void cannot_read_data_using_keyA() {
                cardWrapper
                        .authenticateToBlockNumber(blockNumber, KEY_A, REGISTER_0)
                        .assertErrorWhenReadingBlock(blockNumber);
            }

            @Test
            void can_read_data_using_keyB() {
                cardWrapper
                        .authenticateToBlockNumber(blockNumber, KEY_B, REGISTER_1)
                        .assertBlockOnCardContainsData(blockNumber, BLOCK_DATA_ZEROS);
            }
        }
    }

    @Nested
    class custom_access_bits_for_sector_trailer {
        int sectorTrailerBlockNumber = cardWrapper.toBlockNumber(1, 3);

        @BeforeEach
        void before() {
            // Set configuration C100 - keys writable by keyB, not readable.
            // Access bits readable by both keys, never writable again.
            cardWrapper
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .loadKeyToRegister(REGISTER_1, AA_BB_CC_DD_EE_FF)
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0)
                    .writeBlock(sectorTrailerBlockNumber, FF_FF_FF_FF_FF_FF + " F7 8F 00 69 " + AA_BB_CC_DD_EE_FF);
        }


        @Test
        void can_read_trailer_via_keyA_but_keys_are_zeroed() {
            cardWrapper
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0)
                    .assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                            _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void can_read_trailer_via_keyB_but_keys_are_zeroed() {
            cardWrapper
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1)
                    .assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                            _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void cannot_change_keys_using_keyA() {
            cardWrapper.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0);

            // attempt: change keyA = 00:::00 and keyB = FF:::FF
            ResponseAPDU response = cardWrapper.execWriteBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Check access bits not changed
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void can_change_keys_using_keyB() {
            cardWrapper.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);

            cardWrapper.writeBlock(sectorTrailerBlockNumber,
                    AA_AA_AA_AA_AA_AA + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);

            // We need to re-authenticate now
            cardWrapper
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_0);

            // In C100 configuration both keys are not readable
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void cannot_change_access_bits_even_with_keyB() {
            cardWrapper.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);
            // Change back to default, remember C100 configuration prevents
            // any changes to access bits.
            cardWrapper.writeBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " FF 07 80 69 " + FF_FF_FF_FF_FF_FF);

            // Check access bits not changed, keys zero'ed as usual
            cardWrapper.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);

            // Info: experiments with real cards confirmed that even if bits are
            // invalid they get ignored.
        }

        @Test
        void writing_invalid_access_bits_returns_error() {
            // This may be not the real card behaviour - but it's useful
            // when testing GUI
            int otherTrailer = cardWrapper.toBlockNumber(8, 3);

            // Use default key
            cardWrapper.authenticateToBlockNumber(otherTrailer, KEY_A, REGISTER_0);

            cardWrapper.assertCannotWriteDataToBlock(otherTrailer,
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