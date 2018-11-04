package SerialComm;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TestForSerialMonitor extends Application{


    private static AnimationTimer timer;

    long lastTimerCount;


    @Override
    public void init(){


        try{
            JSSCSerial serial = new JSSCSerial("COM3",9600);
            serial.connectToSerial();
            lastTimerCount = System.nanoTime();

            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if(now > lastTimerCount + 1_000_000_000L){
                        System.out.println(serial.getReceivedMessage());
                    }
                }
            };
            serial.sendToSerial("WriteSomething");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        timer.start();
    }
}
