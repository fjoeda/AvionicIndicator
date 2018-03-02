package sample;

import Avionics.AirCompass;
import Avionics.Altimeter;
import Avionics.Horizon;
import SerialComm.SerialCommunication;
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

    public static SerialPort serialPort;

    private BufferedReader input;
    private OutputStream output;

    private String PortName = null;
    private String BaudRate = null;

    private String data = "";

    private long           lastTimerCall;
    private AnimationTimer timer;



    SerialCommunication serial;

    public void SendToSerial(ActionEvent actionEvent) {
        try {
            output.write(SendSerialText.getText().getBytes());
            ConsoleText.appendText(SendSerialText.getText() + System.lineSeparator());
            SendSerialText.clear();
            output.wait();
        } catch (Exception e){
            System.out.println("Error send to serial : " + e.toString() +
                    "\n" + e.getMessage());
        }
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
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PortName);
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            try {
                serialPort = (SerialPort) commPort;

                serialPort.setSerialPortParams(Integer.valueOf(BaudRate),8,1,0);

                output = serialPort.getOutputStream();
                //input = serialPort.getInputStream();
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

                serialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        try {
                            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                                /*data = input.toString();
                                ConsoleText.appendText(data + System.lineSeparator());
                                input.close();*/
                                String data = input.readLine();
                                ConsoleText.appendText(data + System.lineSeparator());
                                System.out.println(data);
                                compass.setBearing(ParseNilaiYaw(data));
                                horizon.setPitch(ParseNilaiPitch(data));
                                horizon.setRoll(ParseNilaiRoll(data));
                                altimeter.setValue(ParseNilaiAltitude(data));
                            }
                        } catch (IOException e) {
                            //Log.debug("Catch 1 : " + e.toString());
                        }
                    }
                });

                serialPort.notifyOnDataAvailable(true);

            } catch (Exception e) {
                //Log.debug("Catch 2 : " + e.toString());
            }
        } else {
            new Alert(Alert.AlertType.ERROR,"You haven't select any COM port or baud rate or both").showAndWait();
        }
    }

    public Double ParseNilaiAltitude(String dapetData){
        String[] parts = dapetData.split("#");
        String altitude = parts[1];
        Double nilai_altitude = Double.valueOf(altitude);
        return nilai_altitude;
    }
    public Double ParseNilaiYaw(String dapetData){
        String[] parts = dapetData.split("#");
        String yaw = parts[2];
        Double nilai_yaw = Double.valueOf(yaw);
        return nilai_yaw;

    }
    public Double ParseNilaiPitch(String dapetData){
        String[] parts = dapetData.split("#");
        String pitch = parts[3];
        Double nilai_pitch = Double.valueOf(pitch);
        return nilai_pitch;
    }
    public Double ParseNilaiRoll(String dapetData){
        String[] parts = dapetData.split("#");
        String roll = parts[4];
        Double nilai_roll = Double.valueOf(roll);
        return nilai_roll;
    }
    public Double ParseNilaiLatitude(String dapetData){
        String[] parts = dapetData.split("#");
        String latitude = parts[5];
        Double nilai_latitude = Double.valueOf(latitude);
        return nilai_latitude;
    }
    public Double ParseNilaiLongitude(String dapetData){
        String[] parts = dapetData.split("#");
        String longitude = parts[6];
        Double nilai_longitude = Double.valueOf(longitude);
        return nilai_longitude;
    }

}
