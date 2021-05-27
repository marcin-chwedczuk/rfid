package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AcrTerminalProvider {
    private static final Logger logger = LoggerFactory.getLogger(AcrTerminalProvider.class);

    public static final AcrTerminalProvider INSTANCE = new AcrTerminalProvider();
    private AcrTerminalProvider() {}

    private static CardTerminal fakeTerminal = null;

    // VisibleForTesting
    public static void setFakeTerminal(CardTerminal terminal) {
        fakeTerminal = terminal;
    }

    public String name() {
        return "ACR122";
    }

    public Collection<? extends AcrTerminal> findTerminals() {
        logger.info("Getting list of the terminals.");
        try {
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);

            List<CardTerminal> terminals = new ArrayList<>(factory.terminals().list());
            if (fakeTerminal != null) {
                terminals.add(fakeTerminal);
            }

            return terminals.stream()
                    .map(AcrTerminal::new)
                    .collect(toList());
        } catch (CardException | NoSuchAlgorithmException e) {
            logger.warn("Cannot get list of the terminals.", e);
            return Collections.emptyList();
        }
    }
}
