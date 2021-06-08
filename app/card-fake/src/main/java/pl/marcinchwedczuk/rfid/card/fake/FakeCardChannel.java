package pl.marcinchwedczuk.rfid.card.fake;

import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;

import javax.smartcardio.*;
import java.nio.ByteBuffer;

class FakeCardChannel extends CardChannel {
    private final FakeCard fakeCard;
    private final Acr122Simulator acr122;

    private boolean closed = false;

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
        checkState();
        return acr122.executeCommand(command);
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        checkState();

        throw new RuntimeException("Not implemented in FakeCardChannel.");
    }

    @Override
    public void close() throws CardException {
        this.closed = true;
    }

    private void checkState() {
        if (closed) {
            throw new IllegalStateException("Operation cannot be performed on a closed channel.");
        }
    }
}
