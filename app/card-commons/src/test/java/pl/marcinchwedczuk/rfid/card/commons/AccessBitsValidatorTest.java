package pl.marcinchwedczuk.rfid.card.commons;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccessBitsValidatorTest {
    @Test
    void default_sector_trailer_is_valid() {
        boolean result = new AccessBitsValidator(Mifare1K.defaultSectorTrailer())
                .isValid();

        assertThat(result)
                .isTrue();
    }

    @Test
    void access_bits_all_zeros_are_invalid() {
        boolean result = new AccessBitsValidator(new byte[16])
                .isValid();

        assertThat(result)
                .isFalse();
    }
}