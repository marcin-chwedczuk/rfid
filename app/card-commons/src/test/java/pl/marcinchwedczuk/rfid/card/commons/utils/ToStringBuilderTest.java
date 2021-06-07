package pl.marcinchwedczuk.rfid.card.commons.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ToStringBuilderTest {
    @Test
    void builder_works() {
        String result = new ToStringBuilder(ToStringBuilder.class)
                .appendField("a", 1)
                .appendField("b", "foo")
                .appendField("c", List.of(1, 2, 3))
                .toString();

        assertThat(result)
                .isEqualTo("ToStringBuilder { a: 1, b: 'foo', c: [1, 2, 3] }");
    }
}