package FlyingLog;

import java.io.BufferedReader;
import java.io.FileWriter;

public class CSV_Log {
    FileWriter fileWriter = null;
    BufferedReader bufferedReader = null;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String HEADER = "Tanggal,Baterai,Altitude,Roll,Pitch,Yaw,Latitude,Longitude,Speed,Status";

    public void CSV_Write(String Tanggal, String Baterai, String Altitude, String Roll,
                          String Pitch, String Yaw, String Latitude, String Longitude,
                          String Speed, String Status){
        try {
            fileWriter = new FileWriter("FlyingLog.csv");
            fileWriter.append(HEADER);
            fileWriter.append(NEW_LINE_SEPARATOR);
            fileWriter.append(Tanggal);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Baterai);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Altitude);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Roll);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Pitch);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Yaw);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Latitude);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Longitude);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Speed);
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(Status);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void CSV_Reader(){

    }
}
