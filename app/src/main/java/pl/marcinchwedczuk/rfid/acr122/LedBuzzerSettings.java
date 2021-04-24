package pl.marcinchwedczuk.rfid.acr122;

public class LedBuzzerSettings {
    private LedSettings ledSettings;

    private int T1 = 10;
    private int T2 = 10;
    private int numberOfRepetitions;
    private Buzzer linkToBuzzer;

    private LedBuzzerSettings(byte ledState, byte[] blinkingDurationControl) {
        ledSettings = LedSettings.ofControlByte(ledState);

        if (blinkingDurationControl.length != 4) {
            throw new IllegalArgumentException("blinkingDurationControl");
        }

        T1 = blinkingDurationControl[0];
        T2 = blinkingDurationControl[1];
        numberOfRepetitions = blinkingDurationControl[2];
        linkToBuzzer = Buzzer.fromControlByte(blinkingDurationControl[3]);
    }

    public enum Buzzer {
        BUZZER_DISABLED,
        BUZZER_DURING_T1,
        BUZZER_DURING_T2,
        BUZZER_DURING_T1_T2;

        public byte toControlByte() {
            switch (this) {
                case BUZZER_DISABLED: return 0x00;
                case BUZZER_DURING_T1: return 0x01;
                case BUZZER_DURING_T2: return 0x02;
                case BUZZER_DURING_T1_T2: return 0x03;
                default: throw new AssertionError();
            }
        }

        public static Buzzer fromControlByte(byte b) {
            switch (b) {
                case 0x00: return BUZZER_DISABLED;
                case 0x01: return BUZZER_DURING_T1;
                case 0x02: return BUZZER_DURING_T2;
                case 0x03: return BUZZER_DURING_T1_T2;
                default: throw new IllegalArgumentException("b");
            }
        }
    }
}
