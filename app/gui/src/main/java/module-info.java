module pl.marcinchwedczuk.rfid {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.smartcardio;
    requires java.management;

    requires org.slf4j;

    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    requires pl.marcinchwedczuk.rfid.card.acr122;
    requires pl.marcinchwedczuk.rfid.card.fake;
    requires pl.marcinchwedczuk.rfid.card.commons;

    exports pl.marcinchwedczuk.rfid.gui;
    // Make resources available to javafx
    opens pl.marcinchwedczuk.rfid.gui.img;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.rfid.gui;
    opens pl.marcinchwedczuk.rfid.gui.main;
    opens pl.marcinchwedczuk.rfid.gui.card;
    opens pl.marcinchwedczuk.rfid.gui.settings;
    opens pl.marcinchwedczuk.rfid.gui.sender;
    opens pl.marcinchwedczuk.rfid.gui.progress;
    opens pl.marcinchwedczuk.rfid.gui.about;
    opens pl.marcinchwedczuk.rfid.gui.controls.keybox;
    opens pl.marcinchwedczuk.rfid.gui.controls.maskedtext;
    opens pl.marcinchwedczuk.rfid.xml;
}