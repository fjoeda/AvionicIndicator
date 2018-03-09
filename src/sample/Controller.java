package sample;

import Avionics.AirCompass;
import Avionics.Altimeter;
import Avionics.Horizon;
import SerialComm.SerialCommunication;
import TextParser.StringParser;
import gnu.io.*;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.sound.sampled.Port;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Enumeration;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    private static final Random RND = new Random();

    private AirCompass     compass;
    private Horizon        horizon;
    private Altimeter      altimeter;


    @FXML
    public HBox avionics;
    public javafx.scene.control.TextField SendSerialText;
    public javafx.scene.control.Button SendButton;
    public javafx.scene.control.TextArea ConsoleText;
    public ComboBox<String> PortList;
    public ComboBox<String> BaudList;
    public Button ConnectButton;


    private String PortName = null;
    private String BaudRate = null;



    private long           lastTimerCall;
    private AnimationTimer timer;


    SerialCommunication serial = null;

    public void SendToSerial(ActionEvent actionEvent) {
        if(serial != null)
        serial.SendToSerial((SendSerialText.getText())+System.lineSeparator());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        compass   = new AirCompass();
        horizon   = new Horizon();
        altimeter = new Altimeter();

        avionics.getChildren().addAll(compass, horizon, altimeter);

        PortList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> PortName = newValue);
        BaudList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> BaudRate = newValue);

        ObservableList<String> baudRate = FXCollections.observableArrayList("4800", "9600", "19200", "57600", "115200");

        BaudList.setItems(baudRate);
        BaudList.getSelectionModel().select(1);

        lastTimerCall = System.nanoTime();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 1_000_000_000L && serial.getReceivedMessage()!=null){
                    System.out.println(serial.getReceivedMessage());
                    try{
                        compass.setBearing(StringParser.getYaw(serial.getReceivedMessage()));
                        horizon.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
                        horizon.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
                        altimeter.setValue(StringParser.getAltitude(serial.getReceivedMessage()));
                        ConsoleText.appendText((serial.getReceivedMessage()+System.lineSeparator()));
                    }catch (Exception e){

                    }

                }
            }
        };

    }

    public void RefreshPortList(MouseEvent mouseEvent) {
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            if (!PortList.getItems().contains(portId.getName()))
                PortList.getItems().add(portId.getName());
        }
    }

    public void ConnectToSerial(ActionEvent actionEvent) throws Exception {
        if(PortName != null && BaudRate != null){
            serial = new SerialCommunication(PortName,Integer.valueOf(BaudRate));
            serial.connectToSerial();
            timer.start();
        } else {
            new Alert(Alert.AlertType.ERROR,"You haven't select any COM port or baud rate or both").showAndWait();
        }
    }



}
