package pl.marcinchwedczuk.rfid.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ProgressDialog {
    private Stage window;
    private boolean cancelled = false;

    @FXML private Button cancelButton;
    @FXML public ProgressBar progressBar;
    @FXML public Label operationName;

    public Stage getWindow() { return window; }

    private void init(Stage window, String description, Runnable onCancel) {
        this.window = window;
        this.operationName.setText(description);
        this.cancelButton.setOnAction(event -> {
            if (!cancelled) {
                cancelled = true;
                onCancel.run();
            }
        });
    }

    public void setProgress(int percentage) {
        percentage = Math.max(0, Math.min(100, percentage));
        progressBar.setProgress(percentage / 100.0);
    }

    public void done() {
        window.close();
    }

    public static ProgressDialog show(Scene owner, String description, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/ProgressDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("Operation in progress...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initOwner(owner.getWindow());
            childWindow.setResizable(false);

            ProgressDialog controller = loader.getController();
            controller.init(childWindow, description, onCancel);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}