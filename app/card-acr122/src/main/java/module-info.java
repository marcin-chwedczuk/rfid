module pl.marcinchwedczuk.rfid.card.acr122 {
    requires java.smartcardio;
    requires org.apache.logging.log4j;

    requires pl.marcinchwedczuk.rfid.card.api;

    exports pl.marcinchwedczuk.rfid.card.acr122;
}