package SerialComm;

import jssc.*;

import java.util.ArrayList;
import java.util.List;


public class JSSCSerial {


    private SerialPort serial;
    private String receivedData;
    private int BaudRate;

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
    public String getReceivedMessage(){
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
                            //sendToSerial(receivedData);

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
