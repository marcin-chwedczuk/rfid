package pl.marcinchwedczuk.rfid.card.acr122;

public enum CardName {
    NXP_MIFARE_STANDARD_1K(0x0001, "NXP Mifare Standard 1k"),
    NXP_MIFARE_STANDARD_4K(0x0002, "NXP Mifare Standard 4k"),
    NXP_MIFARE_ULTRALIGHT(0x0003, "NXP Mifare UltraLight"),
    ST_MICROELECTRONICS_SR176(0x0006, "ST MicroElectronics SR176"),
    ST_MICROELECTRONICS_SRI4K_SRIX4K_SRIX512_SRI512_SRT512_(0x0007, "ST MicroElectronics SRI4K, SRIX4K, SRIX512, SRI512, SRT512"),
    ATMEL_AT88SC0808CRF(0x000A, "Atmel AT88SC0808CRF"),
    ATMEL_AT88SC1616CRF(0x000B, "Atmel AT88SC1616CRF"),
    ATMEL_AT88SC3216CRF(0x000C, "Atmel AT88SC3216CRF"),
    ATMEL_AT88SC6416CRF(0x000D, "Atmel AT88SC6416CRF"),
    TEXAS_INTRUMENTS_TAG_IT(0x0012, "Texas Intruments TAG IT"),
    ST_MICROELECTRONICS_LRI512(0x0013, "ST MicroElectronics LRI512"),
    NXP_ICODE_SLI(0x0014, "NXP ICODE SLI"),
    NXP_ICODE1(0x0016, "NXP ICODE1"),
    ST_MICROELECTRONICS_LRI64(0x0021, "ST MicroElectronics LRI64"),
    ST_MICROELECTRONICS_LR12(0x0024, "ST MicroElectronics LR12"),
    ST_MICROELECTRONICS_LRI128(0x0025, "ST MicroElectronics LRI128"),
    NXP_MIFARE_MINI(0x0026, "NXP Mifare Mini"),
    INNOVISION_JEWEL(0x002F, "Innovision Jewel"),
    INNOVISION_TOPAZ(0x0030, "Innovision Topaz"),
    ATMEL_AT88RF04C(0x0034, "Atmel AT88RF04C"),
    NXP_ICODE_SL2(0x0035, "NXP ICODE SL2"),
    NXP_MIFARE_ULTRALIGHT_C(0x003A, "NXP Mifare UltraLight C");

    private final int pixNN;
    private final String cardName;

    CardName(int pixNN, String cardName) {
        this.pixNN = pixNN;
        this.cardName = cardName;
    }

    public String readableName() {
        return cardName;
    }

    public static CardName fromPixNN(int pixNN) {
        for (CardName value: CardName.values()) {
            if (value.pixNN == pixNN) {
                return value;
            }
        }

        throw new AcrException(String.format(
                "Unrecognized card name with pixNN: 0x%04x", pixNN));
    }
}
