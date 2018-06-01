package sample;

import Avionics.*;
import SerialComm.SerialCommunication;
import TextParser.StringParser;
import Visualize3D.Importer3D;
import Visualize3D.SubSceneContainer;
import Visualize3D.ViewerModel;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import gnu.io.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

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

public class Controller implements Initializable, MapComponentInitializedListener{
    private static final Random RND = new Random();
    public Label Status;
    public Label BatteryLabel;

    private AirCompass     compass;
    private Horizon        horizon;
    private Altimeter      altimeter;
    private RadialGauge    speedometer;

    @FXML
    public javafx.scene.control.TextField SendSerialText;
    public javafx.scene.control.Button SendButton;
    public javafx.scene.control.TextArea ConsoleText;
    public MenuButton MainMenu;
    public MenuItem LoadWaypoint;
    public MenuItem SaveWaypoint;
    public ComboBox<String> PortList;
    public ComboBox<String> BaudList;
    public Button ConnectButton;
    public GoogleMapView mapView;
    public GridPane SecAvionic;
    public Pane Pane3D;
    public SubSceneContainer subSceneContainer;

    private String PortName = null;
    private String BaudRate = null;
    private ViewerModel viewer;
    private GoogleMap map;
    private LatLong position;
    private ArrayList<LatLong> positionList = new ArrayList<>();
    private LatLong positionLast;
    private LatLong positionNow;
    private MVCArray mvc;
    private PolylineOptions polylineOptions;
    private ArrayList<LatLong> routes = new ArrayList<>();
    private boolean isConnectedToSerial = false;

    private ArrayList<Waypoint> waypoints = new ArrayList<>();

    private int index = 0;
    private int loadIndex = 0;

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "id,latitude,longitude";

    private static final int WAYPOINT_ID = 0;
    private static final int WAYPOINT_LATITUDE = 1;
    private static final int WAYPOINT_LONGITUDE = 2;

    FileWriter fileWriter = null;
    BufferedReader fileReader = null;

    private long           lastTimerCall;
    private long           lastSpeedDataGot;
    private double         speed;
    private AnimationTimer timer;
    private AnimationTimer speedometerTimer;

    SerialCommunication serial = null;

