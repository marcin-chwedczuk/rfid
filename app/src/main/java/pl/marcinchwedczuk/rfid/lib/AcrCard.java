package pl.marcinchwedczuk.rfid.lib;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;

public class AcrCard {
    private final AcrTerminal terminal;
    private final Card card;

    public AcrCard(AcrTerminal terminal, Card card) {
        this.terminal = terminal;
        this.card = card;
    }

    public AtrInfo atrInfo() {
        ATR atr = card.getATR();
        return AtrInfo.parse(atr.getBytes());
    }
}
