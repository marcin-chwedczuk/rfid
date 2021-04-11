module pl.marcinchwedczuk {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.smartcardio;

    requires org.apache.logging.log4j;

    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    exports pl.marcinchwedczuk.rfid.gui;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.rfid.gui to javafx.fxml;
    opens pl.marcinchwedczuk.rfid.xml to com.fasterxml.jackson.databind;
}