package sample;

import Avionics.*;
import SerialComm.SerialCommunication;
import TextParser.StringParser;
import Visualize3D.Importer3D;
import Visualize3D.SubSceneContainer;
import Visualize3D.ViewerModel;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import gnu.io.CommPortIdentifier;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

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
    public Label FlightDistance;
    public Label FlightTime;
    public ToggleSwitch tglStab;
    public ToggleSwitch tglAlt;
    public ToggleSwitch tglHead;
    public ToggleSwitch tglWay;
    public ToggleSwitch tglAuto;
    public Button MinimizeButton;
    public Button CloseButton;
    public Button SendWaypoint;
    public ToggleSwitch tglTakeOff;

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
    private ArrayList<Marker> trackedMarkerList = new ArrayList<>();
    private Coordinate positionLast;
    private Coordinate positionNow;
    private ArrayList<Coordinate> routes = new ArrayList<>();
    private boolean isConnectedToSerial = false;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private Marker planeMarker;
    private CoordinateLine coordinateLine;
    private int flightDistance = 0;
    private boolean afterConnect = false;
    private int flightTime = 0;

    private int index = 0;
    private int loadIndex = 0;

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "id,latitude,longitude";

    private ArrayList<String> csvLine = new ArrayList<>();

    private int MODE = 0;
    private int COMMAND = 0;
    private int AUTO = 0;
    private int ARMED =0;

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
        planeMarker = new Marker(getClass().getResource("Object/PlaneIcon.png"),-15,-20)
                .setPosition(new Coordinate(-7.7713847,110.3774998))
                .setVisible(true);


        //Map Event Handler
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            position = event.getCoordinate();
            addMarker(position);
        });

        mapView.initialize();


        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 100_000_000L && serial.getReceivedMessage()!=null){

                    refreshOrientation();
                    //System.out.println(StringParser.getDataLength(serial.getReceivedMessage()));
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
                    flightTime++;
                    FlightTime.setText(StringParser.getTimeFormatFromSecond(flightTime));
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
                           //routes.add(positionLast);
                           mapView.addMarker(planeMarker);
                           timer.start();
                           //routeTimer.start();
                           tglArm1.setDisable(!newValue);
                           tglArm2.setDisable(!newValue);

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
                   afterConnect = false;
                   routes.clear();
                   tglArm1.setDisable(newValue);
                   tglArm2.setDisable(newValue);
                   // save dulu
                   System.gc();
               }
           }
        });

        tglTakeOff.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                serial.sendToSerial("t");
            }else{
                serial.sendToSerial("l");
            }
        });

        tglArm1.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isConnectedToSerial){
                if(newValue&&tglArm2.isSelected()){
                    arm();
                }else if(!newValue&&!tglArm2.isSelected()){
                    disarm();

                }
            }else{
                tglArm1.setSelected(false);
            }
        });

        tglArm2.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isConnectedToSerial){
                if(newValue&&tglArm1.isSelected()){
                    arm();
                }else if(!newValue&&!tglArm1.isSelected()){
                    disarm();
                }
            }else{
                tglArm2.setSelected(false);
            }
        });

        tglAuto.selectedProperty().addListener((observable, oldValue, newValue) -> {
            tglTakeOff.setDisable(!newValue);
            tglAlt.setDisable(!newValue);
            tglWay.setDisable(!newValue);
            tglStab.setDisable(!newValue);
            tglHead.setDisable(!newValue);
            tglPlaneMode.setDisable(!newValue);
            tglVtolMode.setDisable(!newValue);
            serial.sendToSerial(StringParser.setAutoUAV(newValue? 1 : 0));
        });

        tglPlaneMode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                tglVtolMode.setSelected(false);
                MODE = 1;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });

        tglVtolMode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                tglPlaneMode.setSelected(false);
                MODE = 0;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });

        tglHead.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                COMMAND = 3;
                tglAlt.setSelected(false);
                tglStab.setSelected(false);
                tglWay.setSelected(false);
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }else if(COMMAND==3){
                COMMAND = 0;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });
        tglAlt.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                COMMAND = 2;
                tglStab.setSelected(false);
                tglWay.setSelected(false);
                tglHead.setSelected(false);
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));


            }else if(COMMAND==2){
                COMMAND = 0;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });

        tglStab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                COMMAND = 1;
                tglWay.setSelected(false);
                tglHead.setSelected(false);
                tglAlt.setSelected(false);
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));

            }else if(COMMAND==1){
                COMMAND = 0;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });

        tglWay.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                COMMAND = 4;
                tglStab.setSelected(false);
                tglHead.setSelected(false);
                tglAlt.setSelected(false);
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));

            }else if(COMMAND==4){
                COMMAND = 0;
                serial.sendToSerial(StringParser.sentData(MODE,COMMAND));
            }
        });

        tglArm1.setDisable(!ConnectButton.isSelected());
        tglArm2.setDisable(!ConnectButton.isSelected());
        tglAlt.setDisable(!tglAuto.isSelected());
        tglWay.setDisable(!tglAuto.isSelected());
        tglStab.setDisable(!tglAuto.isSelected());
        tglHead.setDisable(!tglAuto.isSelected());
        tglPlaneMode.setDisable(!tglAuto.isSelected());
        tglVtolMode.setDisable(!tglAuto.isSelected());
        tglTakeOff.setDisable(!tglAuto.isSelected());


    }


    private void arm(){
        ARMED = 1;
        serial.sendToSerial(StringParser.armUAV(ARMED));
        routeTimer.start();
        //ConnectButton.setDisable(true);
    }

    private void disarm(){
        ARMED = 0;
        serial.sendToSerial(StringParser.armUAV(ARMED));
        routeTimer.stop();
        //ConnectButton.setDisable(false);
        //save flight log

    }



    private void recordFlightData(LocalDateTime  time, Coordinate position, String batteryLevel ,double speed, double altitude, int flightTime){
        String LineToSave = time.toLocalDate().toString()+","+time.toLocalTime().toString()+","
                +position.getLatitude()+","+position.getLongitude()+","+String.valueOf(batteryLevel)+","
                +String.valueOf(speed)+","+String.valueOf(altitude)+","+flightTime;
        System.out.println(LineToSave);
        csvLine.add(LineToSave);

    }

    public void RefreshPortList(MouseEvent mouseEvent) {
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            if (!PortList.getItems().contains(portId.getName()))
                PortList.getItems().add(portId.getName());
        }
    }

    private void addMarker(Coordinate position){
        Marker marker = new Marker(getClass().getResource("Object/marker1.png"),-10,-10).setPosition(position).setVisible(true);
        mapView.addMarker(marker);
        markerList.add(marker);
        if(!positionList.contains(position)){
            positionList.add(position);
            waypoints.add(new Waypoint(index,Double.toString(position.getLatitude()),Double.toString(position.getLongitude())));
            index++;
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


        mapView.removeMarker(planeMarker);
        planeMarker.setPosition(positionNow).setVisible(true);
        mapView.addMarker(planeMarker);
        recordFlightData(LocalDateTime.now(),positionNow,StringParser.getBattery(serial.getReceivedMessage()),
                StringParser.getAirspeed(serial.getReceivedMessage()),StringParser.getAltitude(serial.getReceivedMessage()),flightTime);
        System.gc();
        if(!afterConnect){
            positionLast = positionNow;
            afterConnect = true;
        }

        try{
            if(Waypoint.distance(positionNow,positionLast)<2000&&(
                    (positionNow.getLatitude()!= positionLast.getLatitude())||
                    (positionNow.getLongitude()!=positionLast.getLongitude()))){

                routes.add(positionNow);
                mapView.setCenter(positionNow);
                flightDistance += Waypoint.distance(positionNow,positionLast);
                FlightDistance.setText(StringParser.getDistanceString(flightDistance));
                coordinateLine = new CoordinateLine(routes);
                coordinateLine.setColor(Color.DARKBLUE)
                        .setVisible(true)
                        .setWidth(4);
                mapView.addCoordinateLine(coordinateLine);
                addTrackedMarker(positionNow);
                positionLast = positionNow;
                mapView.setZoom(18);

            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void refreshOrientation(){
        if(StringParser.getDataLength(serial.getReceivedMessage())==14){
            viewer.setPitch(StringParser.getPitch(serial.getReceivedMessage()));
            viewer.setRoll(StringParser.getRoll(serial.getReceivedMessage()));
            try{
                MessageLog.appendText(serial.getReceivedMessage()+System.lineSeparator());
                MessageLog.appendText("--------------------------------"+System.lineSeparator());
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

    private void addTrackedMarker(Coordinate position){
        for(Marker waypoint : markerList){
            if(Waypoint.distance(position,waypoint.getPosition())<5){
                System.out.println("waypoint near");
                if(trackedMarkerList.isEmpty()){
                    mapView.removeMarker(waypoint);
                    addTrackedMarkerToMap(position);
                    markerList.remove(waypoint);
                }else{
                    for(Marker tracked : trackedMarkerList){
                        if(!waypoint.getPosition().equals(tracked.getPosition())){
                            mapView.removeMarker(waypoint);
                            addTrackedMarkerToMap(position);
                            markerList.remove(waypoint);
                            break;
                        }
                    }
                }

            }

        }
    }

    private void addTrackedMarkerToMap(Coordinate position){
        Marker marker = new Marker(getClass().getResource("Object/marker2.png"),-10,-10).setPosition(position).setVisible(true);
        mapView.addMarker(marker);
        trackedMarkerList.add(marker);
        System.out.println("added");
    }


    /*
    private File getFileDirectoryFromFileChooser(ActionEvent ae, String title,int mode){

    }*/

    public void saveFlightLog(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gamaforce GCS Flight Record","*.ggfr")
        );
        fileChooser.setTitle("Save Flight Record");
        File file = fileChooser.showSaveDialog(((Node)actionEvent.getSource()).getScene().getWindow());
        try{
            fileWriter = new FileWriter(file.toString());
            for(String lines: csvLine){
                fileWriter.append(lines);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
            csvLine.clear();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveWaypoint(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gamaforce GCS Waypoint","*.ggwp")
        );
        fileChooser.setTitle("Save Waypoint");
        File file = fileChooser.showSaveDialog(((Node)actionEvent.getSource()).getScene().getWindow());
        try {
            fileWriter = new FileWriter(file.toString());

            fileWriter.append(FILE_HEADER);
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
        Node node = (Node) actionEvent.getSource();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Waypoint");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gamaforce GCS Waypoint","*.ggwp")
        );
        File file = fileChooser.showOpenDialog(node.getScene().getWindow());
        waypoints.clear();
        try {
            if(file.exists()){
                String line = "";
                fileReader = new BufferedReader(new FileReader(file.toString()));
                fileReader.readLine();

                while ((line = fileReader.readLine()) != null) {
                    String[] tokens = line.split(COMMA_DELIMITER);

                    if (tokens.length > 0) {
                        Coordinate pos = new Coordinate(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2]));
                        addMarker(pos);
                        Waypoint waypoint = new Waypoint(Integer.valueOf(tokens[0]), Double.toString(pos.getLatitude()), Double.toString(pos.getLongitude()));
                        waypoints.add(waypoint);
                        System.out.println(String.valueOf(waypoint.getId()) + ", " + String.valueOf(waypoint.getLatitude()) + ", " + String.valueOf(waypoint.getLongitude()));
                        loadIndex++;
                    }
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

    public void openFlightRecord() {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/InteractiveLog/InteractiveLog.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Log");
            stage.setScene(new Scene(root));
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveFlightRecord(ActionEvent actionEvent) {
    }

    public void clearWaypoint() {
        for(Marker marker : markerList){
            mapView.removeMarker(marker);
        }

        for(Marker marker : trackedMarkerList){
            mapView.removeMarker(marker);
        }
        waypoints.clear();
        positionList.clear();
        index = 0;
    }

    public void clearMessageLog() {
        MessageLog.clear();
    }

    public void closeWindows(){
        Platform.exit();
    }

    public void minimizeWindows(ActionEvent actionEvent){
        //(((Node)actionEvent.getSource()).getScene().getWindow()).getScene().
    }

    public void sendWaypoint(ActionEvent actionEvent) {
        String waypointMessage="w#";
        for(Waypoint waypoint : waypoints){
            waypointMessage+=waypoint.getLatitude()+"#"+waypoint.getLongitude()+"#";
        }
        waypointMessage +="*";
        serial.sendToSerial(waypointMessage);
    }
}

final class FileHandleMode{
    public static int FILE_OPEN = 0;
    public static int FILE_SAVE = 1;
}