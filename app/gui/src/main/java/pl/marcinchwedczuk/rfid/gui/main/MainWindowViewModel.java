package pl.marcinchwedczuk.rfid.gui.main;

import pl.marcinchwedczuk.rfid.gui.abstractions.Timer;

import java.util.Objects;

public class MainWindowViewModel {
    private final Timer timer;

    public MainWindowViewModel(Timer timer) {
        this.timer = Objects.requireNonNull(timer);
    }
}
