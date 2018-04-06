package Visualize3D;

import SerialComm.SerialCommunication;
import TextParser.StringParser;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.w3c.dom.DOMImplementation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    public Button setRotationButton;
    public Button connectButton;
    public TextField pitch;
    public TextField roll;
    public SubSceneContainer subSceneContainer;
    public HBox outerBox;

    private AnimationTimer timer;
    private long lastTimerCall;

    private ViewerModel viewer;

    SerialCommunication serial;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serial = new SerialCommunication();
        serial.setPortName("COM14");
        serial.setBaudRate(9600);
        lastTimerCall = System.nanoTime();

        viewer = new ViewerModel();
        subSceneContainer.setSubScene(viewer.getSubScene());

        subSceneContainer.prefWidthProperty().bind(outerBox.widthProperty());
        subSceneContainer.prefHeightProperty().bind(outerBox.heightProperty());

        load3DFile(getClass().getResource("Object/FW.stl").toExternalForm());

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 500_000_000L && serial.getReceivedMessage()!=null){
                    viewer.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
                    viewer.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
                    lastTimerCall = now;
                }
            }
        };

    }

    private void load3DFile(String url){
        viewer.setContent(null);
        System.out.println((new File(url)).exists());
        new Thread(() -> {
            try {
                Group content = Importer3D.load(url);
                handleLoadResult(content);
                System.out.println("Loaded");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void rotationButtonClick(ActionEvent actionEvent) {
        viewer.setRoll(Double.valueOf(roll.getText()));
        viewer.setPitch(Double.valueOf(pitch.getText()));
    }

    public void connectSerial(ActionEvent actionEvent) {
        try {
            serial.connectToSerial();
            timer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLoadResult(final Group content) {

        Platform.runLater(() -> viewer.setContent(content));
    }
}
