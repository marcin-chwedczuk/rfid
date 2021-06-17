package pl.marcinchwedczuk.rfid.gui.progress;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.marcinchwedczuk.rfid.gui.abstractions.ProgressDialog;

import java.io.IOException;

public class FxProgressDialog implements ProgressDialog {
    public static FxProgressDialog show(Scene owner, String description, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("FxProgressDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner.getWindow());
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("Operation in progress...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);

            FxProgressDialog controller = loader.getController();
            controller.init(childWindow, description, onCancel);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stage window;
    private boolean cancelled = false;

    @FXML
    private Button cancelButton;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label operationName;

    public Stage getWindow() {
        return window;
    }

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

}
