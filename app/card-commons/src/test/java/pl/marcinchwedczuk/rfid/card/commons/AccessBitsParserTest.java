package pl.marcinchwedczuk.rfid.card.commons;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AccessBitsParserTest {
    @Test
    void parses_default_sector_trailer_aka_transport_configuration() {
        AccessBits accessBits = new AccessBitsParser()
                .parse(Mifare1K.defaultSectorTrailer());

        assertThat(accessBits.dataBlockAccessForBlock(0))
                .isEqualTo(DataBlockAccess.C000);

        assertThat(accessBits.dataBlockAccessForBlock(1))
                .isEqualTo(DataBlockAccess.C000);

        assertThat(accessBits.dataBlockAccessForBlock(2))
                .isEqualTo(DataBlockAccess.C000);

        assertThat(accessBits.trailerBlockAccess())
                .isEqualTo(TrailerBlockAccess.C001);
    }

    @Test
    void round_trip_works() {
        byte[] sectorTrailer = Mifare1K.defaultSectorTrailer();
        AccessBits accessBits = new AccessBitsParser().parse(sectorTrailer);
        byte[] roundTripped = new AccessBitsParser().unparse(accessBits);

        byte[] sectorTrailerAccessBits = extractAccessBytes(sectorTrailer);
        byte[] roundTrippedAccessBits = extractAccessBytes(roundTripped);
        assertThat(ByteArrays.toHexString(sectorTrailerAccessBits))
                .isEqualTo(ByteArrays.toHexString(roundTrippedAccessBits));
    }

    private static byte[] extractAccessBytes(byte[] sectorTrailer) {
        return Arrays.copyOfRange(sectorTrailer, 6, 6 + 3);
    }
}