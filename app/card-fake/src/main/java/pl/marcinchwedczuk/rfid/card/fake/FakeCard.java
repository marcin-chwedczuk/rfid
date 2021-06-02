package pl.marcinchwedczuk.rfid.card.fake;

import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;
import pl.marcinchwedczuk.rfid.card.fake.impl.CardState;
import pl.marcinchwedczuk.rfid.card.fake.impl.Mifare1KSimulator;

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

    public FakeCard(String protocol, CardState cardState) {
        this.protocol = Objects.requireNonNull(protocol);
        Objects.requireNonNull(cardState);

        acr122 = new Acr122Simulator(
                cardState == CardState.CARD_ABSENT
                    ? null
                    : new Mifare1KSimulator());
    }

    @Override
    public ATR getATR() {
        return acr122.getATR();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public CardChannel getBasicChannel() {
        return new FakeCardChannel(this, acr122);
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        return new FakeCardChannel(this, acr122);
    }

    @Override
    public void beginExclusive() throws CardException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public void endExclusive() throws CardException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        return acr122.transmitControlCommand(controlCode, command);
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        // no-op
    }
}
