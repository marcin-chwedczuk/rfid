package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.marcinchwedczuk.rfid.lib.*;

import javax.smartcardio.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow {
    @FXML public ComboBox<AcrTerminal> terminalsList;
    @FXML public Label infoScreen;

    private final Timer timer = new Timer(true);
    private Optional<Stage> cartWindow = Optional.empty();

    private void out(String format, Object... args) {
    }

    @FXML
    public void initialize() {
        refreshTerminalList(null);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    checkCardStateChanged();
                });
            }
        }, 1000, 500);
    }

    private void checkCardStateChanged() {
        currentTerminal().ifPresent(
                terminal -> {
                    if (!terminal.isCardPresent()) {
                        cartWindow.ifPresent(stage -> stage.close());
                        cartWindow = Optional.empty();
                    } else {
                        if (cartWindow.isEmpty()) {
                            cartWindow = Optional.of(showCardWindow());
                        }
                    }
                });
    }

    private Stage showCardWindow() {
        AcrCard card = currentTerminal().get().connect();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/pl/marcinchwedczuk/rfid/gui/CardWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("MIFARE 1K Tag Editor");
            childWindow.setScene(new Scene(loader.load(), 800, 1024));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initOwner(terminalsList.getScene().getWindow());
            childWindow.setMinWidth(800);
            childWindow.setMinHeight(640);

            childWindow.setOnCloseRequest(we -> card.disconnect());
            ((CardWindow)loader.getController()).setCard(card);

            childWindow.show();
            return childWindow;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buttonClicked(ActionEvent actionEvent) {
        try {
            out("default terminal type: %s", TerminalFactory.getDefaultType());


            // Display the list of terminals
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
            List<CardTerminal> terminals = factory.terminals().list();





            // Use the first terminal
            CardTerminal terminal = terminals.get(0);

            out("Wait for a card...");
            terminal.waitForCardPresent(0);

            // T=0 WTF? https://buzz.smartcardfocus.com/smart-card-terminology-explained/
            Card card = terminal.connect("T=0");
            out("PROTOCOL: %s", card.getProtocol());

            AcrCard acrCard = new AcrCard(null, card);
            AtrInfo atrInfo = acrCard.atrInfo();
            out("STANDARD: %s", atrInfo.cardStandard.readableName());
            out("CARD NAME: %s", atrInfo.cardName.readableName());

            //out("FIRMWARE: %s", acrCard.getReaderFirmwareVersion());
            out("CARD UID: %s", acrCard.getCardUID());

            byte ff = (byte)0xff;
            int reg = 0;
            acrCard.loadKeyToRegister(new byte[] { ff, ff, ff, ff, ff, ff }, reg);
            acrCard.authenticate(SectorBlock.firstBlockOfSector(1), KeyType.KEY_A, reg);

            //byte[] data = acrCard.readBinaryBlock(SectorBlock.firstBlockOfSector(1), 16);
            //out("DATA: %s", ByteUtils.asHexString(data, ":"));

            //acrCard.writeBinaryBlock(SectorBlock.firstBlockOfSector(1),
             //       "Hello, world!...".getBytes(StandardCharsets.US_ASCII));

            byte[] trailer1 = acrCard.readBinaryBlock(SectorBlock.fromSectorAndBlock(1, 3), 16);
            TrailerBlock tb = new TrailerBlock(trailer1);

            out("%s", tb.accessBits.asTable());

            out("KEY A: %s", ByteUtils.asHexString(tb.keyA, ":"));
            out("KEY B: %s", ByteUtils.asHexString(tb.keyB, ":"));

            /*
            card.beginExclusive();
            try {
                CardChannel chann = card.getBasicChannel();
                chann.transmit(new CommandAPDU(new byte[] {

                }));
            } finally {
                card.endExclusive();
            }*/

            card.disconnect(true);

            /*
            // Connect wit hthe card
            Card card = terminal.connect("*");
            System.out.println("card: " + card);
            CardChannel channel = card.getBasicChannel();

            // Send Select Applet command
            byte[] aid = {(byte)0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01};
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid));
            System.out.println("answer: " + answer.toString());

            // Send test command
            answer = channel.transmit(new CommandAPDU(0x00, 0x00, 0x00, 0x00));
            System.out.println("answer: " + answer.toString());
            byte r[] = answer.getData();
            for (int i=0; i<r.length; i++)
                System.out.print((char)r[i]);
            System.out.println();

            // Disconnect the card
            card.disconnect(false);
             */
        } catch(Exception e) {
            new DialogBoxes().exception(e);
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void refreshTerminalList(ActionEvent actionEvent) {
        infoScreen.setText("Select terminal to start...");

        terminalsList.getItems().clear();
        terminalsList.getItems().addAll(AcrTerminal.getTerminals());
        terminalsList.getSelectionModel().selectFirst();

        if (!terminalsList.getSelectionModel().isEmpty()) {
            infoScreen.setText("Put MIFARE 1K card on the terminal to start...");
        } else if (terminalsList.getItems().isEmpty()) {
            infoScreen.setText("No terminal found...");
        }
    }

    private Optional<AcrTerminal> currentTerminal() {
        return Optional.ofNullable(terminalsList.getSelectionModel().getSelectedItem());
    }

    public void closeMainWindow(ActionEvent actionEvent) {
        Platform.exit();
    }
}
