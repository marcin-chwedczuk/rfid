package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.application.Platform;
import javafx.scene.Scene;
import pl.marcinchwedczuk.rfid.gui.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseUiCommand<W> {
    private final Scene ownerWindow;

    private final List<W> workItems = new ArrayList<>();
    private int currentItem = 0;

    private boolean canceled = false;
    private Exception error = null;
    private ProgressDialog progressDialog = null;

    protected BaseUiCommand(Scene ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    protected boolean wasCanceled() { return canceled; }
    protected Exception error() { return error; }

    protected abstract String operationName();
    protected abstract List<W> defineWorkItems();
    protected void before() { }
    protected abstract void doWork(W item);
    protected void after() { }

    /**
     * @return Return true if exception was handled and execution should proceed.
     */
    protected boolean handleWorkError(Exception e) {
        return false;
    }

    public void runCommandAsync() {
        workItems.addAll(defineWorkItems());
        Platform.runLater(this::runBefore);
    }

    private void runBefore() {
        progressDialog = ProgressDialog.show(ownerWindow, operationName(), this::cancel);

        try {
            before();
            Platform.runLater(this::runDoWork);
        } catch (Exception e) {
            this.error = e;
            Platform.runLater(this::runAfter);
        }
    }

    private void runDoWork() {
        if (currentItem >= workItems.size()) {
            Platform.runLater(this::runAfter);
            return;
        }

        if (canceled) {
            Platform.runLater(this::runAfter);
            return;
        }

        double progress = (currentItem * 100.0 / workItems.size());
        progressDialog.setProgress(progress);

        try {
            W item = workItems.get(currentItem);
            doWork(item);
        } catch (Exception e) {
            if (!handleWorkError(e)) {
                error = e;
                Platform.runLater(this::runAfter);
                return;
            }
        }

        currentItem++;
        Platform.runLater(this::runDoWork);
    }

    private void runAfter() {
        progressDialog.done();

        runAfter();
    }

    public void cancel() {
        this.canceled = true;
    }
}
