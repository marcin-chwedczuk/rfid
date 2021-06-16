package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import pl.marcinchwedczuk.javafx.validation.Input;
import pl.marcinchwedczuk.javafx.validation.converters.Converters;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;
import pl.marcinchwedczuk.rfid.card.commons.utils.ByteArrays;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class SenderViewModel {
    public final Input<String, String> commandText = new Input<String, String>(Converters.identityConverter())
            .withInitialValue(
                    "# Get firmware version\n" +
                    "FF 00 48 00 00");

    // TODO: Create Output class in validation library
    private final StringProperty outputText = new SimpleStringProperty("");
    public ReadOnlyStringProperty outputTextProperty() {
        return outputText;
    }

    private final StringProperty errorMessage = new SimpleStringProperty("");
    public ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage;
    }

    public BooleanBinding showErrorProperty() {
        return Bindings.greaterThan(Bindings.length(errorMessage), 0);
    }

    private final CommandTextParser commandTextParser = new CommandTextParser();
    private final HexEditorFormatter hexEditorFormatter = new HexEditorFormatter();

    private final AcrTerminalCommands terminalCommands;

    public SenderViewModel(AcrTerminalCommands terminalCommands) {
        this.terminalCommands = Objects.requireNonNull(terminalCommands);
    }

    public void send() {
        if (!validateInput()) {
            return;
        }

        byte[] command = commandTextParser.parseToBytes(commandText.getModelValue());

        if (outputText.get().length() > 0) {
            appendOutput("");
            appendOutput("---------------------------------------------------------------");
        }

        appendOutput(commandText.getModelValue());
        appendOutput("");

        try {
            byte[] responseBytes = terminalCommands.sendCommand(command);
            appendOutput(hexEditorFormatter.formatAsString(responseBytes));
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            message.append(e.toString()).append(System.lineSeparator());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            message.append(sw.toString());

            appendOutput(message.toString());
        }
    }

    private boolean validateInput() {
        if (!commandTextParser.isValid(commandText.getModelValue())) {
            errorMessage.set("Invalid command text. " +
                    "Command bytes should be written using hex literals e.g. 'AA BB CC 01'.");
            return false;
        }
        else {
            errorMessage.set("");
            return true;
        }
    }

    private void appendOutput(String s) {
        outputText.set(outputText.get() + "\n" + s);
    }

    public void clear() {
        outputText.set("");
    }
}
