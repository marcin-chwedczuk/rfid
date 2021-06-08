package pl.marcinchwedczuk.rfid.card.fake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FakeCardChannelTest {
    CardChannel channel;

    @BeforeEach
    void before() throws CardException {
        this.channel = FakeCardTerminal.withCardPresent()
                .connect("T=0")
                .getBasicChannel();
    }

    @Test
    void after_channel_is_closed_methods_throw_exceptions() throws CardException {
        channel.close();

        CommandAPDU dummyCommand = new CommandAPDU(ByteArrays.fromHexString("FF 00 51 CC 00"));
        assertThatThrownBy(() -> channel.transmit(dummyCommand))
                .isInstanceOf(IllegalStateException.class);

        ByteBuffer dummy = ByteBuffer.wrap(new byte[10]);
        assertThatThrownBy(() -> channel.transmit(dummy, dummy))
                .isInstanceOf(IllegalStateException.class);

        assertThatCode(() -> channel.getCard())
                .doesNotThrowAnyException();

        assertThatCode(() -> channel.getChannelNumber())
                .doesNotThrowAnyException();
    }

    @Test
    void close_can_be_called_multiple_times() throws CardException {
        channel.close();
        channel.close();
        channel.close();
    }
}