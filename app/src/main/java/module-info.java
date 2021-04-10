module pl.marcinchwedczuk {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.smartcardio;
    requires org.apache.logging.log4j;

    exports pl.marcinchwedczuk.rfid.gui;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.rfid.gui to javafx.fxml;
}