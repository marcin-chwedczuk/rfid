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

    @Nested
    class toHexString {
        @Test
        void toMacString_works() {
            String result = ByteArrays.toMacString(new byte[] { 0x01, 0x02, 0x03 });
            assertThat(result)
                    .isEqualTo("01:02:03");
        }

        @Test
        void toHexString_works() {
            String result = ByteArrays.toHexString(new byte[] { 0x01, 0x02, 0x03 });
            assertThat(result)
                    .isEqualTo("01 02 03");
        }

        @Test
        void toHexString_with_separator_works() {
            String result = ByteArrays.toHexString(new byte[] { 0x01, 0x02, 0x03 }, "--");
            assertThat(result)
                    .isEqualTo("01--02--03");
        }
    }

    @Nested
    class fromHexString {
        @Test
        void fromMacString_works() {
            byte[] result = ByteArrays.fromMacString("01:02:03");
            assertThat(result)
                    .isEqualTo(ByteArrays.of(1, 2, 3));
        }

        @Test
        void fromHexString_works() {
            byte[] result = ByteArrays.fromHexString("01 02 03");
            assertThat(result)
                    .isEqualTo(ByteArrays.of(1, 2, 3));
        }

        @Test
        void fromHexString_with_separator_works() {
            byte[] result = ByteArrays.fromHexString("01--02--03", "--");
            assertThat(result)
                    .isEqualTo(ByteArrays.of(1, 2, 3));
        }
    }
}