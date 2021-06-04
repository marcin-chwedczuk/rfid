package pl.marcinchwedczuk.rfid.card.acr122;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PiccOperatingParameterTest {
    @Test
    void newDefault_factory_method_works() {
        PiccOperatingParameter picc = PiccOperatingParameter.newDefault();

        assertThat(picc.getAutoAtsGeneration())
                .isEqualTo(FeatureStatus.ENABLED);
        assertThat(picc.getAutoPiccPolling())
                .isEqualTo(FeatureStatus.ENABLED);

        assertThat(picc.getPollingInterval())
                .isEqualTo(PoolingInterval.INTERVAL_250_MILLIS);

        assertThat(picc.getFeliCa212K())
                .isEqualTo(DetectionStatus.DETECT);
        assertThat(picc.getFeliCa424K())
                .isEqualTo(DetectionStatus.DETECT);
        assertThat(picc.getTopaz())
                .isEqualTo(DetectionStatus.DETECT);
        assertThat(picc.getIso14443TypeA())
                .isEqualTo(DetectionStatus.DETECT);
        assertThat(picc.getIso14443TypeB())
                .isEqualTo(DetectionStatus.DETECT);
    }

    @Test
    void toByte_values_works() {
        PiccOperatingParameter picc = PiccOperatingParameter.newDefault();
        assertThat(picc.toByte() & 0xFF)
                .isEqualTo(0xFF);

        picc.setAutoAtsGeneration(FeatureStatus.DISABLED);
        picc.setAutoPiccPolling(FeatureStatus.DISABLED);
        picc.setPollingInterval(PoolingInterval.INTERVAL_500_MILLIS);
        picc.setFeliCa212K(DetectionStatus.IGNORE);
        picc.setFeliCa424K(DetectionStatus.IGNORE);
        picc.setTopaz(DetectionStatus.IGNORE);
        picc.setIso14443TypeA(DetectionStatus.IGNORE);
        picc.setIso14443TypeB(DetectionStatus.IGNORE);

        assertThat(picc.toByte() & 0xFF)
                .isEqualTo(0x00);
    }

    @Test
    void round_trip_works() {
        PiccOperatingParameter picc = PiccOperatingParameter.newDefault();

        picc.setPollingInterval(PoolingInterval.INTERVAL_500_MILLIS);
        picc.setFeliCa212K(DetectionStatus.IGNORE);
        picc.setFeliCa424K(DetectionStatus.IGNORE);
        picc.setTopaz(DetectionStatus.IGNORE);

        byte b = picc.toByte();
        PiccOperatingParameter recreated = PiccOperatingParameter.parseByte(b);

        assertThat(recreated.getAutoAtsGeneration())
                .isEqualTo(FeatureStatus.ENABLED);
        assertThat(recreated.getAutoPiccPolling())
                .isEqualTo(FeatureStatus.ENABLED);

        assertThat(recreated.getPollingInterval())
                .isEqualTo(PoolingInterval.INTERVAL_500_MILLIS);

        assertThat(recreated.getFeliCa212K())
                .isEqualTo(DetectionStatus.IGNORE);
        assertThat(recreated.getFeliCa424K())
                .isEqualTo(DetectionStatus.IGNORE);
        assertThat(recreated.getTopaz())
                .isEqualTo(DetectionStatus.IGNORE);
        assertThat(recreated.getIso14443TypeA())
                .isEqualTo(DetectionStatus.DETECT);
        assertThat(recreated.getIso14443TypeB())
                .isEqualTo(DetectionStatus.DETECT);
    }
}