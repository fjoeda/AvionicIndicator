package InteractiveLog;

import TextParser.StringParser;
import au.com.bytecode.opencsv.CSVReader;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.CoordinateLine;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.chart.XYChart.Series;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import sample.Waypoint;

public class Controller implements Initializable {
    public MapView mapView;
    public javafx.scene.control.Label awalStatus;
    public javafx.scene.control.Label akhirStatus;
    public Label timeStatus;
    public Label distStatus;
    public Label status;
    public VBox AirspeedVbox;
    public VBox AltitudeVbox;


    private LineChart<Number,Number> AltitudeChart;
    private LineChart<Number,Number> AirspeedChart;
    @FXML
    private Button btnLoad;
    private Window primaryStage;

    private ArrayList<Coordinate> coordinateList = new ArrayList<>();
    private AnimationTimer timer;
    long lastTimerCall;



    String FILE_PATH = System.getProperty("user.dir") + "/waypoints.csv";
    int records = 0, index = 0;
    String dayaAwal, dayaAkhir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final NumberAxis xAxis1 = new NumberAxis();
        final NumberAxis yAxis1 = new NumberAxis();
        final NumberAxis xAxis2 = new NumberAxis();
        final NumberAxis yAxis2 = new NumberAxis();
        AltitudeChart = new LineChart<>(xAxis1,yAxis1);
        AirspeedChart = new LineChart<>(xAxis2,yAxis2);
        String BING_MAP_API_KEY = "AjBEkbJVIF_enJ7KdZTSXxNgn58ADVVRqFNKbSBeSCmNukw4hQYAAcaIM61q2mGp";

        mapView.setBingMapsApiKey(BING_MAP_API_KEY);
        mapView.setMapType(MapType.BINGMAPS_ROAD);
        mapView.setZoom(12);
        mapView.setCenter(new Coordinate(-7.7713847,110.3774998));

        // init MapView-Cache
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = System.getProperty("user.dir") + "/mapjfx-cache";
        try {
            Files.createDirectories(Paths.get(cacheDir));
            offlineCache.setCacheDirectory(cacheDir);
            offlineCache.setActive(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapView.initialize();
        AltitudeChart.setTitle("Altitude");
        AirspeedChart.setTitle("Airspeed");
        xAxis1.setTickLabelFill(Color.WHITE);
        yAxis1.setTickLabelFill(Color.WHITE);
        xAxis2.setTickLabelFill(Color.WHITE);
        yAxis2.setTickLabelFill(Color.WHITE);
        AltitudeChart.legendVisibleProperty().setValue(false);
        AirspeedChart.legendVisibleProperty().setValue(false);
        AirspeedVbox.getChildren().add(AirspeedChart);
        AltitudeVbox.getChildren().add(AltitudeChart);

        LoadFile();
        if(coordinateList.isEmpty())
            Platform.exit();
        setCenterAndZoom(coordinateList);
        CoordinateLine route = new CoordinateLine(coordinateList).setColor(Color.DARKBLUE)
                .setWidth(4)
                .setVisible(true);

        mapView.addCoordinateLine(route);
        lastTimerCall = System.nanoTime();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 10_000_000_000L){

                    mapView.addCoordinateLine(route);
                    lastTimerCall = now;
                }
            }
        };
        timer.start();


    }

    public void LoadFile() {
        File recordsDir = new File(System.getProperty("user.dir"));
        XYChart.Series speedChart = new XYChart.Series();
        XYChart.Series altitudeChart = new XYChart.Series();

        if (! recordsDir.exists()) {
            recordsDir.mkdirs();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Waypoint");
        fileChooser.setInitialDirectory(recordsDir);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gamaforce GCS Flight Record","*.ggfr")
        );

        File file = fileChooser.showOpenDialog(primaryStage);

        //Belum bisa dapetin nama file waktu buka file explorer
        String FILE_PATH = System.getProperty("user.dir") + "/waypoints.csv";
        String dayaAwal;
        String dayaAkhir;


        //Ngitung jumlah records di CSV
        try (
                Reader reader = Files.newBufferedReader(Paths.get(file.toString()));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                records+=1;
            }

            System.out.println(records);
        } catch (IOException e) {
            System.out.println(e);
        }

        try (
                Reader reader = Files.newBufferedReader(Paths.get(file.toString()));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                coordinateList.add(new Coordinate(Double.parseDouble(nextRecord[2]),
                        Double.parseDouble(nextRecord[3])));
                Number time = Integer.parseInt(nextRecord[7]);
                Number speed = Double.parseDouble(nextRecord[5]);
                Number altitude = Double.parseDouble(nextRecord[6]);
                speedChart.getData().add(new XYChart.Data(time,speed));
                altitudeChart.getData().add(new XYChart.Data(time,altitude));

                try {
                    if (index == 1) {
                        dayaAwal = nextRecord[4];
                        awalStatus.setText(dayaAwal);
                    } else if (index == records - 1) {
                        dayaAkhir = nextRecord[4];
                        akhirStatus.setText(dayaAkhir);
                        timeStatus.setText(StringParser.getTimeFormatFromSecond(Integer.parseInt(nextRecord[7])));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }


                distStatus.setText(getTotalDistance(coordinateList));
                index += 1;
            }
            AltitudeChart.getData().add(altitudeChart);
            AirspeedChart.getData().add(speedChart);

        } catch (IOException e) {
            System.out.println(e);
        }


    }



    private String getTotalDistance(ArrayList<Coordinate> coordinateList){
        double totalDistance = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        for(int i = 0;i<coordinateList.size()-1;i++){
            totalDistance += Waypoint.distance(coordinateList.get(i),coordinateList.get(i+1));
        }
        if(totalDistance<3000){
            return String.valueOf(df.format(totalDistance))+" m";
        }else {
            totalDistance /=1000;
            return String.valueOf(df.format(totalDistance))+" Km";
        }
    }

    //buat set zoom secara otomatis
    //rumus buat meter
    // resolution = (cos(latitude * pi/180) * 2 * pi * 6378137) / (768 * 2^zoomlevel)

    private void setCenterAndZoom(ArrayList<Coordinate> coordinateList){
        double minLat = 999, minLng = 999;
        double maxLat = -999, maxLng = -999;
        for(Coordinate pos : coordinateList){
            if(minLat>=pos.getLatitude()){
                minLat = pos.getLatitude();
            }
            if(minLng>=pos.getLongitude()){
                minLng = pos.getLongitude();
            }
            if(maxLat<=pos.getLatitude()){
                maxLat = pos.getLatitude();
            }
            if(maxLng<=pos.getLongitude()){
                maxLng = pos.getLongitude();
            }
        }
        Coordinate center = new Coordinate((minLat+(maxLat-minLat)/2),(minLng+(maxLng-minLng)/2));
        mapView.setCenter(center);
        double midLat = (minLat+(maxLat-minLat)/2);
        System.out.println(Waypoint.distance(new Coordinate(minLat,minLng),new Coordinate(maxLat,maxLng)));
        double zoom = getProperZoomOnMap(midLat,
                Waypoint.distance(new Coordinate(minLat,minLng),new Coordinate(maxLat,maxLng)),
                616);
        mapView.setZoom(zoom-1);
        System.out.println(zoom);
        System.out.println(mapView.getZoom());
    }

    private double getProperZoomOnMap(double latitude, double distanceResolution, double width){
        return logBase((Math.cos(latitude*Math.PI/180)*2*Math.PI*6372137*width)/(256*distanceResolution),2);
    }

    private double logBase(double val,double base){
        return Math.log(val)/Math.log(base);
    }



}
