package pl.marcinchwedczuk.rfid.card.acr122;

import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.EnableDisable.DISABLE;
import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.EnableDisable.ENABLE;
import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.PoolingInterval.INTERVAL_250_MILLIS;
import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.PoolingInterval.INTERVAL_500_MILLIS;
import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.SkipDetect.DETECT;
import static pl.marcinchwedczuk.rfid.card.acr122.PiccOperatingParameter.SkipDetect.SKIP;

public class PiccOperatingParameter {
    private EnableDisable autoPiccPolling;

    /**
     * To issue ATS (Answer To Select) Request whenever
     * an ISO14443-4 Type A tag is activated
     */
    private EnableDisable autoAtsGeneration;


    public enum PoolingInterval {
        INTERVAL_250_MILLIS,
        INTERVAL_500_MILLIS
    }

    private PoolingInterval pollingInterval;

    /* The Tag Types to be detected during PICC Polling. */
    private SkipDetect feliCa424K;
    private SkipDetect feliCa212K;
    private SkipDetect topaz;
    private SkipDetect iso14443TypeB;

    /**
     * To detect the Mifare Tags, the Auto ATS Generation must be disabled first.
     */
    private SkipDetect iso14443TypeA;

    public PiccOperatingParameter() {
        this((byte) 0);
    }

    private PiccOperatingParameter(byte bits) {
        RichByte richBits = new RichByte(bits);

        autoPiccPolling = richBits.isBitSet(7) ? ENABLE : DISABLE;
        autoAtsGeneration = richBits.isBitSet(6) ? ENABLE : DISABLE;
        pollingInterval = richBits.isBitSet(5)
                ? INTERVAL_250_MILLIS
                : INTERVAL_500_MILLIS;

        // TODO: richBits.mapBit(3, SkipDetect::fromBoolean)
        feliCa424K = richBits.isBitSet(4) ? DETECT : SKIP;
        feliCa212K = richBits.isBitSet(3) ? DETECT : SKIP;
        topaz = richBits.isBitSet(2) ? DETECT : SKIP;
        iso14443TypeB = richBits.isBitSet(1) ? DETECT : SKIP;
        iso14443TypeA = richBits.isBitSet(0) ? DETECT : SKIP;
    }

    public byte toByte() {
        return new RichByte(0)
                .withBit(7, autoPiccPolling == ENABLE)
                .withBit(6, autoAtsGeneration == ENABLE)
                .withBit(5, pollingInterval == INTERVAL_250_MILLIS)
                .withBit(4, feliCa424K == DETECT)
                .withBit(3, feliCa212K == DETECT)
                .withBit(2, topaz == DETECT)
                .withBit(1, iso14443TypeB == DETECT)
                .withBit(0, iso14443TypeA == DETECT)
                .asByte();
    }

    public EnableDisable getAutoPiccPolling() {
        return autoPiccPolling;
    }

    public void setAutoPiccPolling(EnableDisable autoPiccPolling) {
        this.autoPiccPolling = autoPiccPolling;
    }

    public EnableDisable getAutoAtsGeneration() {
        return autoAtsGeneration;
    }

    public void setAutoAtsGeneration(EnableDisable autoAtsGeneration) {
        this.autoAtsGeneration = autoAtsGeneration;
    }

    public PoolingInterval getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(PoolingInterval pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public SkipDetect getFeliCa424K() {
        return feliCa424K;
    }

    public void setFeliCa424K(SkipDetect feliCa424K) {
        this.feliCa424K = feliCa424K;
    }

    public SkipDetect getFeliCa212K() {
        return feliCa212K;
    }

    public void setFeliCa212K(SkipDetect feliCa212K) {
        this.feliCa212K = feliCa212K;
    }

    public SkipDetect getTopaz() {
        return topaz;
    }

    public void setTopaz(SkipDetect topaz) {
        this.topaz = topaz;
    }

    public SkipDetect getIso14443TypeB() {
        return iso14443TypeB;
    }

    public void setIso14443TypeB(SkipDetect iso14443TypeB) {
        this.iso14443TypeB = iso14443TypeB;
    }

    public SkipDetect getIso14443TypeA() {
        return iso14443TypeA;
    }

    public void setIso14443TypeA(SkipDetect iso14443TypeA) {
        this.iso14443TypeA = iso14443TypeA;
    }

    @Override
    public String toString() {
        return "PiccOperatingParameter{" + "\n" +
                "enableAutoPiccPolling=" + autoPiccPolling + "\n" +
                ", enableAutoAtsGeneration=" + autoAtsGeneration + "\n" +
                ", pollingInterval=" + pollingInterval + "\n" +
                ", detectFeliCa424K=" + feliCa424K + "\n" +
                ", detectFeliCa212K=" + feliCa212K + "\n" +
                ", detectTopaz=" + topaz + "\n" +
                ", detectISO14443TypeB=" + iso14443TypeB + "\n" +
                ", detectISO14443TypeA=" + iso14443TypeA + "\n" +
                '}';
    }

    public static PiccOperatingParameter fromBitPattern(byte bits) {
        return new PiccOperatingParameter(bits);
    }

    // TODO: EnableStatus & DetectStatus, to/from Boolean
    public enum EnableDisable {
        ENABLE,
        DISABLE
    }

    public enum SkipDetect {SKIP, DETECT}
}
