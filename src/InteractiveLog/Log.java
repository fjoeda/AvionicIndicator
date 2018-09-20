package InteractiveLog;

import au.com.bytecode.opencsv.CSVReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Log extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    sample.Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("InteractiveLog.fxml"));
        controller  = (new FXMLLoader(getClass().getResource("InteractiveLog.fxml"))).getController();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Waypoint");

        primaryStage.setTitle("Log");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
