package sample;

import Avionics.*;
import SerialComm.SerialCommunication;
import TextParser.StringParser;
import Visualize3D.Importer3D;
import Visualize3D.SubSceneContainer;
import Visualize3D.ViewerModel;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import gnu.io.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import javax.sound.sampled.Port;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {
    public Label Status;
    public Label BatteryLabel;
    public static HBox TitleBar;
    public ToggleSwitch tglArm1;
    public ToggleSwitch tglArm2;
    public ToggleSwitch tglPlaneMode;
    public ToggleSwitch tglVtolMode;
    public Button ClearWaypoint;
    public Button SaveWaypoint;
    public Button OpenWaypoint;
    public Button OpenFlightRecord;
    public Button SaveFlightRecord;
    public Button ClearMessageButton;
    public TextArea MessageLog;

    private AirCompass     compass;
    private Horizon        horizon;
    private Altimeter      altimeter;
    private RadialGauge    speedometer;


    public javafx.scene.control.TextField SendSerialText;
    public javafx.scene.control.Button SendButton;
    public javafx.scene.control.TextArea ConsoleText;
    public ComboBox<String> PortList;
    public ComboBox<String> BaudList;
    public ToggleSwitch ConnectButton;
    public MapView mapView;
    public GridPane SecAvionic;
    public Pane Pane3D;
    public SubSceneContainer subSceneContainer;

    private String PortName = null;
    private String BaudRate = null;
    private ViewerModel viewer;
    private Coordinate position;
    private ArrayList<Coordinate> positionList = new ArrayList<>();
    private ArrayList<Marker> markerList = new ArrayList<>();
    private Coordinate positionLast;
    private Coordinate positionNow;
    private ArrayList<Coordinate> routes = new ArrayList<>();
    private boolean isConnectedToSerial = false;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private Marker planeMarker;
    private CoordinateLine coordinateLine;

    private int index = 0;
    private int loadIndex = 0;

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "id,latitude,longitude";

    private static final int WAYPOINT_ID = 0;
    private static final int WAYPOINT_LATITUDE = 1;
    private static final int WAYPOINT_LONGITUDE = 2;

    private int mode = 0;
    private boolean ARMED =false;

    private FileWriter fileWriter = null;
    private BufferedReader fileReader = null;

    private long           lastTimerCall;
    private long           lastRouteDataGot;
    private double         speed;
    private AnimationTimer timer;
    private AnimationTimer routeTimer;

    SerialCommunication serial = null;

    public void SendToSerial(ActionEvent actionEvent) {
        if(serial != null)
        serial.sendToSerial((SendSerialText.getText()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String BING_MAP_API_KEY = "AjBEkbJVIF_enJ7KdZTSXxNgn58ADVVRqFNKbSBeSCmNukw4hQYAAcaIM61q2mGp";
        compass   = new AirCompass();
        horizon   = new Horizon();
        altimeter = new Altimeter();
        speedometer = RadialGaugeBuilder.create()
                .title("")
                .unit("m/s")
                .style("-body: rgb(30, 30, 30); -tick-mark-fill: white; -tick-label-fill: white;")
                .animated(false)
                .maxValue(30)
                .majorTickSpace(2)
                .value(0)
                .build();

        SecAvionic.add(horizon,0,0);
        SecAvionic.add(compass,0,2);
        SecAvionic.add(speedometer,1,0);
        SecAvionic.add(altimeter,1,2);

        PortList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> PortName = newValue);
        BaudList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> BaudRate = newValue);

        ObservableList<String> baudRate = FXCollections.observableArrayList("4800", "9600", "19200", "57600", "115200");

        BaudList.setItems(baudRate);
        BaudList.getSelectionModel().select(1);
        initialize3DScene();

        lastTimerCall = System.nanoTime();
        lastRouteDataGot = System.nanoTime();

        //Initialize Map
        mapView.setBingMapsApiKey(BING_MAP_API_KEY);
        mapView.setMapType(MapType.BINGMAPS_ROAD);
        mapView.setZoom(12);
        mapView.setCenter(new Coordinate(-7.7713847,110.3774998));
        planeMarker = new Marker(getClass().getResource("Object/PlaneIcon.png"))
                .setPosition(new Coordinate(-7.7713847,110.3774998))
                .setVisible(true);


        //Map Event Handler
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            position = event.getCoordinate();
            Marker marker = new Marker(getClass().getResource("Object/marker1.png")).setPosition(position).setVisible(true);
            mapView.addMarker(marker);
            markerList.add(marker);
            System.out.println(event.getCoordinate());
            if(!positionList.contains(position)){
                positionList.add(position);
                waypoints.add(new Waypoint(index,Double.toString(position.getLatitude()),Double.toString(position.getLongitude())));
                index++;
            }

        });

        mapView.initialize();


        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 100_000_000L && serial.getReceivedMessage()!=null){
                    MessageLog.appendText(serial.getReceivedMessage()+System.lineSeparator());
                    MessageLog.appendText("--------------------------------"+System.lineSeparator());
                    refreshOrientation();
                    System.out.println(StringParser.getDataLength(serial.getReceivedMessage()));
                    lastTimerCall = now;
                }
            }
        };


        routeTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now>lastRouteDataGot + 1_000_000_000L && serial.getReceivedMessage()!=null){
                    refreshMapRoute();
                    lastRouteDataGot = now;
                }
            }
        };

        /*
        * Initialize toggle button handlers
        * */
        ConnectButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
           if(newValue){
               if(!isConnectedToSerial){
                   try{
                       if(PortName != null && BaudRate != null){
                           serial = new SerialCommunication(PortName,Integer.valueOf(BaudRate));
                           serial.connectToSerial();
                           isConnectedToSerial = true;
                           routes.add(positionNow);
                           mapView.addMarker(planeMarker);
                           timer.start();
                           routeTimer.start();

                       } else {
                           new Alert(Alert.AlertType.ERROR,"You haven't select any COM port or baud rate or both").showAndWait();
                           ConnectButton.setSelected(false);
                       }
                   }catch (Exception e){
                        System.out.println(e.getMessage());
                   }

               }
           } else {
               if(isConnectedToSerial){
                   serial.disconnectSerial();
                   isConnectedToSerial = false;
                   timer.stop();
                   routeTimer.stop();
               }
           }
        });

        tglArm1.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(ConnectButton.isSelected()){
                if(newValue&&tglArm2.isSelected()){
                    serial.sendToSerial(StringParser.armUAV(1));
                }else if(!newValue&&!tglArm2.isSelected()){
                    serial.sendToSerial(StringParser.armUAV(0));
                }
            }else{
                tglArm1.setSelected(false);
            }
        });

        tglArm2.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(ConnectButton.isSelected()){
                if(newValue&&tglArm1.isSelected()){
                    serial.sendToSerial(StringParser.armUAV(1));
                }else if(!newValue&&!tglArm1.isSelected()){
                    serial.sendToSerial(StringParser.armUAV(0));
                }
            }else{
                tglArm2.setSelected(false);
            }
        });

    }






    public void RefreshPortList(MouseEvent mouseEvent) {
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            if (!PortList.getItems().contains(portId.getName()))
                PortList.getItems().add(portId.getName());
        }
    }



    private void initialize3DScene(){
        viewer = new ViewerModel();
        subSceneContainer.setSubScene(viewer.getSubScene());

        subSceneContainer.prefWidthProperty().bind(Pane3D.widthProperty());
        subSceneContainer.prefHeightProperty().bind(Pane3D.heightProperty());

        load3DFile(getClass().getResource("Object/FW.stl").toExternalForm());
    }

    private void load3DFile(String url){
        viewer.setContent(null);
        System.out.println((new File(url)).exists());
        new Thread(() -> {
            try {
                Group content = Importer3D.load(url);
                handleLoadResult(content);
                System.out.println("Loaded");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleLoadResult(final Group content) {
        Platform.runLater(() -> viewer.setContent(content));
    }

    private void refreshMapRoute(){
        positionNow = new Coordinate(StringParser.getLatitude(serial.getReceivedMessage()),
                StringParser.getLongitude(serial.getReceivedMessage()));

        mapView.setCenter(positionNow);
        mapView.removeMarker(planeMarker);
        planeMarker.setPosition(positionNow).setVisible(true);
        mapView.addMarker(planeMarker);

        try{

            System.out.println(positionNow);
            if(Waypoint.distance(positionNow,positionLast)<1000&&(
                    (positionNow.getLatitude()!= positionLast.getLatitude())||
                    (positionNow.getLongitude()!=positionLast.getLongitude()))){
                routes.add(positionNow);
                mapView.removeCoordinateLine(coordinateLine);
                coordinateLine = new CoordinateLine(new ArrayList<Coordinate>(){{add(positionNow);add(positionLast);}});
                coordinateLine.setColor(Color.YELLOWGREEN)
                        .setVisible(true)
                        .setWidth(7);
                mapView.addCoordinateLine(coordinateLine);
                positionLast = positionNow;
                mapView.setZoom(16);

            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void refreshOrientation(){
        if(StringParser.getDataLength(serial.getReceivedMessage())==13){
            viewer.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
            viewer.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
            try{
                compass.setBearing(StringParser.getYaw(serial.getReceivedMessage()));
                horizon.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
                horizon.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
                altimeter.setValue(StringParser.getAltitude(serial.getReceivedMessage()));
                speedometer.setValue(StringParser.getAirspeed((serial.getReceivedMessage())));
                BatteryLabel.setText(StringParser.getBattery(serial.getReceivedMessage()));
                if(StringParser.getArm(serial.getReceivedMessage())==1){
                    Status.setText("ARMED");
                }else{
                    Status.setText("DISARMED");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public void saveWaypoint(ActionEvent actionEvent) {
        try {
            fileWriter = new FileWriter("Waypoints.csv");

            fileWriter.append(FILE_HEADER.toString());
            fileWriter.append(NEW_LINE_SEPARATOR);

            for (Waypoint waypoint : waypoints) {
                fileWriter.append(String.valueOf(waypoint.getId()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(waypoint.getLatitude()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(waypoint.getLongitude()));
                fileWriter.append(NEW_LINE_SEPARATOR);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Berhasil menyimpan waypoint");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadWaypoint(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        //fileChooser.showOpenDialog();
        waypoints.clear();
        try {
            String line = "";
            fileReader = new BufferedReader(new FileReader("Waypoints.csv"));
            fileReader.readLine();

            while ((line = fileReader.readLine()) != null) {
                String[] tokens = line.split(COMMA_DELIMITER);

                if (tokens.length > 0) {
                    Coordinate pos = new Coordinate(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2]));
                    ///Tambah marker
                    Waypoint waypoint = new Waypoint(Integer.valueOf(tokens[0]), Double.toString(pos.getLatitude()), Double.toString(pos.getLongitude()));
                    waypoints.add(waypoint);
                    System.out.println(String.valueOf(waypoint.getId()) + ", " + String.valueOf(waypoint.getLatitude()) + ", " + String.valueOf(waypoint.getLongitude()));
                    loadIndex++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void openFlightRecord(ActionEvent actionEvent) {
    }

    public void saveFlightRecord(ActionEvent actionEvent) {
    }

    public void clearWaypoint(ActionEvent actionEvent) {
        for(Marker marker : markerList){
            mapView.removeMarker(marker);
        }
        waypoints.clear();
        positionList.clear();
        index = 0;
    }

    public void ClearMessageLog(ActionEvent actionEvent) {
        MessageLog.clear();
    }
}