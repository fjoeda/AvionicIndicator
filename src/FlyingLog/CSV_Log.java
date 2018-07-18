package FlyingLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;

import static java.nio.file.attribute.PosixFilePermission.*;

public class CSV_Log {
    FileWriter fileWriter = null;
    BufferedReader bufferedReader = null;
    private String filename;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String HEADER = "Tanggal,Baterai,Altitude,Roll,Pitch,Yaw,Latitude,Longitude,Speed,Status";

    public CSV_Log(String filename) throws IOException{
        //this.filename = filename;
        File file = new File(filename);
        file.setExecutable(true);
        file.setWritable(true);
        file.setReadable(true);
        //Files.setPosixFilePermissions(file.toPath(), EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE));
        fileWriter = new FileWriter(file);
        fileWriter.append(HEADER);
    }

    public void CSV_Write(String Tanggal, String Baterai, String Altitude, String Roll,
                          String Pitch, String Yaw, String Latitude, String Longitude,
                          String Speed, String Status){
        try {
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
                //fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void CSV_Reader(){

    }
}
