module pl.marcinchwedczuk.rfid.card.fake {
    requires java.smartcardio;
    requires org.slf4j;

    requires pl.marcinchwedczuk.rfid.card.api;
    requires pl.marcinchwedczuk.rfid.card.commons;

    exports pl.marcinchwedczuk.rfid.card.fake;
}