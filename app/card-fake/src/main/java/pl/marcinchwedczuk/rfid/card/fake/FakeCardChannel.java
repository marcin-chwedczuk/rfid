package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.*;
import java.nio.ByteBuffer;

class FakeCardChannel extends CardChannel {
    private final FakeCard fakeCard;
    private final Acr122Simulator acr122;

    public FakeCardChannel(FakeCard fakeCard, Acr122Simulator acr122) {
        this.fakeCard = fakeCard;
        this.acr122 = acr122;
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
        return acr122.executeCommand(command);
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        throw new RuntimeException("Not implemented in FakeCardChannel.");
    }

    @Override
    public void close() throws CardException {
        // TODO: Closed channel should throw exceptions
    }
}
