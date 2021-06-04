package pl.marcinchwedczuk.rfid.card.fake;

import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;
import pl.marcinchwedczuk.rfid.card.fake.impl.CardState;
import pl.marcinchwedczuk.rfid.card.fake.impl.Mifare1KSimulator;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

import static pl.marcinchwedczuk.rfid.card.fake.impl.CardState.CARD_ABSENT;
import static pl.marcinchwedczuk.rfid.card.fake.impl.CardState.CARD_PRESENT;

public class FakeCardTerminal extends CardTerminal {
    public static FakeCardTerminal withCardPresent() {
        return new FakeCardTerminal(CARD_PRESENT);
    }
    public static FakeCardTerminal withCardAbsent() {
        return new FakeCardTerminal(CARD_ABSENT);
    }

    private final CardState cardState;
    private final Acr122Simulator acr122;

    public FakeCardTerminal() { this(CARD_PRESENT); }

    public FakeCardTerminal(CardState cardState) {
        this.cardState = cardState;
        this.acr122 = new Acr122Simulator(
                cardState == CardState.CARD_ABSENT
                        ? null
                        : new Mifare1KSimulator());
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
                return new FakeCard(protocol, acr122);

            case "DIRECT":
                return new FakeCard(protocol, acr122);

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
