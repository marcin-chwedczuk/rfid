package pl.marcinchwedczuk.rfid.gui;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestWindow implements Initializable {

    public TableView<DataAccessInfoTable.DataAccessInfoBean> table;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new DataAccessInfoTable(table).setup();
    }

    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxProgressDialog.class.getResource("/pl/marcinchwedczuk/rfid/gui/TestWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.setTitle("Test...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setResizable(true);

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
