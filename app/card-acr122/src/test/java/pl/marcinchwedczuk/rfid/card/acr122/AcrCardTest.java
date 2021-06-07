package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.marcinchwedczuk.rfid.card.commons.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.commons.Register.REGISTER_0;

class AcrCardTest {
    byte[] defaultKey = ByteArrays.fromHexString("FF FF FF FF FF FF");

    AcrTerminal acrTerminal;
    AcrCard acrCard;

    @BeforeEach
    void before() {
        this.acrTerminal = new AcrTerminal(FakeCardTerminal.withCardPresent());
        this.acrCard = acrTerminal.connect();
    }

    @AfterEach
    void after() {
        this.acrCard.disconnect();
        this.acrCard = null;
    }

    @Test
    void terminal_returns_terminal_associated_with_card() {
        AcrTerminal returned = acrCard.terminal();
        assertThat(returned)
                .isSameAs(acrTerminal);
    }

    @Test
    void atrInfo_returns_atr_information() {
        AtrInfo info = acrCard.atrInfo();
        assertThat(info)
                .isNotNull();

        assertThat(info.cardName)
                .isEqualTo(CardName.NXP_MIFARE_STANDARD_1K);

        assertThat(info.cardStandard)
                .isEqualTo(CardStandard.ISO_14443A_LEVEL34);
    }

    @Test
    void getCardUID_returns_card_UID() {
        String cardID = acrCard.getCardUID();
        assertThat(cardID)
                .isEqualTo("AA:BB:CC:DD");
    }

    @Test
    void read_data_from_card() {
        Sector firstSector = Sector.of(0);

        acrCard.loadKeyIntoRegister(defaultKey, REGISTER_0);
        acrCard.authenticateToSector(firstSector, KEY_A, REGISTER_0);

        byte[] data = acrCard.readData(
                DataAddress.of(firstSector, Block.BLOCK_0), 16);

        // We read manufacturer data and card ID from fake card
        assertThat(ByteArrays.toHexString(data))
                .isEqualTo("AA BB CC DD D7 08 04 00 01 6F 01 6D 45 68 F8 1D");
    }

    @Test
    void write_data_to_card() {
        Sector firstSector = Sector.of(0);
        DataAddress block2 = DataAddress.of(firstSector, Block.BLOCK_2);

        acrCard.loadKeyIntoRegister(defaultKey, REGISTER_0);
        acrCard.authenticateToSector(firstSector, KEY_A, REGISTER_0);

        acrCard.writeBinaryBlock(block2, ByteArrays.fromHexString(
                "00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF"));

        byte[] data = acrCard.readData(block2, 16);
        assertThat(ByteArrays.toHexString(data))
                .isEqualTo("00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF");
    }
}