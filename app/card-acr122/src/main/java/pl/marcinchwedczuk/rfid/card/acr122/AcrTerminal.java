package pl.marcinchwedczuk.rfid.card.acr122;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.acr122.impl.AcrStandardErrors;
import pl.marcinchwedczuk.rfid.card.acr122.impl.LoggingCardDecorator;
import pl.marcinchwedczuk.rfid.card.acr122.impl.PcScUtils;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * {@code javax.smartcardio} wrapper for ACR122 reader.
 */
public class AcrTerminal extends AcrTerminalCommands {
    private static final Logger logger = LoggerFactory.getLogger(AcrTerminal.class);
    private static final byte FF = (byte) 0xFF;

    // TODO: Revert to private
    public final CardTerminal cardTerminal;

    public AcrTerminal(CardTerminal cardTerminal) {
        if (cardTerminal == null) throw new NullPointerException();

        this.cardTerminal = cardTerminal;
    }

    /**
     * No card should be on reader while executing this command
     * (DIRECT mode is used to connect).
     */
    public byte[] sendRawCommand(byte[] bytes) throws CardException {
        if (isCardPresent()) {
            // TODO: Switch to card mode when card is present
            throw new IllegalStateException(
                    "This method can only be called when card is not present on the terminal.");
        }

        // See: https://stackoverflow.com/a/29235803/1779504
        // System specific see: https://github.com/leg0/libnfc/blob/master/libnfc/drivers/acr122_pcsc.c#L53
        // macOS: https://github.com/pokusew/nfc-pcsc/issues/13#issuecomment-302482621
        Card card = new LoggingCardDecorator(cardTerminal.connect("DIRECT"));
        try {
            return card.transmitControlCommand(
                    PcScUtils.IOCTL_CCID_ESCAPE(), bytes);
        } catch (CardException e) {
            if (PcScUtils.isEscapeCommandNotEnabled(e)) {
                throw AcrStandardErrors.escapeCommandNotEnabled();
            }
            throw e;
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
        String protocol = "T=0";
        try {
            Card card = cardTerminal.connect(protocol);
            return new AcrCard(this, new LoggingCardDecorator(card));
        } catch (CardException e) {
            throw new AcrException(
                    String.format("Cannot connect to card using protocol: '%s'.", protocol),
                    e);
        }
    }
}
