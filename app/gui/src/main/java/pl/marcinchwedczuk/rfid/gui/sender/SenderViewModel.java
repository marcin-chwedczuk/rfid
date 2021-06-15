package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import pl.marcinchwedczuk.javafx.validation.Input;
import pl.marcinchwedczuk.javafx.validation.converters.Converters;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class SenderViewModel {
    public final Input<String, String> commandText = new Input<String, String>(Converters.identityConverter())
            .withInitialValue("FF 00 48 00 00");

    // TODO: Create Output class in validation library
    private final StringProperty outputText = new SimpleStringProperty("");
    public ReadOnlyStringProperty outputTextProperty() {
        return outputText;
    }

    private final AcrTerminalCommands terminalCommands;

    public SenderViewModel(AcrTerminalCommands terminalCommands) {
        this.terminalCommands = Objects.requireNonNull(terminalCommands);
    }

    public void send() {
        byte[] command = ByteArrays.fromHexString(commandText.getModelValue());
        try {
            byte[] responseBytes = terminalCommands.sendCommand(command);
            outputText.set(ByteArrays.toHexString(responseBytes));
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            message.append(e.toString()).append(System.lineSeparator());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            message.append(sw.toString());

            outputText.set(message.toString());
        }
    }

    public void clear() {

    }
}
