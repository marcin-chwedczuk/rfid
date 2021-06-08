package pl.marcinchwedczuk.rfid.card.fake;

import pl.marcinchwedczuk.rfid.card.fake.impl.Acr122Simulator;
import pl.marcinchwedczuk.rfid.card.fake.impl.CardPresence;
import pl.marcinchwedczuk.rfid.card.fake.impl.Mifare1KSimulator;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

import static pl.marcinchwedczuk.rfid.card.fake.impl.CardPresence.CARD_ABSENT;
import static pl.marcinchwedczuk.rfid.card.fake.impl.CardPresence.CARD_PRESENT;

public class FakeCardTerminal extends CardTerminal {
    public static FakeCardTerminal withCardPresent() {
        return new FakeCardTerminal(CARD_PRESENT);
    }
    public static FakeCardTerminal withCardAbsent() {
        return new FakeCardTerminal(CARD_ABSENT);
    }

    private final CommandHistory commandHistory = new CommandHistory();
    private final CardPresence cardPresence;
    private final Acr122Simulator acr122;

    public FakeCardTerminal() { this(CARD_PRESENT); }

    public FakeCardTerminal(CardPresence cardPresence) {
        this.cardPresence = cardPresence;
        this.acr122 = new Acr122Simulator(
                commandHistory,
                cardPresence.isCardPresent() ? new Mifare1KSimulator() : null);
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    @Override
    public String getName() {
        return String.format("Fake Terminal (%s)", cardPresence);
    }

    @Override
    public Card connect(String protocol) throws CardException {
        switch (protocol) {
            case "T=0":
                if (cardPresence == CARD_ABSENT)
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
        return (cardPresence == CARD_PRESENT);
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
