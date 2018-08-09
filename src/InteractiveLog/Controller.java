package InteractiveLog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Button btnLoad;
    private Window primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void LoadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Waypoint");
        fileChooser.setInitialDirectory(new File("E:"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.showOpenDialog(primaryStage);
    }
}
