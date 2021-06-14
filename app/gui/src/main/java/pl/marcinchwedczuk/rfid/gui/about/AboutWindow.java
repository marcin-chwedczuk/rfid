package pl.marcinchwedczuk.rfid.gui.about;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import pl.marcinchwedczuk.rfid.gui.App;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutWindow implements Initializable {
    public static void showModal(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AboutWindow.class.getResource("AboutWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("About...");
            childWindow.setResizable(false);
            childWindow.setScene(new Scene(loader.load()));

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private Label javaFxLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startAnimation();
    }

    private void startAnimation() {
        final Duration SEC_2 = Duration.millis(2000);

        RotateTransition rotate = new RotateTransition(SEC_2);
        rotate.setByAngle(360);
        rotate.setAutoReverse(false);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(Animation.INDEFINITE);

        ScaleTransition shrinkGrow = new ScaleTransition(SEC_2);
        shrinkGrow.setByX(1.1f);
        shrinkGrow.setByY(1.1f);
        shrinkGrow.setAutoReverse(true);
        shrinkGrow.setCycleCount(Animation.INDEFINITE);

        ParallelTransition parallelTransition = new ParallelTransition(javaFxLabel, rotate, shrinkGrow);
        parallelTransition.play();
    }

    @FXML
    public void closeWindow() {
        final Stage stage = (Stage) javaFxLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openApplicationGitHubPage() {
        openLink("https://github.com/marcin-chwedczuk/rfid");
    }

    @FXML
    private void openSangaYTChannel() {
        openLink("https://www.youtube.com/watch?v=VZDPBHtS0k4&list=PLXsEtURGV4pRLnvgoTd4FjzKFbXEfFNXn");
    }

    private void openLink(String url) {
        App.hostServices().showDocument(url);
    }
}
