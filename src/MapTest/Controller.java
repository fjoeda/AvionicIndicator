package MapTest;

import SerialComm.SerialCommunication;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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

        });




    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        mapView.addMapInializedListener(this);
        serial = new SerialCommunication();
        serial.setPortName("COM4");
        serial.setBaudRate(9600);

    }

    public void clearWaypoint(ActionEvent actionEvent) {
    }

    public void connectSerial(ActionEvent actionEvent) {
        try {
            serial.connectToSerial();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
