package pl.marcinchwedczuk.rfid.gui.abstractions.impl;

import pl.marcinchwedczuk.rfid.gui.abstractions.TimeProvider;

import java.time.LocalDateTime;

public class SystemTimeTimeProvider implements TimeProvider {
    @Override
    public LocalDateTime localNow() {
        return LocalDateTime.now();
    }
}
