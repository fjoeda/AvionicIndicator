package SerialComm;

import gnu.io.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.Enumeration;



public class SerialCommunication {



    public static SerialPort serialPort;

    private BufferedReader input;
    private OutputStream output;
    private OutputStream output1;

    private String PortName = null;
    private int BaudRate = 0;
    private String ReceivedMessage;

    CommPortIdentifier portId = null;
    Enumeration portEnum;

    public SerialCommunication(String PortName, int BaudRate){
        this.BaudRate = BaudRate;
        this.PortName = PortName;
    }

    public SerialCommunication(){

    }

    private static final String PORT_NAMES[] = {
            "/dev/tty.usbserial-A9007UX1", // Mac OS X
            "/dev/ttyACM0", // Raspberry Pi
            "/dev/ttyUSB0", // Linux
            "COM3", // Windows
    };

    //Kirim data ke Serial

    public void SendToSerial(String message) {
        try{
            output1.write(message.getBytes());
            output1.flush();

        }catch (Exception e){

        }
    }

    // Menghubungkan ke Serial

    public void connectToSerial() throws Exception {
        System.out.println("Clicked");
        if(PortName!=null&&BaudRate!=0){
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PortName);
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            try{
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(BaudRate,8,1,0);
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                output = serialPort.getOutputStream();
                output1 = output;
                serialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        try {
                            if(event.getEventType()==SerialPortEvent.DATA_AVAILABLE){
                                String data = input.readLine();
                                ReceivedMessage = data;
                                //System.out.println(data);
                            }
                        }catch (IOException e){

                        }

                    }
                });
                serialPort.notifyOnDataAvailable(true);
            }catch (Exception e){

            }
        }else{
            new Alert(Alert.AlertType.ERROR,"You haven't select any COM port or baud rate or both").showAndWait();
        }
    }



    public String getReceivedMessage(){
        return ReceivedMessage;
    }

    public void setBaudRate(int baudRate) {
        BaudRate = baudRate;
    }

    public void setPortName(String portName){
        PortName = portName;
    }

    public void disconnectSerial(){
        serialPort.close();
    }
}
