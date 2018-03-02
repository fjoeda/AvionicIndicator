package sample;


import Avionics.AirCompass;
import Avionics.Altimeter;
import Avionics.Horizon;
import SerialComm.SerialCommunication;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    private static final Random RND = new Random();

    private AirCompass     compass;
    private Horizon        horizon;
    private Altimeter      altimeter;

    private long           lastTimerCall;
    private AnimationTimer timer;

    @FXML
    public HBox avionics;

    SerialCommunication serial;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        compass   = new AirCompass();
        horizon   = new Horizon();
        altimeter = new Altimeter();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 5_000_000_000l) {
                    compass.setBearing(RND.nextInt(360));
                    horizon.setPitch(RND.nextInt(90) - 45);
                    horizon.setRoll(RND.nextInt(90) - 45);
                    altimeter.setValue(RND.nextInt(20000));
                    lastTimerCall = now;
                }
            }
        };

        avionics.getChildren().addAll(compass, horizon, altimeter);

        timer.start();
    }
     public void ParseData(){
        //silahkan pakai nilai_x untuk string type, atau int_nilai_x untuk integer type.
        String serialData = serial.getReceivedMessage();
        String[] parts = serialData.split("#");
        String nilai_roll = parts[1];
        Integer int_nilai_roll = Integer.valueOf(nilai_roll);
        String nilai_pitch = parts[2];
        Integer int_nilai_pitch = Integer.valueOf(nilai_pitch);
        String nilai_yaw = parts[3];
        Integer int_nilai_yaw = Integer.valueOf(nilai_yaw);
        String nilai_altitude = parts[4];
        Integer int_nilai_altitude = Integer.valueOf(nilai_altitude);
    }
}
