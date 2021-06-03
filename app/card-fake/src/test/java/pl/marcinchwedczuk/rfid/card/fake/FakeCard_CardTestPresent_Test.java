package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.fake.impl.CardState;

import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.marcinchwedczuk.rfid.card.fake.impl.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.fake.impl.KeyType.KEY_B;
import static pl.marcinchwedczuk.rfid.card.fake.impl.Register.REGISTER_0;
import static pl.marcinchwedczuk.rfid.card.fake.impl.Register.REGISTER_1;

public class FakeCard_CardTestPresent_Test extends BaseFakeCardTest {
    // Card in JavaCardIO represents both Card and the Terminal device itself.
    // The only difference is the protocol used to connect to them.
    public FakeCard_CardTestPresent_Test() {
        super(new FakeCard("T=0", CardState.CARD_PRESENT));
    }

    @Override
    protected ResponseAPDU sendToCard(String commandBytes) throws CardException {
        return testUtil.sendToCard(commandBytes);
    }

    @Test
    void returns_proper_ATR() {
        byte[] atrBytes = card.getATR().getBytes();

        // Example ATR for MIFARE 1K cards
        String expected = "3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A";

        assertThatHexStringOf(atrBytes)
                .isEqualTo(expected);
    }

    @Nested
    class authentication_tests {
        @Nested
        class not_authenticated_yet {
            int blockNumber = testUtil.toBlockNumber(1, 2);

            @Test
            void cannot_read_data() {
                testUtil.assertCannotReadBlock(blockNumber);
            }

            @Test
            void cannot_write_data() {
                testUtil.assertCannotWriteDataToBlock(blockNumber);
            }

            @Test
            void authenticate_with_wrong_key_fails() {
                testUtil.loadKeyToRegister(REGISTER_0, AA_AA_AA_AA_AA_AA);

                ResponseAPDU response = testUtil.execAuthenticateToBlockNumber(0, KEY_A, REGISTER_0);
                assertThatResponseBytes(response)
                        .isEqualTo(RESP_ERR);
            }
        }

        @Nested
        class after_authenticating_with_default_key_to_sector0 {
            @BeforeEach
            void before() {
                // Card has the default transport configuration in the beginning.
                testUtil
                        .loadKeyToRegister(REGISTER_1, FF_FF_FF_FF_FF_FF)
                        .authenticateToBlockNumber(0, KEY_A, REGISTER_1);
            }

            @Test
            void can_read_data_from_all_blocks_of_sector_0() {
                testUtil
                        .assertBlockOnCardContainsData(0, BLOCK_DATA_ZEROS)
                        .assertBlockOnCardContainsData(1, BLOCK_DATA_ZEROS)
                        .assertBlockOnCardContainsData(2, BLOCK_DATA_ZEROS);

                // Notice keyA is zero'ed, access bits are in the transport configuration
                String expectedTrailerBlock = "00 00 00 00 00 00 FF 07 80 69 FF FF FF FF FF FF";
                testUtil.assertBlockOnCardContainsData(3, expectedTrailerBlock);
            }

            @Test
            void can_write_data_to_block1_of_sector0() {
                int blockNumber = testUtil.toBlockNumber(0, 1);
                testUtil.assertCanWriteDataToBlock(blockNumber, BLOCK_DATA_00_01_02___0F);
            }

            @Test
            void manufacturer_block_data_cannot_be_changed() {
                // Sector 0, block 0 is special
                testUtil.assertCannotWriteDataToBlock(0);
            }

            @Test
            void cannot_read_data_from_other_sectors() {
                int blockNumber = testUtil.toBlockNumber(7, 0);
                testUtil.assertCannotReadBlock(blockNumber);
            }

            @Test
            void cannot_write_data_to_other_sectors() {
                int blockNumber = testUtil.toBlockNumber(7, 0);
                testUtil.assertCannotWriteDataToBlock(blockNumber);
            }
        }
    }

    @Nested
    class card_with_data_permissions_data_readable_and_writable_by_keyB_only_and_keyB_is_AA___FF {
        final int blockNumber = testUtil.toBlockNumber(1, 1);
        final int trailerBlockNumber = testUtil.toBlockNumber(1, 3);

