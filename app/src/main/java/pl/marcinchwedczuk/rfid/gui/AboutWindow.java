package pl.marcinchwedczuk.rfid.gui;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutWindow implements Initializable {
    @FXML private Label javaFxLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RotateTransition rotate = new RotateTransition();
        rotate.setAxis(Rotate.Z_AXIS);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setDuration(Duration.millis(2000));
        rotate.setAutoReverse(false);
        rotate.setNode(javaFxLabel);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        ScaleTransition shrinkGrow = new ScaleTransition(Duration.millis(2000), javaFxLabel);
        shrinkGrow.setByX(1.3f);
        shrinkGrow.setByY(1.3f);
        shrinkGrow.setCycleCount(Animation.INDEFINITE);
        shrinkGrow.setAutoReverse(true);

        new ParallelTransition(rotate, shrinkGrow).play();
    }

    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/AboutWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("About...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            // childWindow.initOwner(owner.getWindow());
            childWindow.setResizable(false);

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeWindow(ActionEvent unused) {
        final Stage stage = (Stage) javaFxLabel.getScene().getWindow();
        stage.close();
    }
}
