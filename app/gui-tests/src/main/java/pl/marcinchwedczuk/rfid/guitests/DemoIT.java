package pl.marcinchwedczuk.rfid.guitests;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import pl.marcinchwedczuk.rfid.gui.App;

import java.util.concurrent.TimeoutException;

public class DemoIT extends ApplicationTest {
    private FxRobot robot = new FxRobot();
    private Application app;

    @Override
    public void start(Stage stage) throws Exception {
        app = new App();
        app.start(stage);
    }

    @Override
    public void stop() throws Exception {
        FxToolkit.hideStage();
    }

    @Test
    public void application_starts_properly() throws InterruptedException {
        Thread.sleep(3000);
        robot.clickOn(robot.lookup("#closeWindowButton").queryButton());
    }

    @Test
    public void application_starts_properly2() throws InterruptedException {
        Thread.sleep(3000);
        robot.clickOn(robot.lookup("#closeWindowButton").queryButton());
    }
}