        @BeforeEach
        void before() {
            // Use default keys to change permissions:
            // Data in sector 1 readable by Key B only
            // and keyA = FF:::FF, keyB = AA:BB:CC:DD:EE:FF
            testUtil
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(trailerBlockNumber, KEY_A, REGISTER_0)
                    .writeBlock(trailerBlockNumber, "FF FF FF FF FF FF DF 05 A2 69 AA BB CC DD EE FF");

            // Load new keys into registers
            testUtil
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .loadKeyToRegister(REGISTER_1, AA_BB_CC_DD_EE_FF);
        }

        @Test
        void cannot_read_and_write_data_using_keyA() {
            testUtil
                    .authenticateToBlockNumber(blockNumber, KEY_A, REGISTER_0)
                    .assertCannotReadBlock(blockNumber)
                    .assertCannotWriteDataToBlock(blockNumber);
        }

        @Test
        void can_read_and_write_data_using_keyB() {
            testUtil
                    .authenticateToBlockNumber(blockNumber, KEY_B, REGISTER_1)
                    .assertBlockOnCardContainsData(blockNumber, BLOCK_DATA_ZEROS)
                    .assertCanWriteDataToBlock(blockNumber, BLOCK_DATA_00_01_02___0F);
        }
    }

    @Nested
    class access_bits_permissions_keys_writable_by_keyB_accessBits_not_writable {
        int sectorTrailerBlockNumber = testUtil.toBlockNumber(1, 3);

        @BeforeEach
        void before() {
            // Set configuration C100 - keys writable by keyB, not readable.
            // Access bits readable by both keys, never writable again.
            testUtil
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .loadKeyToRegister(REGISTER_1, AA_BB_CC_DD_EE_FF)
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0)
                    .writeBlock(sectorTrailerBlockNumber, FF_FF_FF_FF_FF_FF + " F7 8F 00 69 " + AA_BB_CC_DD_EE_FF);
        }

        @Test
        void can_read_trailer_via_keyA_but_keys_are_zeroed() {
            testUtil
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0)
                    .assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                            _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void can_read_trailer_via_keyB_but_keys_are_zeroed() {
            testUtil
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1)
                    .assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                            _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void cannot_change_keys_using_keyA() {
            testUtil.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_A, REGISTER_0);

            // attempt: change keyA = 00:::00 and keyB = FF:::FF
            ResponseAPDU response = testUtil.execWriteBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);
            assertThatResponseBytes(response)
                    .isEqualTo(RESP_ERR);

            // Check access bits not changed
            testUtil.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void can_change_keys_using_keyB() {
            testUtil.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);

            testUtil.writeBlock(sectorTrailerBlockNumber,
                    AA_AA_AA_AA_AA_AA + " F7 8F 00 69 " + FF_FF_FF_FF_FF_FF);

            // We need to re-authenticate now
            testUtil
                    .loadKeyToRegister(REGISTER_0, FF_FF_FF_FF_FF_FF)
                    .authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_0);

            // In C100 configuration both keys are not readable
            testUtil.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);
        }

        @Test
        void cannot_change_access_bits_even_with_keyB() {
            testUtil.authenticateToBlockNumber(sectorTrailerBlockNumber, KEY_B, REGISTER_1);
            // Change back to default, remember C100 configuration prevents
            // any changes to access bits.
            testUtil.writeBlock(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " FF 07 80 69 " + FF_FF_FF_FF_FF_FF);

            // Check access bits not changed, keys zero'ed as usual
            testUtil.assertBlockOnCardContainsData(sectorTrailerBlockNumber,
                    _00_00_00_00_00_00 + " F7 8F 00 69 " + _00_00_00_00_00_00);

            // Info: experiments with real cards confirmed that even if bits are
            // invalid they get ignored.
        }

        @Test
        void writing_invalid_access_bits_returns_error() {
            // This may be not the real card behaviour - but it's useful
            // when testing GUI.
            int otherTrailer = testUtil.toBlockNumber(8, 3);

            // Use default key
            testUtil.authenticateToBlockNumber(otherTrailer, KEY_A, REGISTER_0);

            testUtil.assertCannotWriteDataToBlock(otherTrailer,
                    FF_FF_FF_FF_FF_FF + " 00 00 00 69 " + FF_FF_FF_FF_FF_FF);
        }
    }
}