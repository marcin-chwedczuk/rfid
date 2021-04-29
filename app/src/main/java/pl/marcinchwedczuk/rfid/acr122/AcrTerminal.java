package pl.marcinchwedczuk.rfid.acr122;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.*;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * {@code javax.smartcardio} wrapper for ACR122 reader.
 */
public class AcrTerminal extends AcrTerminalCommands {
    private static final Logger logger = LogManager.getLogger(AcrTerminal.class);
    private static final byte FF = (byte)0xFF;

    private final CardTerminal cardTerminal;

    public AcrTerminal(CardTerminal cardTerminal) {
        if (cardTerminal == null) throw new NullPointerException();

        this.cardTerminal = cardTerminal;
    }

    /**
     * No card should be on reader while executing this command
     * (DIRECT mode is used to connect).
     */
    protected byte[] sendCommandToTerminal(byte[] bytes) throws CardException {
        if (isCardPresent()) {
            // TODO: Switch to card mode when card is present
            throw new IllegalStateException(
                    "This method can only be called when card is not present on the terminal.");
        }

        // See: https://stackoverflow.com/a/29235803/1779504
        // System specific see: https://github.com/leg0/libnfc/blob/master/libnfc/drivers/acr122_pcsc.c#L53
        // macOS: https://github.com/pokusew/nfc-pcsc/issues/13#issuecomment-302482621
        Card card = cardTerminal.connect("DIRECT");
        try {
            return card.transmitControlCommand(
                    PcScUtils.IOCTL_CCID_ESCAPE(), bytes);
        } finally {
            card.disconnect(false);
        }
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
