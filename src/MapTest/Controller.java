package MapTest;

import SerialComm.SerialCommunication;
import TextParser.StringParser;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.swing.text.Position;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable, MapComponentInitializedListener{

    public Button goButton;
    public TextField lngText;
    public TextField latText;
    public GoogleMapView mapView;
    public Button clearButton;
    public Button clearconnectButton;
    private GoogleMap map;
    private LatLong position;
    private ArrayList<LatLong> positionList = new ArrayList<>();
    private SerialCommunication serial;
    private AnimationTimer timer;

    long lastTimerCall;
    LatLong positionLast;
    LatLong positionNow;

    MVCArray mvc;
    PolylineOptions polylineOptions;
    ArrayList<LatLong> routes = new ArrayList<>();

    public void buttonClick(ActionEvent actionEvent) {
    }

    @Override
    public void mapInitialized() {
        LatLong MyPosition = new LatLong(-7.889,108.7137);
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(MyPosition).mapType(MapTypeIdEnum.ROADMAP)
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




    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        mapView.addMapInializedListener(this);
        serial = new SerialCommunication();
        serial.setPortName("COM14");
        serial.setBaudRate(9600);
        lastTimerCall = System.nanoTime();



        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now > lastTimerCall + 1_000_000_000L && serial.getReceivedMessage()!=null){

                    try{
                        positionNow = new LatLong(StringParser.getLatitude(serial.getReceivedMessage()),
                                StringParser.getLongitude(serial.getReceivedMessage()));
                        System.out.println(positionNow);
                        if((positionNow.getLatitude()!= positionLast.getLatitude())||(positionNow.getLongitude()!=positionLast.getLongitude())){
                            routes.add(positionNow);
                            positionLast = positionNow;
                            map.setCenter(positionNow);
                            mvc.push(positionNow);
                            System.out.println(serial.getReceivedMessage());


                        }


                    }catch (Exception e){

                    }
                    lastTimerCall=now;


                }
            }
        };

    }

    public void clearWaypoint(ActionEvent actionEvent) {
        map.clearMarkers();
    }

    public void connectSerial(ActionEvent actionEvent) {
        try {
            serial.connectToSerial();
            timer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
