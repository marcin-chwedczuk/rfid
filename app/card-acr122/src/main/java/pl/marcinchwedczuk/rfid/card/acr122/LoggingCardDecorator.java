package pl.marcinchwedczuk.rfid.card.acr122;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;
import pl.marcinchwedczuk.rfid.card.commons.StringUtils;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.util.Objects;

public class LoggingCardDecorator extends Card {
    private static final Logger logger = LoggerFactory.getLogger(LoggingCardDecorator.class);

    private final Card card;

    public LoggingCardDecorator(Card card) {
        this.card = Objects.requireNonNull(card);
    }

    @Override
    public ATR getATR() {
        ATR atr = card.getATR();
        logger.debug("getATR() returns {}", ByteArrays.toMacString(atr.getBytes()));
        return atr;
    }

    @Override
    public String getProtocol() {
        String proto = card.getProtocol();
        logger.debug("getProtocol() returns {}", proto);
        return proto;
    }

    @Override
    public CardChannel getBasicChannel() {
        return new LoggingCardChannelDecorator(card.getBasicChannel());
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
        return new LoggingCardChannelDecorator(card.openLogicalChannel());
    }

    @Override
    public void beginExclusive() throws CardException {
        logger.debug("beginExclusive()");
        card.beginExclusive();
    }

    @Override
    public void endExclusive() throws CardException {
        card.endExclusive();
        logger.debug("endExclusive()");
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        logger.debug(String.format("transmitControlCommand(%08x, %s)", controlCode, ByteArrays.toHexString(command)));
        return card.transmitControlCommand(controlCode, command);
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
        logger.debug("disconnect({})", reset);
        card.disconnect(reset);
    }

    private class LoggingCardChannelDecorator extends CardChannel {
        private final Logger logger = LoggerFactory.getLogger(LoggingCardDecorator.class);

        private final CardChannel cardChannel;

        public LoggingCardChannelDecorator(CardChannel cardChannel) {
            this.cardChannel = cardChannel;
        }

        @Override
        public Card getCard() {
            return LoggingCardDecorator.this;
        }

        @Override
        public int getChannelNumber() {
            int num = cardChannel.getChannelNumber();
            logger.debug("getChannelNumber() returns {}", num);
            return num;
        }

        @Override
        public ResponseAPDU transmit(CommandAPDU command) throws CardException {
            try {
                logger.debug("transmit({})", ByteArrays.toHexString(command.getBytes()));
                ResponseAPDU response = cardChannel.transmit(command);
                logger.debug("transmit(...) returns {}", ByteArrays.toHexString(response.getBytes()));
                return response;
            } catch (CardException e) {
                logger.error("transmit(...) throws", e);
                throw e;
            }
        }

        @Override
        public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
            try {
                byte[] tmp = new byte[command.remaining()];
                command.get(tmp);
                logger.debug("transmit({}, response)", ByteArrays.toHexString(tmp));

                int result = cardChannel.transmit(command, response);

                tmp = new byte[response.remaining()];
                command.get(tmp);
                logger.debug("transmit(...) returns {}, {}", result, ByteArrays.toHexString(tmp));

                return result;
            } catch (CardException e) {
                logger.error("transmit(...) throws", e);
                throw e;
            }
        }

        @Override
        public void close() throws CardException {
            logger.debug("close()");
            cardChannel.close();
        }
    }
}
