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
        return (Double.parseDouble((data.split("#"))[5]));
    }

    public static double getLongitude(String data){
        return (Double.parseDouble((data.split("#"))[6]));
    }

    public static double getAirspeed(String data){
        return (Double.parseDouble((data.split("#"))[7]));
    }

    public static String getBattery(String data){
        return ((data.split("#"))[8]);
    }

    public static int getMode(String data){
        return (Integer.parseInt((data.split("#"))[9]));
    }

    public static int getCommand(String data){
        return (Integer.parseInt((data.split("#"))[10]));
    }

    public static int getAuto(String data){return (Integer.parseInt((data.split("#"))[11]));}

    public static int getArm(String data){ return (Integer.parseInt((data.split("#"))[12])); }

    public static String sentData(int mode, int command){ return ("@#"+String.valueOf(mode)+"#"+String.valueOf(command)+"#*"); }

    public static String armUAV(int mode){ return("a#"+String.valueOf(mode)+"#*"); }

    public static String setAutoUAV(int mode){ return("u#"+String.valueOf(mode)+"#*"); }


    public static int getDataLength(String data){ return (data.split("#").length);}

    public static String getTimeFormatFromSecond(int second){
        int mins, secs;
        String strMins, strSecs;
        mins = second / 60;
        secs = second % 60;
        if(mins<10)
            strMins = 0+String.valueOf(mins);
        else
            strMins = String.valueOf(mins);

        if(secs<10)
            strSecs = 0+String.valueOf(secs);
        else
            strSecs = String.valueOf(secs);

        return strMins+":"+strSecs;
    }

    public static String getDistanceString(int distance){
        return String.valueOf(distance)+" m";
    }

    public StringParser(){

    }


}
