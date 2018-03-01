package sample;


import SerialComm.SerialCommunication;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    SerialCommunication serial;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // tempat buat inisiasi objek kompas dan lain lain
        serial = new SerialCommunication();
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
