package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.scene.Scene;
import pl.marcinchwedczuk.rfid.gui.FxDialogBoxes;
import pl.marcinchwedczuk.rfid.gui.FxProgressDialog;

public class UiServices {
    private final Scene ownerWindow;

    public UiServices(Scene ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    void showErrorDialog(String message) {
        FxDialogBoxes.error(message);
    }

    ProgressDialog showProgressDialog(String message, Runnable cancelCallback) {
        return FxProgressDialog.show(ownerWindow, message, cancelCallback);
    }

    public interface ProgressDialog {
        void updateProgress(double percentage);
        void close();
    }
}
