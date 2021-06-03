package pl.marcinchwedczuk.rfid.card.commons;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ByteArraysTest {
    @Nested
    class of_method {
        @Test
        void can_create_empty_array() {
            byte[] result = ByteArrays.of();

            assertThat(result)
                    .isEmpty();
        }

        @Test
        void returns_valid_byte_array() {
            byte[] result = ByteArrays.of(0x00, 0x08, 0xff);

            assertThat(result[0] & 0xff).isEqualTo(0x00);
            assertThat(result[1] & 0xff).isEqualTo(0x08);
            assertThat(result[2] & 0xff).isEqualTo(0xff);
        }
    }


}