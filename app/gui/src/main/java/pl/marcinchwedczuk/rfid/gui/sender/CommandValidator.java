package pl.marcinchwedczuk.rfid.gui.sender;

import javafx.beans.Observable;
import pl.marcinchwedczuk.javafx.validation.Objections;
import pl.marcinchwedczuk.javafx.validation.ValidationResult;
import pl.marcinchwedczuk.javafx.validation.Validator;

import java.util.Collection;
import java.util.Objects;

public class CommandValidator implements Validator<String> {
    private final CommandTextParser parser;

    public CommandValidator(CommandTextParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public <TT extends String> ValidationResult<TT> validate(TT cmdText) {
        if (!parser.isValid(cmdText)) {
            return ValidationResult.failure(
                    cmdText,
                    Objections.error(
                            "Cannot parse command text. " +
                            "Command bytes must be written using hex literals e.g. '11 22 AA FFF'."));
        }

        byte[] cmdBytes = parser.parseToBytes(cmdText);
        if (cmdBytes.length < 4) {
            return ValidationResult.failure(
                    cmdText,
                    Objections.error("Command must consists of at least four bytes."));
        }

        return ValidationResult.success(cmdText);
    }
}
