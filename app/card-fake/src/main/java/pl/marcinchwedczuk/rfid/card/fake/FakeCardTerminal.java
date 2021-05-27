package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class FakeCardTerminal extends CardTerminal {
    @Override
    public String getName() {
        return "Fake Terminal";
    }

    @Override
    public Card connect(String protocol) throws CardException {
        return new FakeCard();
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return true;
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        return true;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        return false;
    }
}
