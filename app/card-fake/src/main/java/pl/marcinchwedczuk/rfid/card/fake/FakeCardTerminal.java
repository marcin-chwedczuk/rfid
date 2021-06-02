package pl.marcinchwedczuk.rfid.card.fake;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

import static pl.marcinchwedczuk.rfid.card.fake.CardState.CARD_ABSENT;
import static pl.marcinchwedczuk.rfid.card.fake.CardState.CARD_PRESENT;

public class FakeCardTerminal extends CardTerminal {
    public static FakeCardTerminal withCardPresent() {
        return new FakeCardTerminal(CARD_PRESENT);
    }
    public static FakeCardTerminal withCardAbsent() {
        return new FakeCardTerminal(CARD_ABSENT);
    }

    private final CardState cardState;

    public FakeCardTerminal() { this(CARD_PRESENT); }

    public FakeCardTerminal(CardState cardState) {
        this.cardState = cardState;
    }

    @Override
    public String getName() {
        return String.format("Fake Terminal (%s)", cardState);
    }

    @Override
    public Card connect(String protocol) throws CardException {
        switch (protocol) {
            case "T=0":
                if (cardState == CARD_ABSENT)
                    throw new CardNotPresentException("Card not present.");
                return new FakeCard(protocol, cardState);

            case "DIRECT":
                return new FakeCard(protocol, cardState);

            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return (cardState == CARD_PRESENT);
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        if (isCardPresent()) {
            return true;
        }

        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        if (!isCardPresent()) {
            return true;
        }

        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
