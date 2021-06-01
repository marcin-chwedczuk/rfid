package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

public class CardAbsentFakeCardTerminal extends CardTerminal {
    @Override
    public String getName() {
        return "Fake Terminal (Card Absent)";
    }

    @Override
    public Card connect(String protocol) throws CardException {
        throw new CardNotPresentException("Card not present");
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return false;
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        return true;
    }
}