    public void SendToSerial(ActionEvent actionEvent) {
        if(serial != null)
        serial.SendToSerial((SendSerialText.getText())+System.lineSeparator());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        compass   = new AirCompass();
        horizon   = new Horizon();
        altimeter = new Altimeter();
        speedometer = RadialGaugeBuilder.create()
                .title("")
                .unit("Km/h")
                .style("-body: rgb(30, 30, 30); -tick-mark-fill: white; -tick-label-fill: white;")
                .animated(false)
                .maxValue(200)
                .majorTickSpace(20)
                .build();

        mapView.addMapInializedListener(this);
        SecAvionic.add(horizon,0,0);
        SecAvionic.add(compass,0,1);
        SecAvionic.add(speedometer,1,0);
        SecAvionic.add(altimeter,1,1);

        PortList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> PortName = newValue);
        BaudList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> BaudRate = newValue);

        ObservableList<String> baudRate = FXCollections.observableArrayList("4800", "9600", "19200", "57600", "115200");

        BaudList.setItems(baudRate);
        BaudList.getSelectionModel().select(1);
        initialize3DScene();

        lastTimerCall = System.nanoTime();
        lastSpeedDataGot = System.nanoTime();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 100_000_000L && serial.getReceivedMessage()!=null){
                    System.out.println(serial.getReceivedMessage());
                    refreshMapRoute();
                    refreshOrientation();
                    System.out.println(StringParser.getDataLength(serial.getReceivedMessage()));
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override
    public void mapInitialized() {
        //LatLong MyPosition = new LatLong(-7.8891243,113.71372344);
        LatLong MyPosition = new LatLong(-7.7692766,110.3736047);
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(MyPosition).mapType(MapTypeIdEnum.SATELLITE)
                .zoom(12)
                .mapTypeControl(false)
                .streetViewControl(false)
                .rotateControl(false)
                .scaleControl(false);

        map = mapView.createMap(mapOptions);

        map.addMouseEventHandler(UIEventType.click,(GMapMouseEvent event)->{
            position = event.getLatLong();
            map.addMarker((new Marker((new MarkerOptions()).position(position))));
            if(!positionList.contains(position)) {
                positionList.add(position);

                Waypoint waypoint = new Waypoint(index, Double.toString(position.getLatitude()), Double.toString(position.getLongitude()));
                waypoints.add(waypoint);

                index++;
            }
        });

        positionLast = new LatLong(1.32,0.23);

        mvc = new MVCArray(routes.toArray());
        polylineOptions = new PolylineOptions()
                .path(mvc)
                .strokeColor("blue")
                .strokeWeight(2);
        Polyline poly = new Polyline(polylineOptions);
        map.addMapShape(poly);
        speedometerTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 100_000_000L && serial.getReceivedMessage()!=null){
                    speed = (Math.abs(positionNow.distanceFrom(positionLast)))*3.6/0.1;
                    speedometer.setValue(speed);
                    lastSpeedDataGot = now;
                }
            }
        };
    }

    public void RefreshPortList(MouseEvent mouseEvent) {
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            if (!PortList.getItems().contains(portId.getName()))
                PortList.getItems().add(portId.getName());
        }
    }

    public void ConnectToSerial(ActionEvent actionEvent) throws Exception {
        if(!isConnectedToSerial){
            if(PortName != null && BaudRate != null){
                serial = new SerialCommunication(PortName,Integer.valueOf(BaudRate));
                serial.connectToSerial();
                isConnectedToSerial = true;
                ConnectButton.setText("Disconnect");
                timer.start();
                speedometerTimer.start();

            } else {
                new Alert(Alert.AlertType.ERROR,"You haven't select any COM port or baud rate or both").showAndWait();
            }
        }else{
            serial.disconnectSerial();
            ConnectButton.setText("Connect");
            isConnectedToSerial = false;
            timer.stop();
            speedometerTimer.stop();
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
        positionNow = new LatLong(StringParser.getLatitude(serial.getReceivedMessage()),
                StringParser.getLongitude(serial.getReceivedMessage()));

        map.setCenter(positionNow);
        try{

            System.out.println(positionNow);
            if((positionNow.getLatitude()!= positionLast.getLatitude())||(positionNow.getLongitude()!=positionLast.getLongitude())){
                routes.add(positionNow);
                positionLast = positionNow;
                map.setCenter(positionNow);
                map.setZoom(16);
                mvc.push(positionNow);
            }
        } catch (Exception e){

        }
    }

    private void refreshOrientation(){
        viewer.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
        viewer.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
        try{
            compass.setBearing(StringParser.getYaw(serial.getReceivedMessage()));
            horizon.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
            horizon.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
            altimeter.setValue(StringParser.getAltitude(serial.getReceivedMessage()));
        } catch (Exception e) {

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
        map.clearMarkers();

        try {
            String line = "";

            fileReader = new BufferedReader(new FileReader("Waypoints.csv"));
            fileReader.readLine();

            while ((line = fileReader.readLine()) != null) {
                String[] tokens = line.split(COMMA_DELIMITER);
                if (tokens.length > 0) {
                    Waypoint waypoint = new Waypoint(index, Double.toString(position.getLatitude()), Double.toString(position.getLongitude()));
                }
            }

            for (Waypoint waypoint : waypoints) {
                //map.addMarker((new Marker((new MarkerOptions()).position(positionList.get(loadIndex)))));
                LatLong location = new LatLong(Double.parseDouble(waypoint.getLatitude()), Double.parseDouble(waypoint.getLongitude()));
                map.addMarker((new Marker((new MarkerOptions()).position(location))));
                System.out.println(String.valueOf(waypoint.getId()) + ", " + String.valueOf(waypoint.getLatitude()) + ", " + String.valueOf(waypoint.getLongitude()));
                loadIndex++;
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
        map.clearMarkers();
        positionList.clear();
        index = 0;
    }
}