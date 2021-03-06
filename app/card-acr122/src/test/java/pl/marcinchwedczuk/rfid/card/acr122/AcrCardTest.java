package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.AccessBits;
import pl.marcinchwedczuk.rfid.card.commons.AccessLevel;
import pl.marcinchwedczuk.rfid.card.commons.DataBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.TrailerBlockAccess;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;
import pl.marcinchwedczuk.rfid.card.fake.FakeCardTerminal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.marcinchwedczuk.rfid.card.commons.KeyType.KEY_A;
import static pl.marcinchwedczuk.rfid.card.commons.KeyType.KEY_B;
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

        acrCard.writeData(block2, ByteArrays.fromHexString(
                "00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF"));

        byte[] data = acrCard.readData(block2, 16);
        assertThat(ByteArrays.toHexString(data))
                .isEqualTo("00 11 22 33 44 55 66 77 88 99 AA BB CC DD EE FF");
    }

    // TODO: Change API, trailer should not be readable as byte[]
    // to avoid changing CBITS by mistake.
    @Test
    void read_permissions_from_card() {
        Sector firstSector = Sector.of(0);
        DataAddress trailerBlockAddress = DataAddress.of(firstSector, Block.TRAILER);

        acrCard.loadKeyIntoRegister(defaultKey, REGISTER_0);
        acrCard.authenticateToSector(firstSector, KEY_A, REGISTER_0);

        byte[] trailerData = acrCard.readData(trailerBlockAddress, 16);
        TrailerBlock trailerBlock = new TrailerBlock(trailerData);

        // Key A is not readable in default configuration
        assertThat(trailerBlock.keyAHexString())
                .isEqualTo("00:00:00:00:00:00");
        assertThat(trailerBlock.keyBHexString())
                .isEqualTo("FF:FF:FF:FF:FF:FF");

        AccessBits accessBits = trailerBlock.accessBits;

        // Check data block permissions
        assertThat(accessBits.getDataBlockAccesses())
                .hasSize(3);

        DataBlockAccess block2Access = accessBits.dataBlockAccessForBlock(2);
        assertThat(block2Access.readAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);
        assertThat(block2Access.writeAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);
        assertThat(block2Access.incrementAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);
        assertThat(block2Access.otherOperationsAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);

        // Check trailer permissions
        TrailerBlockAccess trailerAccess = accessBits.trailerBlockAccess();
        assertThat(trailerAccess.readAccessToKeyA)
                .isEqualTo(AccessLevel.NEVER);
        assertThat(trailerAccess.writeAccessToKeyA)
                .isEqualTo(AccessLevel.KEY_A);

        assertThat(trailerAccess.readAccessToKeyB)
                .isEqualTo(AccessLevel.KEY_A);
        assertThat(trailerAccess.writeAccessToKeyB)
                .isEqualTo(AccessLevel.KEY_A);

        assertThat(trailerAccess.readAccessToAccessBits)
                .isEqualTo(AccessLevel.KEY_A);
        assertThat(trailerAccess.writeAccessToAccessBits)
                .isEqualTo(AccessLevel.KEY_A);
    }

    @Test
    void write_permissions_to_card() {
        Sector firstSector = Sector.of(3);
        DataAddress trailerBlockAddress = DataAddress.of(firstSector, Block.TRAILER);

        byte[] newKeyB = ByteArrays.of(1, 2, 3, 4, 5, 6);

        acrCard.loadKeyIntoRegister(defaultKey, REGISTER_0);
        acrCard.authenticateToSector(firstSector, KEY_A, REGISTER_0);

        TrailerBlock trailerBlock = new TrailerBlock();
        trailerBlock.setKeyA(defaultKey);
        trailerBlock.setKeyB(newKeyB);
        trailerBlock.accessBits = new AccessBits(
            DataBlockAccess.C000, DataBlockAccess.C100, DataBlockAccess.C110, TrailerBlockAccess.C100);

        acrCard.writeData(trailerBlockAddress, trailerBlock.toBytes());

        // Authenticate using keyB
        acrCard.loadKeyIntoRegister(newKeyB, REGISTER_0);
        acrCard.authenticateToSector(firstSector, KEY_B, REGISTER_0);

        trailerBlock = new TrailerBlock(acrCard.readData(trailerBlockAddress, 16));
        AccessBits accessBits = trailerBlock.accessBits;

        // Check data block permissions
        assertThat(accessBits.getDataBlockAccesses())
                .hasSize(3);

        DataBlockAccess block2Access = accessBits.dataBlockAccessForBlock(2);
        assertThat(block2Access.readAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);
        assertThat(block2Access.writeAccess)
                .isEqualTo(AccessLevel.KEY_B);
        assertThat(block2Access.incrementAccess)
                .isEqualTo(AccessLevel.KEY_B);
        assertThat(block2Access.otherOperationsAccess)
                .isEqualTo(AccessLevel.KEY_A_OR_B);

        // Check trailer permissions
        TrailerBlockAccess trailerAccess = accessBits.trailerBlockAccess();
        assertThat(trailerAccess.readAccessToKeyA)
                .isEqualTo(AccessLevel.NEVER);
        assertThat(trailerAccess.writeAccessToKeyA)
                .isEqualTo(AccessLevel.KEY_B);

        assertThat(trailerAccess.readAccessToKeyB)
                .isEqualTo(AccessLevel.NEVER);
        assertThat(trailerAccess.writeAccessToKeyB)
                .isEqualTo(AccessLevel.KEY_B);

        assertThat(trailerAccess.readAccessToAccessBits)
                .isEqualTo(AccessLevel.KEY_A_OR_B);
        assertThat(trailerAccess.writeAccessToAccessBits)
                .isEqualTo(AccessLevel.NEVER);
    }
}