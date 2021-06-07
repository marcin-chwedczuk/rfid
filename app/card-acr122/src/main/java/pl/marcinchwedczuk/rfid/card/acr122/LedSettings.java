package pl.marcinchwedczuk.rfid.card.acr122;

import pl.marcinchwedczuk.rfid.card.acr122.impl.RichByte;

import java.util.Objects;

import static pl.marcinchwedczuk.rfid.card.acr122.LedBlinkingMask.BLINK;
import static pl.marcinchwedczuk.rfid.card.acr122.LedBlinkingMask.NOT_BLINK;
import static pl.marcinchwedczuk.rfid.card.acr122.LedState.OFF;
import static pl.marcinchwedczuk.rfid.card.acr122.LedState.ON;
import static pl.marcinchwedczuk.rfid.card.acr122.StateMask.NO_CHANGE;
import static pl.marcinchwedczuk.rfid.card.acr122.StateMask.UPDATE;

public class LedSettings {
    static LedSettings newDefault() {
        return ofControlByte((byte)0x00);
    }

    static LedSettings ofControlByte(byte b) {
        return new LedSettings(b);
    }

    private LedState finalRedLED;
    private LedState finalGreenLED;

    private StateMask maskRedLED;
    private StateMask maskGreenLED;

    private LedState initialBlinkingRedLED;
    private LedState initialBlinkingGreenLED;

    private LedBlinkingMask blinkingMaskRedLED;
    private LedBlinkingMask blinkingMaskGreenLED;

    LedSettings(byte b) {
        RichByte richByte = new RichByte(b);

        finalRedLED = richByte.isBitSet(0) ? ON : OFF;
        finalGreenLED = richByte.isBitSet(1) ? ON : OFF;

        maskRedLED = richByte.isBitSet(2) ? UPDATE : NO_CHANGE;
        maskGreenLED = richByte.isBitSet(3) ? UPDATE : NO_CHANGE;

        initialBlinkingRedLED = richByte.isBitSet(4) ? ON : OFF;
        initialBlinkingGreenLED = richByte.isBitSet(5) ? ON : OFF;

        blinkingMaskRedLED = richByte.isBitSet(6) ? BLINK : NOT_BLINK;
        blinkingMaskGreenLED = richByte.isBitSet(7) ? BLINK : NOT_BLINK;
    }

    int toUnsignedControlByte() {
        return toControlByte() & 0xFF;
    }

    byte toControlByte() {
        return new RichByte(0)
                .withBit(0, finalRedLED == ON)
                .withBit(1, finalGreenLED == ON)
                .withBit(2, maskRedLED == UPDATE)
                .withBit(3, maskGreenLED == UPDATE)
                .withBit(4, initialBlinkingRedLED == ON)
                .withBit(5, initialBlinkingGreenLED == ON)
                .withBit(6, blinkingMaskRedLED == BLINK)
                .withBit(7, blinkingMaskGreenLED == BLINK)
                .asByte();
    }

    public void setFinalRedLED(LedState finalRedLED) {
        this.finalRedLED = Objects.requireNonNull(finalRedLED);
    }

    public void setFinalGreenLED(LedState finalGreenLED) {
        this.finalGreenLED = Objects.requireNonNull(finalGreenLED);
    }

    public void setMaskRedLED(StateMask maskRedLED) {
        this.maskRedLED = Objects.requireNonNull(maskRedLED);
    }

    public void setMaskGreenLED(StateMask maskGreenLED) {
        this.maskGreenLED = Objects.requireNonNull(maskGreenLED);
    }

    public void setInitialBlinkingRedLED(LedState initialBlinkingRedLED) {
        this.initialBlinkingRedLED = Objects.requireNonNull(initialBlinkingRedLED);
    }

    public void setInitialBlinkingGreenLED(LedState initialBlinkingGreenLED) {
        this.initialBlinkingGreenLED = Objects.requireNonNull(initialBlinkingGreenLED);
    }

    public void setBlinkingMaskRedLED(LedBlinkingMask blinkingMaskRedLED) {
        this.blinkingMaskRedLED = Objects.requireNonNull(blinkingMaskRedLED);
    }

    public void setBlinkingMaskGreenLED(LedBlinkingMask blinkingMaskGreenLED) {
        this.blinkingMaskGreenLED = Objects.requireNonNull(blinkingMaskGreenLED);
    }
}

