package SerialComm;

import javafx.application.Platform;
import jssc.*;

import java.util.ArrayList;
import java.util.List;


public class JSSCSerial {


    private SerialPort serial;
    volatile String receivedData = "";
    public String waypointMessage;
    private int BaudRate;
    private byte[] data;

    Boolean receivingMessage = false;

    public static ArrayList<String> getPortList(){
        String portList[] = SerialPortList.getPortNames();
        ArrayList<String> result = new ArrayList<String>();
        for(String port : portList){
            result.add(port);
        }
        return result;
    }

    public JSSCSerial(String PortName,int BaudRate) throws jssc.SerialPortException{
        serial = new SerialPort(PortName);
        this.BaudRate = BaudRate;

    }

    public String getStringfromByte(byte[] bytes){
        StringBuilder message = new StringBuilder();
        for(byte b:bytes){
            if ( (b == '\r' || b == '\n') && message.length() > 0) {
                String toProcess = message.toString();
                message.setLength(0);
            }
            else {
                message.append((char)b);
            }
        }
        return message.toString();
    }

    public String getReceivedMessage(){
        //receivedData = getStringfromByte(data);
        return receivedData;
    }

    public void sendToSerial(String message) {
        try {
            serial.writeBytes(message.getBytes());
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void connectToSerial(){
        try {
            serial.openPort();
            serial.setParams(BaudRate,8,1,SerialPort.PARITY_NONE);
            serial.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            serial.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent event) {
                    if(event.isRXCHAR() && event.getEventValue() > 0) {
                        try {
                            receivedData = serial.readString(event.getEventValue());
                        }
                        catch (SerialPortException ex) {
                            System.out.println("Error in receiving string from COM-port: " + ex);
                        }
                    }
                }
            });
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String str){
        receivedData = str;
    }

    public void disconnectSerial(){

        if(serial.isOpened()) {
            try {
                serial.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }


}
