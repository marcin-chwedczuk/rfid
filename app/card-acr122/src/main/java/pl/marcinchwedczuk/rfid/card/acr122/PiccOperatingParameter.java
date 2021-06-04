package pl.marcinchwedczuk.rfid.card.acr122;

import pl.marcinchwedczuk.rfid.card.acr122.impl.RichByte;
import pl.marcinchwedczuk.rfid.card.commons.utils.ToStringBuilder;

import static pl.marcinchwedczuk.rfid.card.acr122.FeatureStatus.DISABLED;
import static pl.marcinchwedczuk.rfid.card.acr122.FeatureStatus.ENABLED;
import static pl.marcinchwedczuk.rfid.card.acr122.PoolingInterval.INTERVAL_250_MILLIS;
import static pl.marcinchwedczuk.rfid.card.acr122.PoolingInterval.INTERVAL_500_MILLIS;
import static pl.marcinchwedczuk.rfid.card.acr122.DetectionStatus.DETECT;
import static pl.marcinchwedczuk.rfid.card.acr122.DetectionStatus.IGNORE;

public class PiccOperatingParameter {
    static PiccOperatingParameter parseByte(byte b) {
        return new PiccOperatingParameter(b);
    }

    public static PiccOperatingParameter newDefault() {
        return new PiccOperatingParameter((byte)0xFF);
    }

    private FeatureStatus autoPiccPolling;

    /**
     * To issue ATS (Answer To Select) Request whenever
     * an ISO14443-4 Type A tag is activated.
     */
    private FeatureStatus autoAtsGeneration;

    /**
     * To set the time interval between successive PICC Polling.
     */
    private PoolingInterval pollingInterval;

    /* The Tag Types to be detected during PICC Polling. */
    private DetectionStatus feliCa424K;
    private DetectionStatus feliCa212K;
    private DetectionStatus topaz;
    private DetectionStatus iso14443TypeB;

    /**
     * To detect the Mifare Tags, the Auto ATS Generation must be disabled first.
     */
    private DetectionStatus iso14443TypeA;

    private PiccOperatingParameter(byte b) {
        RichByte richByte = new RichByte(b);

        autoPiccPolling = richByte.isBitSet(7) ? ENABLED : DISABLED;
        autoAtsGeneration = richByte.isBitSet(6) ? ENABLED : DISABLED;
        pollingInterval = richByte.isBitSet(5)
                ? INTERVAL_250_MILLIS
                : INTERVAL_500_MILLIS;

        feliCa424K = richByte.isBitSet(4) ? DETECT : IGNORE;
        feliCa212K = richByte.isBitSet(3) ? DETECT : IGNORE;
        topaz = richByte.isBitSet(2) ? DETECT : IGNORE;
        iso14443TypeB = richByte.isBitSet(1) ? DETECT : IGNORE;
        iso14443TypeA = richByte.isBitSet(0) ? DETECT : IGNORE;
    }

    byte toByte() {
        return new RichByte(0)
                .withBit(7, autoPiccPolling == ENABLED)
                .withBit(6, autoAtsGeneration == ENABLED)
                .withBit(5, pollingInterval == INTERVAL_250_MILLIS)
                .withBit(4, feliCa424K == DETECT)
                .withBit(3, feliCa212K == DETECT)
                .withBit(2, topaz == DETECT)
                .withBit(1, iso14443TypeB == DETECT)
                .withBit(0, iso14443TypeA == DETECT)
                .asByte();
    }

    public FeatureStatus getAutoPiccPolling() {
        return autoPiccPolling;
    }
    public void setAutoPiccPolling(FeatureStatus autoPiccPolling) {
        this.autoPiccPolling = autoPiccPolling;
    }

    public FeatureStatus getAutoAtsGeneration() {
        return autoAtsGeneration;
    }
    public void setAutoAtsGeneration(FeatureStatus autoAtsGeneration) {
        this.autoAtsGeneration = autoAtsGeneration;
    }

    public PoolingInterval getPollingInterval() {
        return pollingInterval;
    }
    public void setPollingInterval(PoolingInterval pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public DetectionStatus getFeliCa424K() {
        return feliCa424K;
    }
    public void setFeliCa424K(DetectionStatus feliCa424K) {
        this.feliCa424K = feliCa424K;
    }

    public DetectionStatus getFeliCa212K() {
        return feliCa212K;
    }
    public void setFeliCa212K(DetectionStatus feliCa212K) {
        this.feliCa212K = feliCa212K;
    }

    public DetectionStatus getTopaz() {
        return topaz;
    }
    public void setTopaz(DetectionStatus topaz) {
        this.topaz = topaz;
    }

    public DetectionStatus getIso14443TypeB() {
        return iso14443TypeB;
    }
    public void setIso14443TypeB(DetectionStatus iso14443TypeB) {
        this.iso14443TypeB = iso14443TypeB;
    }

    public DetectionStatus getIso14443TypeA() {
        return iso14443TypeA;
    }
    public void setIso14443TypeA(DetectionStatus iso14443TypeA) {
        this.iso14443TypeA = iso14443TypeA;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(PiccOperatingParameter.class)
            .appendField("enableAutoPiccPolling", autoPiccPolling)
            .appendField("enableAutoAtsGeneration", autoAtsGeneration)
            .appendField("pollingInterval", pollingInterval)
            .appendField("feliCa424K", feliCa424K)
            .appendField("feliCa212K", feliCa212K)
            .appendField("topaz", topaz)
            .appendField("iso14443TypeB", iso14443TypeB)
            .appendField("iso14443TypeA", iso14443TypeA)
            .toString();
    }
}
