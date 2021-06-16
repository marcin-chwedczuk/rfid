package pl.marcinchwedczuk.rfid.gui.abstractions;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime localNow();
}
