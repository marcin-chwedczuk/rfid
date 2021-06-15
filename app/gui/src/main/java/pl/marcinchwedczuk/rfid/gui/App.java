package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.gui.utils.JavaFxDialogBoxes;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static HostServices hostServices = null;

    public static HostServices hostServices() {
        if (hostServices == null) {
            throw new IllegalStateException();
        }
        return hostServices;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Is this placebo? Maybe, but monospaced fonts look better with this
        // ...or so I think.
        System.setProperty("prism.lcdtext", "false");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Unhandled exception", e);
            new JavaFxDialogBoxes().exception(e);
        });

        App.hostServices = getHostServices();

        Parent root = FXMLLoader.load(
                getClass().getResource("/pl/marcinchwedczuk/rfid/gui/main/MainWindow.fxml"));

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
        try {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            List<String> arguments = runtimeMxBean.getInputArguments();
            logger.info("Jvm Arguments: {}", String.join(System.lineSeparator(), arguments));
        }
        catch (Exception e) { }
    }
}