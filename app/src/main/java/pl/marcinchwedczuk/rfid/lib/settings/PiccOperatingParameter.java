package pl.marcinchwedczuk.rfid.lib.settings;

import static pl.marcinchwedczuk.rfid.lib.settings.PiccOperatingParameter.PoolingInterval.INTERVAL_250_MILLIS;
import static pl.marcinchwedczuk.rfid.lib.settings.PiccOperatingParameter.PoolingInterval.INTERVAL_500_MILLIS;

public class PiccOperatingParameter {
    private boolean enableAutoPiccPolling;

    /**
     * To issue ATS (Answer To Select) Request whenever
     * an ISO14443-4 Type A tag is activated
     */
    private boolean enableAutoAtsGeneration;

    enum PoolingInterval {
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
        enableAutoPiccPolling = isSet(bits, 7);
        enableAutoAtsGeneration = isSet(bits, 6);
        pollingInterval = isSet(bits, 5)
                ? INTERVAL_250_MILLIS
                : INTERVAL_500_MILLIS;

        detectFeliCa424K = isSet(bits, 4);
        detectFeliCa212K = isSet(bits, 3);
        detectTopaz = isSet(bits, 2);
        detectISO14443TypeB = isSet(bits, 1);
        detectISO14443TypeA = isSet(bits, 0);
    }

    private static boolean isSet(byte b, int bitPosition) {
        return ((b & (1 << bitPosition)) != 0);
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
