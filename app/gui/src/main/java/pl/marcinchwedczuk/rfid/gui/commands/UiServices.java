package pl.marcinchwedczuk.rfid.gui.commands;

import javafx.scene.Scene;
import pl.marcinchwedczuk.rfid.gui.abstractions.DialogBoxes;
import pl.marcinchwedczuk.rfid.gui.abstractions.ProgressDialog;
import pl.marcinchwedczuk.rfid.gui.abstractions.impl.FxDialogBoxes;
import pl.marcinchwedczuk.rfid.gui.progress.FxProgressDialog;

public class UiServices {
    private DialogBoxes dialogBoxes = new FxDialogBoxes();

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

}
