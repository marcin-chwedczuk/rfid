package pl.marcinchwedczuk.rfid.card.api;

import java.util.Arrays;
import java.util.Collection;

import static java.util.stream.Collectors.toUnmodifiableList;

public class TerminalProviders {
    // final static org.slf.Logger logger = LoggerFactory.getLogger(Wombat.class);

    static TerminalProvider fromMultipleSources(TerminalProvider... providers) {
        return new TerminalProvider() {
            @Override
            public Collection<? extends Terminal> findTerminals() {
                return Arrays.stream(providers)
                        .flatMap(p -> p.findTerminals().stream())
                        .collect(toUnmodifiableList());
            }
        };
    }

    private static Collection<? extends Terminal> findTerminalsSafe(TerminalProvider provider) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
