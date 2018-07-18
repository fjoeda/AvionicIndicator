package FlyingLog;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class TestCSV extends Application {

    private CSV_Log writer;
    private AnimationTimer timer;
    private long lastTimerCount;

    @Override
    public void init() throws Exception {
        writer = new CSV_Log(LocalDateTime.now().getDayOfWeek().toString()+"_flight_log");
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCount + 1_000_000_000L){
                    writer.CSV_Write(LocalDateTime.now().toString(),"11.4","10","23"
                            ,"23","23","7.84567","109.1203","22","Cruise");
                    System.out.println("logging");
                    lastTimerCount = now;
                }
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        timer.start();
    }
}
