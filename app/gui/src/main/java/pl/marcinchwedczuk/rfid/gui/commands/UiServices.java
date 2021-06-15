package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.scene.Scene;
import pl.marcinchwedczuk.rfid.gui.utils.DialogBoxes;
import pl.marcinchwedczuk.rfid.gui.utils.JavaFxDialogBoxes;
import pl.marcinchwedczuk.rfid.gui.progress.FxProgressDialog;

public class UiServices {
    private DialogBoxes dialogBoxes = new JavaFxDialogBoxes();

    private final Scene ownerWindow;

    public UiServices(Scene ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    void showErrorDialog(String message, String details) {
        dialogBoxes.error(message, details);
    }

    ProgressDialog showProgressDialog(String message, Runnable cancelCallback) {
        return FxProgressDialog.show(ownerWindow, message, cancelCallback);
    }

    public interface ProgressDialog {
        void updateProgress(double percentage);

        void close();
    }
}
