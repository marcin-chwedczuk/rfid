package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.*;
import java.nio.ByteBuffer;

public class FakeCardChannel extends CardChannel {
    private final FakeCard fakeCard;
    private final FakeMifare1K mifare1K;

    public FakeCardChannel(FakeCard fakeCard, FakeMifare1K mifare1K) {
        this.fakeCard = fakeCard;
        this.mifare1K = mifare1K;
    }

    @Override
    public Card getCard() {
        return fakeCard;
    }

    @Override
    public int getChannelNumber() {
        return 0;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU command) throws CardException {
        return mifare1K.accept(command);
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void close() throws CardException {
        // TODO: Closed channel should throw exceptions
    }
}
