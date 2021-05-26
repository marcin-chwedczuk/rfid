package pl.marcinchwedczuk.rfid.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.marcinchwedczuk.rfid.gui.commands.UiServices;

import java.io.IOException;

public class FxProgressDialog implements UiServices.ProgressDialog {
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

    public void updateProgress(double percentage) {
        percentage = Math.max(0, Math.min(100, percentage));
        progressBar.setProgress(percentage / 100.0);
    }

    public void close() {
        window.close();
    }

    public static FxProgressDialog show(Scene owner, String description, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/ProgressDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("Operation in progress...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initOwner(owner.getWindow());
            childWindow.setResizable(false);

            FxProgressDialog controller = loader.getController();
            controller.init(childWindow, description, onCancel);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
