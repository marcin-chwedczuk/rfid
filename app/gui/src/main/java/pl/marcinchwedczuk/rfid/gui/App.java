package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.marcinchwedczuk.rfid.gui.abstractions.impl.FxDialogBoxes;
import pl.marcinchwedczuk.rfid.gui.main.MainWindow;

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
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Unhandled exception", e);
            new FxDialogBoxes().exception(e);
        });

        App.hostServices = getHostServices();

        loadIcons(primaryStage, "/pl/marcinchwedczuk/rfid/gui/appicon/icon-acr122-%d.png", 32, 128, 256);

        MainWindow.show(primaryStage);
    }

    // TODO: Does not work on macOS - can be fixed with this:
    // https://www.reddit.com/r/java/comments/ugugi/how_to_set_the_dock_icon_on_osx/
    private static void loadIcons(Stage stage, String iconPathFormat, int... sizes) {
        for (int size: sizes) {
            String fullPath = String.format(iconPathFormat, size);
            try {
                Image icon = new Image(App.class.getResourceAsStream(fullPath));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                logger.error("Cannot load icon: {}.", fullPath, e);
            }
        }
    }

    public static void main(String[] args) {
        // Is this placebo? Maybe, but monospaced fonts look better with this
        // ...or so I think.
        System.setProperty("prism.lcdtext", "false");

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