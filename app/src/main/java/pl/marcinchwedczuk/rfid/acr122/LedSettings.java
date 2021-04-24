package pl.marcinchwedczuk.rfid.acr122;

import pl.marcinchwedczuk.rfid.acr122.RichByte;

import static pl.marcinchwedczuk.rfid.acr122.LedSettings.LedBlinkingMask.BLINK;
import static pl.marcinchwedczuk.rfid.acr122.LedSettings.LedBlinkingMask.NOT_BLINK;
import static pl.marcinchwedczuk.rfid.acr122.LedSettings.LedState.OFF;
import static pl.marcinchwedczuk.rfid.acr122.LedSettings.LedState.ON;
import static pl.marcinchwedczuk.rfid.acr122.LedSettings.StateMask.NO_CHANGE;
import static pl.marcinchwedczuk.rfid.acr122.LedSettings.StateMask.UPDATE;

public class LedSettings {
    private LedState finalRedLED;
    private LedState finalGreenLED;

    private StateMask maskRedLED;
    private StateMask maskGreenLED;

    private LedState initialBlinkingRedLED;
    private LedState initialBlinkingGreenLED;

    private LedBlinkingMask blinkingMaskRedLED;
    private LedBlinkingMask blinkingMaskGreenLED;

    public LedSettings(byte b) {
        RichByte bits = new RichByte(b);

        finalRedLED = bits.isBitSet(0) ? ON : OFF;
        finalGreenLED = bits.isBitSet(1) ? ON : OFF;

        maskRedLED = bits.isBitSet(2) ? UPDATE : NO_CHANGE;
        maskGreenLED = bits.isBitSet(3) ? UPDATE : NO_CHANGE;

        initialBlinkingRedLED = bits.isBitSet(4) ? ON : OFF;
        initialBlinkingGreenLED = bits.isBitSet(5) ? ON : OFF;

        blinkingMaskRedLED = bits.isBitSet(6) ? BLINK : NOT_BLINK;
        blinkingMaskGreenLED = bits.isBitSet(7) ? BLINK : NOT_BLINK;
    }

    public static LedSettings ofControlByte(byte b) {
        return new LedSettings(b);
    }

    public enum LedState { ON, OFF }
    public enum StateMask { UPDATE, NO_CHANGE }
    public enum LedBlinkingMask { BLINK, NOT_BLINK }
}
