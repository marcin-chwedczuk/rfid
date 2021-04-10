package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;

import java.util.function.Consumer;
import java.util.function.Function;

public class PoorManBackgroundTask {
    private final int from;
    private final int toExclusive;
    private int current = -1;

    private final Function<Integer, Boolean> work;
    private final Runnable before;
    private final Runnable after;

    public PoorManBackgroundTask(int from, int toExclusive, Function<Integer, Boolean> work, Runnable before, Runnable after) {
        this.from = from;
        this.toExclusive = toExclusive;
        this.work = work;
        this.before = before;
        this.after = after;
    }

    public void start() {
        before.run();

        Platform.runLater(this::nextStep);
    }

    private void nextStep() {
        current++;
        if (current < toExclusive) {
            boolean cont = work.apply(current);
            if (cont) {
                Platform.runLater(this::nextStep);
            } else {
                after.run();
            }
        } else {
            after.run();
        }
    }
}
