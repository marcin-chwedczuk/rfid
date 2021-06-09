package pl.marcinchwedczuk.rfid.card.commons;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccessBitsParserTest {

    @Test
    void round_trip_works() {
        byte[] sectorTrailer = Mifare.defaultSectorTrailer();
        byte[] sectorTrailerAccessBits = Arrays.copyOfRange(sectorTrailer, 6, 6 + 3);
        AccessBits accessBits = new AccessBitsParser().parse(sectorTrailer);

        byte[] roundTripped = new AccessBitsParser().unparse(accessBits);
        byte[] roundTrippedAccessBits = Arrays.copyOfRange(roundTripped, 6, 6 + 3);

        assertThat(ByteArrays.toHexString(sectorTrailerAccessBits))
                .isEqualTo(ByteArrays.toHexString(roundTrippedAccessBits));
    }
}