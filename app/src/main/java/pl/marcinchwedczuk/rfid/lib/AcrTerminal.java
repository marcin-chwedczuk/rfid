package pl.marcinchwedczuk.rfid.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * {@code javax.smartcardio} wrapper for ACR122 reader.
 */
public class AcrTerminal {
    private static final Logger logger = LogManager.getLogger(AcrTerminal.class);

    private final CardTerminal cardTerminal;

    public AcrTerminal(CardTerminal cardTerminal) {
        if (cardTerminal == null) throw new NullPointerException();

        this.cardTerminal = cardTerminal;
    }

    public String name() {
        return cardTerminal.getName();
    }

    public boolean isCardPresent() {
        try {
            return cardTerminal.isCardPresent();
        } catch (CardException e) {
            logger.warn("isCardPresent failed", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return name();
    }

    public AcrCard connect() {
        try {
            Card card = cardTerminal.connect("T=0");
            return new AcrCard(this, card);
        } catch (CardException e) {
            throw new AcrException(e);
        }
    }

    public static List<AcrTerminal> getTerminals() {
        logger.info("Getting list of the terminals.");
        try {
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
            List<CardTerminal> terminals = factory.terminals().list();
            return terminals.stream()
                    .map(AcrTerminal::new)
                    .collect(toList());
        } catch (CardException | NoSuchAlgorithmException e) {
            logger.warn("Cannot get list of the terminals.", e);
            return Collections.emptyList();
        }
    }
}
