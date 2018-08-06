package InteractiveLog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;

public class Log extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    sample.Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("InteractiveLog.fxml"));
        //Controller controller = FXMLLoader.load(getClass().getResourceAsStream())
        //controller = FXMLLoader.load(getClass().getResource("InteractiveLog.fxml"))
        controller  = (new FXMLLoader(getClass().getResource("InteractiveLog.fxml"))).getController();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Waypoint");

        primaryStage.setTitle("Log");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        //fileChooser.showOpenDialog(primaryStage);
    }
}
