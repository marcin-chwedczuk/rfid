package pl.marcinchwedczuk.rfid.gui.abstractions;

import java.time.Duration;

public interface Timer {
    void scheduleAtFixedRate(Duration initialDelay, Duration delay, Runnable action);

    void start();
    void stop();

    void destroy();
}
