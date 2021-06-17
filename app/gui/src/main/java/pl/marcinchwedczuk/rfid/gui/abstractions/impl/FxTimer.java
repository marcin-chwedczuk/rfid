package pl.marcinchwedczuk.rfid.gui.abstractions.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.gui.main.MainWindow;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class FxTimer implements pl.marcinchwedczuk.rfid.gui.abstractions.Timer {
    private static final Logger logger = LoggerFactory.getLogger(FxTimer.class);

    private final Timer timer = new Timer(true);

    private boolean isDestroyed = false;
    private AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void scheduleAtFixedRate(Duration initialDelay, Duration delay, Runnable action) {
        checkState();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!started.get()) {
                    return;
                }

                try {
                    action.run();
                } catch (Exception e) {
                    logger.error("Unhandled exception in timer action. Timer will be stopped.", e);
                    timer.cancel();
                }
            }
        }, initialDelay.toMillis(), delay.toMillis());
    }

    @Override
    public void start() {
        checkState();
        started.set(true);
    }

    @Override
    public void stop() {
        started.set(false);
    }

    @Override
    public void destroy() {
        timer.cancel();
        isDestroyed = true;
    }

    private void checkState() {
        if (isDestroyed) {
            throw new IllegalStateException("Timer was destroyed and cannot be started again.");
        }
    }
}
