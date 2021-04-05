package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            new DialogBoxes().exception(e);
        });

        Parent root = FXMLLoader.load(
                getClass().getResource("/pl/marcinchwedczuk/rfid/gui/MainWindow.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("XOX MiniMax");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}