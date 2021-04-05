package pl.marcinchwedczuk.rfid.lib;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * {@code javax.smartcardio} wrapper for ACR122 reader.
 */
public class AcrTerminal {
    private final CardTerminal cardTerminal;

    public AcrTerminal(CardTerminal cardTerminal) {
        if (cardTerminal == null) throw new NullPointerException();

        this.cardTerminal = cardTerminal;
    }

    public AcrCard connect() {
        try {
            Card card = cardTerminal.connect("T=0");
            return new AcrCard(this, card);
        } catch (CardException e) {
            throw new AcrException(e);
        }
    }
}
