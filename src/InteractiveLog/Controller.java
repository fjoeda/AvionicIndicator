package InteractiveLog;

import au.com.bytecode.opencsv.CSVReader;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public MapView mapView;
    public javafx.scene.control.Label awalStatus;
    public javafx.scene.control.Label akhirStatus;
    @FXML
    private Button btnLoad;
    private Window primaryStage;



    String FILE_PATH = System.getProperty("user.dir") + "/waypoints.csv";
    int records = 0, index = 0;
    String dayaAwal, dayaAkhir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String BING_MAP_API_KEY = "AjBEkbJVIF_enJ7KdZTSXxNgn58ADVVRqFNKbSBeSCmNukw4hQYAAcaIM61q2mGp";

        mapView.setBingMapsApiKey(BING_MAP_API_KEY);
        mapView.setMapType(MapType.BINGMAPS_ROAD);
        mapView.setZoom(12);
        mapView.setCenter(new Coordinate(-7.7713847,110.3774998));
        mapView.initialize();

    }

    public void LoadFile(ActionEvent actionEvent) {
        File recordsDir = new File(System.getProperty("user.dir"));

        if (! recordsDir.exists()) {
            recordsDir.mkdirs();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Waypoint");
        fileChooser.setInitialDirectory(recordsDir);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        fileChooser.showOpenDialog(primaryStage);

        //Belum bisa dapetin nama file waktu buka file explorer
        String FILE_PATH = System.getProperty("user.dir") + "/waypoints.csv";
        String dayaAwal;
        String dayaAkhir;

        //Ngitung jumlah records di CSV
        try (
                Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH));
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
                Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                /*System.out.println("Tanggal   : " + nextRecord[0]);
                System.out.println("Baterai   : " + nextRecord[1]);
                System.out.println("Altitude  : " + nextRecord[2]);
                System.out.println("Roll      : " + nextRecord[3]);
                System.out.println("Pitch     : " + nextRecord[4]);
                System.out.println("Yaw       : " + nextRecord[5]);
                System.out.println("Latitude  : " + nextRecord[6]);
                System.out.println("Longitude : " + nextRecord[7]);
                System.out.println("Speed     : " + nextRecord[8]);
                System.out.println("Status    : " + nextRecord[9]);
                System.out.println("==========================");*/

                if (index == 1) {
                    dayaAwal = nextRecord[1];
                    awalStatus.setText(dayaAwal);
                } else if (index == records - 1) {
                    dayaAkhir = nextRecord[1];
                    akhirStatus.setText(dayaAkhir);
                }

                index += 1;
            }
        } catch (IOException e) {
            System.out.println(e);
        }


    }
}
