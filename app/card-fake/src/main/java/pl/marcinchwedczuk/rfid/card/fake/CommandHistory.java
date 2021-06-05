package pl.marcinchwedczuk.rfid.card.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: Add tests for this class
public class CommandHistory {
    private List<CommandHistoryEntry> entries = new ArrayList<>();

    public void addCommand(byte[] command, String comment) {
        entries.add(new CommandHistoryEntry(command, comment));
    }

    public List<CommandHistoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public CommandHistoryEntry getLastEntry() {
        List<CommandHistoryEntry> entries = getEntries();
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(entries.size() - 1);
    }

    public static class CommandHistoryEntry {
        public final byte[] commandBytes;
        public final String comment;

        public CommandHistoryEntry(byte[] commandBytes,
                                   String comment) {
            this.commandBytes = commandBytes.clone();
            this.comment = Objects.requireNonNull(comment);
        }
    }
}
