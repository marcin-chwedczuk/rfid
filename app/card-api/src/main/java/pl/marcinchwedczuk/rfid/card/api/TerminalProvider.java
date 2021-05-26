package pl.marcinchwedczuk.rfid.card.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

public interface TerminalProvider {
    Collection<? extends Terminal> findTerminals();


}
