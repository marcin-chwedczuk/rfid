package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import pl.marcinchwedczuk.javafx.validation.Input;
import pl.marcinchwedczuk.javafx.validation.ValidationGroup;
import pl.marcinchwedczuk.javafx.validation.converters.Converters;
import pl.marcinchwedczuk.rfid.card.acr122.AcrTerminalCommands;
import pl.marcinchwedczuk.rfid.gui.abstractions.TimeProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class SenderViewModel {
    private final CommandTextParser commandTextParser = new CommandTextParser();
    private final HexEditorFormatter hexEditorFormatter = new HexEditorFormatter();

    public final Input<String, String> commandText = new Input<String, String>(Converters.identityConverter())
            .withModelValidator(new CommandValidator(commandTextParser))
            .withInitialValue(
                    "# Get firmware version\n" +
                    "FF 00 48 00 00");

    public final Input<Boolean, Boolean> clearOnSendFlag = new Input<Boolean, Boolean>(Converters.identityConverter())
            .withInitialValue(false);

    // TODO: Create Output class in validation library
    private final StringProperty outputText = new SimpleStringProperty("");
    public ReadOnlyStringProperty outputTextProperty() {
        return outputText;
    }

    private final StringProperty errorMessage = new SimpleStringProperty(null);
    public ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage;
    }

    private final ValidationGroup allInputs = new ValidationGroup(commandText);

    private final TimeProvider timeProvider;
    private final AcrTerminalCommands terminalCommands;

    public SenderViewModel(TimeProvider timeProvider,
                           AcrTerminalCommands terminalCommands) {
        this.timeProvider = Objects.requireNonNull(timeProvider);
        this.terminalCommands = Objects.requireNonNull(terminalCommands);
    }

    public void send() {
        errorMessage.set(null);
        if (!allInputs.validate()) {
            // Set error to first objection
            errorMessage.set(commandText.getObjections().get(0).userMessage);
            return;
        }

        byte[] command = commandTextParser.parseToBytes(commandText.getModelValue());

        if (outputText.get().length() > 0) {
            appendOutput("");
            appendOutput("---------------------------------------------------------------");
            appendOutput("");
        }

        appendOutput("TIME>");
        appendOutput(timeProvider.localNow().toString());
        appendOutput("");

        appendOutput("COMMAND>");
        appendOutput(commandText.getModelValue());
        appendOutput("");

        try {
            byte[] responseBytes = terminalCommands.sendCommand(command);
            appendOutput("RESPONSE>");
            appendOutput(hexEditorFormatter.formatAsString(responseBytes));
        } catch (Exception e) {
            StringBuilder message = new StringBuilder();

            message.append(e.toString()).append(System.lineSeparator());

            // TODO: Extract to util class
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            message.append(sw.toString());

            appendOutput("ERROR>");
            appendOutput(message.toString());
        }

        if (clearOnSendFlag.getModelValue()) {
            commandText.setModelValue("");
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
