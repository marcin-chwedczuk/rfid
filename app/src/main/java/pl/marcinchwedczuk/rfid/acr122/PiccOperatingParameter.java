package pl.marcinchwedczuk.rfid.acr122;

import static pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.PoolingInterval.INTERVAL_250_MILLIS;
import static pl.marcinchwedczuk.rfid.acr122.PiccOperatingParameter.PoolingInterval.INTERVAL_500_MILLIS;

public class PiccOperatingParameter {
    private boolean enableAutoPiccPolling;

    /**
     * To issue ATS (Answer To Select) Request whenever
     * an ISO14443-4 Type A tag is activated
     */
    private boolean enableAutoAtsGeneration;


    public enum PoolingInterval {
        INTERVAL_250_MILLIS,
        INTERVAL_500_MILLIS
    }
    private PoolingInterval pollingInterval;

    /* The Tag Types to be detected during PICC Polling. */
    private boolean detectFeliCa424K;
    private boolean detectFeliCa212K;
    private boolean detectTopaz;
    private boolean detectISO14443TypeB;

    /**
     * To detect the Mifare Tags, the Auto ATS Generation must be disabled first.
     */
    private boolean detectISO14443TypeA;

    private PiccOperatingParameter(byte bits) {
        RichByte richBits = new RichByte(bits);

        enableAutoPiccPolling = richBits.isBitSet(7);
        enableAutoAtsGeneration = richBits.isBitSet(6);
        pollingInterval = richBits.isBitSet(5)
                ? INTERVAL_250_MILLIS
                : INTERVAL_500_MILLIS;

        detectFeliCa424K = richBits.isBitSet(4);
        detectFeliCa212K = richBits.isBitSet(3);
        detectTopaz = richBits.isBitSet(2);
        detectISO14443TypeB = richBits.isBitSet(1);
        detectISO14443TypeA = richBits.isBitSet(0);
    }

    public byte toByte() {
        return new RichByte(0)
                .withBit(7, enableAutoPiccPolling)
                .withBit(6, enableAutoAtsGeneration)
                .withBit(5, pollingInterval == INTERVAL_250_MILLIS)
                .withBit(4, detectFeliCa424K)
                .withBit(3, detectFeliCa212K)
                .withBit(2, detectTopaz)
                .withBit(1, detectISO14443TypeB)
                .withBit(0, detectISO14443TypeA)
                .asByte();
    }

    public boolean isEnableAutoPiccPolling() {
        return enableAutoPiccPolling;
    }

    public void setEnableAutoPiccPolling(boolean enableAutoPiccPolling) {
        this.enableAutoPiccPolling = enableAutoPiccPolling;
    }

    public boolean isEnableAutoAtsGeneration() {
        return enableAutoAtsGeneration;
    }

    public void setEnableAutoAtsGeneration(boolean enableAutoAtsGeneration) {
        this.enableAutoAtsGeneration = enableAutoAtsGeneration;
    }

    public PoolingInterval getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(PoolingInterval pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public boolean isDetectFeliCa424K() {
        return detectFeliCa424K;
    }

    public void setDetectFeliCa424K(boolean detectFeliCa424K) {
        this.detectFeliCa424K = detectFeliCa424K;
    }

    public boolean isDetectFeliCa212K() {
        return detectFeliCa212K;
    }

    public void setDetectFeliCa212K(boolean detectFeliCa212K) {
        this.detectFeliCa212K = detectFeliCa212K;
    }

    public boolean isDetectTopaz() {
        return detectTopaz;
    }

    public void setDetectTopaz(boolean detectTopaz) {
        this.detectTopaz = detectTopaz;
    }

    public boolean isDetectISO14443TypeB() {
        return detectISO14443TypeB;
    }

    public void setDetectISO14443TypeB(boolean detectISO14443TypeB) {
        this.detectISO14443TypeB = detectISO14443TypeB;
    }

    public boolean isDetectISO14443TypeA() {
        return detectISO14443TypeA;
    }

    public void setDetectISO14443TypeA(boolean detectISO14443TypeA) {
        this.detectISO14443TypeA = detectISO14443TypeA;
    }

    @Override
    public String toString() {
        return "PiccOperatingParameter{" + "\n" +
                "enableAutoPiccPolling=" + enableAutoPiccPolling +"\n" +
                ", enableAutoAtsGeneration=" + enableAutoAtsGeneration +"\n" +
                ", pollingInterval=" + pollingInterval +"\n" +
                ", detectFeliCa424K=" + detectFeliCa424K +"\n" +
                ", detectFeliCa212K=" + detectFeliCa212K +"\n" +
                ", detectTopaz=" + detectTopaz +"\n" +
                ", detectISO14443TypeB=" + detectISO14443TypeB +"\n" +
                ", detectISO14443TypeA=" + detectISO14443TypeA +"\n" +
                '}';
    }

    public static PiccOperatingParameter fromBitPattern(byte bits) {
        return new PiccOperatingParameter(bits);
    }
}
