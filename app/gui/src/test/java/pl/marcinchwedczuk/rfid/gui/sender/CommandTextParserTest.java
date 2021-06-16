package pl.marcinchwedczuk.rfid.gui.sender;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommandTextParserTest {
    CommandTextParser parser = new CommandTextParser();

    @Test
    void can_parse_command_without_comments() {
        String cmdText = "AA BB CC\n" +
                "ff 11 22 33";

        assertThat(parser.isValid(cmdText))
                .isTrue();

        byte[] parsed = parser.parseToBytes(cmdText);
        assertThat(ByteArrays.toHexString(parsed))
                .isEqualTo("AA BB CC FF 11 22 33");
    }

    @Test
    void can_parse_command_with_comments() {
        String cmdText =
            "# This is a comment\n" +
            "   # This is another comment \n" +
            "FF AA BB CC DD\n" +
            " ff 88 # Comment inside line\n";

        assertThat(parser.isValid(cmdText))
                .isTrue();

        byte[] parsed = parser.parseToBytes(cmdText);
        assertThat(ByteArrays.toHexString(parsed))
                .isEqualTo("FF AA BB CC DD FF 88");
    }

    @Test
    void isValid_works() {
        assertThat(parser.isValid("FFF 11 22"))
                .isFalse();

        assertThat(parser.isValid("F 11 22"))
                .isFalse();

        assertThat(parser.isValid("FF1122"))
                .isTrue();
    }
}