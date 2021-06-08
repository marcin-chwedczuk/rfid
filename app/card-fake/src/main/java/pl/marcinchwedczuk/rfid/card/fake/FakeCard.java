package pl.marcinchwedczuk.rfid.card.fake;

import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import java.util.Objects;

/**
 * Fake MIFARE 1K Card.
 * <p>
 * All data is based on ACR API specification document
 * (see docs/ folder in this repository).
 */
class FakeCard extends Card {
    private final String protocol;
    private final Acr122Simulator acr122;
    private final FakeCardChannel channel;

    private boolean disconnected = false;

    public FakeCard(String protocol, Acr122Simulator acr122) {
        this.protocol = Objects.requireNonNull(protocol);
        this.acr122 = Objects.requireNonNull(acr122);
        this.channel = new FakeCardChannel(this, acr122);
    }

    @Override
    public ATR getATR() {
        // ATR is saved upon first contact, so a valid
        // value is returned even when card is disconnected.
        // Hence no checkState() call here.
        return acr122.getATR();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public CardChannel getBasicChannel() {
        checkState();
        return channel;
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        checkState();
        return new FakeCardChannel(this, acr122);
    }

    @Override
    public void beginExclusive() throws CardException {
        checkState();

        throw new RuntimeException("Not implemented.");
    }

    @Override
    public void endExclusive() throws CardException {
        checkState();

        throw new RuntimeException("Not implemented.");
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        checkState();
        return acr122.transmitControlCommand(controlCode, command);
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        this.disconnected = true;
        this.channel.close();
    }

    private void checkState() {
        if (disconnected) {
            throw new IllegalStateException("Cannot perform operation after disconnecting from card.");
        }
    }
}
