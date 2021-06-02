package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class LoggingTerminalDecorator extends CardTerminal {
    private static final Logger logger = LoggerFactory.getLogger(LoggingTerminalDecorator.class);

    private final CardTerminal decorated;

    public LoggingTerminalDecorator(CardTerminal decorated) {
        this.decorated = decorated;
    }

    @Override
    public String getName() {
        return decorated.getName();
    }

    @Override
    public Card connect(String protocol) throws CardException {
        logger.debug("Connect to card using protocol {}.", protocol);
        return decorated.connect(protocol);
    }

    @Override
    public boolean isCardPresent() throws CardException {
        return decorated.isCardPresent();
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws CardException {
        return decorated.waitForCardPresent(timeout);
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws CardException {
        return decorated.waitForCardAbsent(timeout);
    }
}
