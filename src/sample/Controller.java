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
}
