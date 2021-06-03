module pl.marcinchwedczuk.rfid.card.commons {
    requires java.smartcardio;
    requires org.slf4j;

    exports pl.marcinchwedczuk.rfid.card.commons;
    exports pl.marcinchwedczuk.rfid.card.commons.utils;
}