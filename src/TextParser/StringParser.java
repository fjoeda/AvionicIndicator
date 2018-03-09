package TextParser;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import SerialComm.SerialCommunication;

public class StringParser{



    SerialCommunication serial = new SerialCommunication();

    public static Double getAltitude(String data){
        return (Double.valueOf(data.split("#")[1]));
    }

    public static Double getYaw(String data){
        return (Double.valueOf(data.split("#")[2]));
    }

    public static Double getPitch(String data){
        return (Double.valueOf(data.split("#")[3]));
    }

    public static Double getRoll(String data){
        return (Double.valueOf(data.split("#")[4]));
    }

    public static Double getLatitude(String data){
        return (Double.valueOf(data.split("#")[5]));
    }

    public static Double getLongitude(String data){
        return (Double.valueOf(data.split("#")[6]));
    }

    public StringParser(){

    }


}
