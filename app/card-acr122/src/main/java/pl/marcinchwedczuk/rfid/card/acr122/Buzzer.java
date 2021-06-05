package pl.marcinchwedczuk.rfid.card.acr122;

public enum Buzzer {
    BUZZER_DISABLED,
    BUZZER_DURING_T1,
    BUZZER_DURING_T2,
    BUZZER_DURING_T1_T2;

    public int toUnsignedByte() {
        return toControlByte() & 0xFF;
    }

    public byte toControlByte() {
        switch (this) {
            case BUZZER_DISABLED:
                return 0x00;
            case BUZZER_DURING_T1:
                return 0x01;
            case BUZZER_DURING_T2:
                return 0x02;
            case BUZZER_DURING_T1_T2:
                return 0x03;
            default:
                throw new AssertionError();
        }
    }

    public static Buzzer fromControlByte(byte b) {
        switch (b) {
            case 0x00:
                return BUZZER_DISABLED;
            case 0x01:
                return BUZZER_DURING_T1;
            case 0x02:
                return BUZZER_DURING_T2;
            case 0x03:
                return BUZZER_DURING_T1_T2;
            default:
                throw new IllegalArgumentException("b");
        }
    }
}
