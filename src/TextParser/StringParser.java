package TextParser;

import javafx.fxml.Initializable;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;

import SerialComm.SerialCommunication;

public class StringParser{





    public static double getAltitude(String data){
        return (Double.valueOf(data.split("#")[1]));
    }

    public static double getYaw(String data){
        return (Double.valueOf(data.split("#")[2]));
    }

    public static double getPitch(String data){
        return (Double.valueOf(data.split("#")[3]));
    }

    public static double getRoll(String data){
        return (Double.valueOf(data.split("#")[4]));
    }

    public static double getLatitude(String data){
        Number number = null;
        try {
            number =(NumberFormat.getInstance(Locale.US)).parse(data.split("#")[5]);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (Double.parseDouble(number.toString()));
    }

    public static double getLongitude(String data){
        Number number = null;
        try {
            number =(NumberFormat.getInstance(Locale.US)).parse(data.split("#")[6]);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (Double.parseDouble(number.toString()));
    }


    public static int getDataLength(String data){

        return (data.split("#").length);}

    public StringParser(){

    }


}
