package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

/**
 * Fake MIFARE 1K Card.
 *
 * All data is based on ACR API specification document
 * (see docs/ folder in this repository).
 */
public class FakeCard extends Card {
    private final FakeMifare1K mifare1K = new FakeMifare1K();

    @Override
    public ATR getATR() {
        return mifare1K.getATR();
    }

    @Override
    public String getProtocol() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CardChannel getBasicChannel() {
        return new FakeCardChannel(this, mifare1K);
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        return new FakeCardChannel(this, mifare1K);
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
        return new byte[0];
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        // no-op
    }
}
