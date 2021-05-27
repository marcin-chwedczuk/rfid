module pl.marcinchwedczuk.rfid {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.smartcardio;

    requires org.slf4j;
    // requires org.apache.logging.log4j.slf4j;

    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.management;

    requires pl.marcinchwedczuk.rfid.card.api;
    requires pl.marcinchwedczuk.rfid.card.acr122;
    requires pl.marcinchwedczuk.rfid.card.fake;

    exports pl.marcinchwedczuk.rfid.gui;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.rfid.gui to javafx.fxml;
    opens pl.marcinchwedczuk.rfid.xml to com.fasterxml.jackson.databind;
}