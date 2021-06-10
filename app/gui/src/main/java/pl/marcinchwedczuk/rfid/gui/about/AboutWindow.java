package pl.marcinchwedczuk.rfid.gui.about;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.marcinchwedczuk.rfid.gui.App;
import pl.marcinchwedczuk.rfid.gui.progress.FxProgressDialog;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutWindow implements Initializable {
    @FXML
    private Label javaFxLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // JavaFX label animation
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
        shrinkGrow.setByX(1.1f);
        shrinkGrow.setByY(1.1f);
        shrinkGrow.setCycleCount(Animation.INDEFINITE);
        shrinkGrow.setAutoReverse(true);

        new ParallelTransition(rotate, shrinkGrow).play();
    }

    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/about/AboutWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("About...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(false);

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void closeWindow() {
        final Stage stage = (Stage) javaFxLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openApplicationGitHubPage() {
        App.hostServices().showDocument(
                "https://github.com/marcin-chwedczuk/rfid");
    }

    @FXML
    private void openSangaYTChannel() {
        App.hostServices().showDocument(
                "https://www.youtube.com/watch?v=VZDPBHtS0k4&list=PLXsEtURGV4pRLnvgoTd4FjzKFbXEfFNXn");
    }
}
