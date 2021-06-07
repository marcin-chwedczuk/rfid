package pl.marcinchwedczuk.rfid.card.acr122;

import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Objects;

/**
 * Request LED blinking and Buzzer buzzing.
 *
 * T1 and T2 define a square wave timings (__|--|__|--|__).
 * LEDs or Buzzer can be enabled only during T1 or T2 parts
 * of the cycle. The official unit for T1 and T2 is 100ms,
 * tests show that is can be anything between 100ms and 200ms.
 *
 * LED initial state is the LED state at the beginning of the "animation".
 * After square wave changes from low-to-high or high-to-low the
 * LED state will be negated (ON -> OFF, OFF -> ON).
 * For example if you start with initial blinking state RedLED = ON and
 * GreenLED = OFF, and T1 = T2 = 1, and NumberOfRepetitions = 2
 * then you will have RedLED enabled, then after 100ms GreenLED, then RedLED
 * again after another 100ms and GreenLED one more time after the last 100ms delay.
 *
 * The final LED state describes what should happen after "animation" ends.
 *
 * Best way to grok these settings is to use GUI and send some example commands
 * to the terminal.
 * It is advised to increase the timeout to at least the time of the animation,
 * because the send command is blocking and will not return until
 * animation ends.
 *
 * NOTE: Set number of repetitions to zero and only the final LED state
 * to control LED indicator.
 */
public class LedBuzzerSettings {
    public static LedBuzzerSettings newDefaults() {
        return new LedBuzzerSettings();
    }

    static LedBuzzerSettings parse(byte ledState, byte[] blinkingDurationControl) {
        return new LedBuzzerSettings(ledState, blinkingDurationControl);
    }

    private LedSettings ledSettings = LedSettings.newDefault();
    private TDuration T1 = TDuration.fromNumberOf100msUnits(1);
    private TDuration T2 = TDuration.fromNumberOf100msUnits(1);
    private Buzzer linkToBuzzer = Buzzer.BUZZER_DISABLED;
    private int numberOfRepetitions = 0;

    private LedBuzzerSettings() { }

    private LedBuzzerSettings(byte ledState, byte[] blinkingDurationControl) {
        ledSettings = LedSettings.ofControlByte(ledState);

        if (blinkingDurationControl.length != 4) {
            throw new IllegalArgumentException("blinkingDurationControl");
        }

        T1 = TDuration.fromNumberOf100msUnits(blinkingDurationControl[0]);
        T2 = TDuration.fromNumberOf100msUnits(blinkingDurationControl[1]);
        numberOfRepetitions = blinkingDurationControl[2];
        linkToBuzzer = Buzzer.fromControlByte(blinkingDurationControl[3]);
    }

    int[] toUnsignedControlBytes() {
        return new int[] {
            T1.toUnsignedByte(),
            T2.toUnsignedByte(),
            numberOfRepetitions,
            linkToBuzzer.toUnsignedByte()
        };
    }

    public LedSettings getLedSettings() {
        return ledSettings;
    }

    public TDuration getT1() {
        return T1;
    }
    public void setT1(TDuration t1) {
        T1 = Objects.requireNonNull(t1);
    }

    public TDuration getT2() {
        return T2;
    }
    public void setT2(TDuration t2) {
        T2 = Objects.requireNonNull(t2);
    }

    public int getNumberOfRepetitions() {
        return numberOfRepetitions;
    }
    public void setNumberOfRepetitions(int numberOfRepetitions) {
        if (numberOfRepetitions < 0 || numberOfRepetitions > 0xFF)
            throw new IllegalArgumentException("numberOfRepetitions");

        this.numberOfRepetitions = numberOfRepetitions;
    }

    public Buzzer getLinkToBuzzer() {
        return linkToBuzzer;
    }
    public void setLinkToBuzzer(Buzzer linkToBuzzer) {
        this.linkToBuzzer = Objects.requireNonNull(linkToBuzzer);
    }
}
