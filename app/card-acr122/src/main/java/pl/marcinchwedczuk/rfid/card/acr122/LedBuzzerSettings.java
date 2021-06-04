package pl.marcinchwedczuk.rfid.card.acr122;

public class LedBuzzerSettings {
    private LedSettings ledSettings;

    /**
     * Initial blinking state duration.
     */
    private int T1 = 10;

    /**
     * Toggle blinking state duration.
     */
    private int T2 = 10;
    private int numberOfRepetitions = 0;
    private Buzzer linkToBuzzer = Buzzer.BUZZER_DISABLED;

    // TODO: Add builder for this class
    public LedBuzzerSettings() {
        this.ledSettings = LedSettings.ofControlByte((byte) 0);
    }

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

    byte[] toControlBytes() {
        return new byte[]{
                (byte) T1,
                (byte) T2,
                (byte) numberOfRepetitions,
                linkToBuzzer.toControlByte()
        };
    }

    public LedSettings getLedSettings() {
        return ledSettings;
    }

    public int getT1() {
        return T1;
    }
    public void setT1(int t1) {
        T1 = t1;
    }

    public int getT2() {
        return T2;
    }
    public void setT2(int t2) {
        T2 = t2;
    }

    public int getNumberOfRepetitions() {
        return numberOfRepetitions;
    }
    public void setNumberOfRepetitions(int numberOfRepetitions) {
        this.numberOfRepetitions = numberOfRepetitions;
    }

    public Buzzer getLinkToBuzzer() {
        return linkToBuzzer;
    }
    public void setLinkToBuzzer(Buzzer linkToBuzzer) {
        this.linkToBuzzer = linkToBuzzer;
    }

}
