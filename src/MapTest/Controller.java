package MapTest;

import SerialComm.SerialCommunication;
import TextParser.StringParser;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapView;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.swing.text.Position;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    public Button goButton;
    public TextField lngText;
    public TextField latText;
    public MapView mapView;
    public Button clearButton;
    public Button clearconnectButton;
    private Coordinate position;
    private ArrayList<Coordinate> positionList = new ArrayList<>();
    private SerialCommunication serial;
    private AnimationTimer timer;

    long lastTimerCall;
    Coordinate positionLast;
    Coordinate positionNow;


    ArrayList<Coordinate> routes = new ArrayList<>();

    public void buttonClick(ActionEvent actionEvent) {
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        

        serial = new SerialCommunication();
        serial.setPortName("COM14");
        serial.setBaudRate(9600);
        lastTimerCall = System.nanoTime();



        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 1_000_000_000L && serial.getReceivedMessage()!=null){

                    try{
                        positionNow = new Coordinate(StringParser.getLatitude(serial.getReceivedMessage()),
                                StringParser.getLongitude(serial.getReceivedMessage()));
                        System.out.println(positionNow);
                        if((positionNow.getLatitude()!= positionLast.getLatitude())||(positionNow.getLongitude()!=positionLast.getLongitude())){
                            routes.add(positionNow);
                            positionLast = positionNow;
                            System.out.println(serial.getReceivedMessage());


                        }


                    }catch (Exception e){

                    }
                    lastTimerCall=now;


                }
            }
        };

    }

    public void clearWaypoint(ActionEvent actionEvent) {
        //clear marker

    }

    public void connectSerial(ActionEvent actionEvent) {
        try {
            serial.connectToSerial();
            timer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
