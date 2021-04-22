package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Is this placebo? Maybe, but monospaces fonts look better with this
        // ...or so I think.
        System.setProperty("prism.lcdtext", "false");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LogManager.getLogger().error("Unhandled exception", e);
            FxDialogBoxes.exception(e);
        });

        Parent root = FXMLLoader.load(
                getClass().getResource("/pl/marcinchwedczuk/rfid/gui/MainWindow.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("Mifare Tag Editor");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        printJvmOptions();
        launch();
    }

    private static void printJvmOptions() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        System.out.println("JVM ARGUMENTS:");
        System.out.println(String.join(System.lineSeparator(), arguments));
        System.out.println();
    }
}