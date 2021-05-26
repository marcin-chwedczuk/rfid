package pl.marcinchwedczuk.rfid.card.acr122;

public enum CardStandard {
    NO_INFORMATION(0x00, "No information given"),

    ISO_14443A_LEVEL1(0x01, "ISO 14443 A, level 1"),
    ISO_14443A_LEVEL2(0x02, "ISO 14443 A, level 2"),
    ISO_14443A_LEVEL34(0x03, "ISO 14443 A, level 3 or 4 (and Mifare)"),

    ISO_14443B_LEVEL1(0x05, "ISO 14443 B, level 1"),
    ISO_14443B_LEVEL2(0x06, "ISO 14443 B, level 2"),
    ISO_14443B_LEVEL34(0x06, "ISO 14443 B, level 3 or 4"),

    ICODE_1(0x09, "ICODE 1"),
    ISO_15693(0x0b, "ISO 15693")
    ;

    private int pixSS;
    private String description;

    CardStandard(int pixSS, String description) {
        this.pixSS = pixSS;
        this.description = description;
    }

    public String readableName() {
        return description;
    }

    public static CardStandard fromPixSS(int pixSS) {
        for (CardStandard cardStandard: CardStandard.values()) {
            if (cardStandard.pixSS == pixSS) {
                return cardStandard;
            }
        }

        throw new AcrException(String.format(
                "Unrecognized card standard with pixSS: 0x%02x", pixSS));
    }
}
