package Visualize3D;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Visualize extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("3DVisual.fxml"));
        primaryStage.setTitle("Map");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
