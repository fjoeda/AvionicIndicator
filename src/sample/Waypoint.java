package sample;

import com.sothawo.mapjfx.Coordinate;

public class Waypoint {
    private int id;
    private String latitude;
    private String longitude;

    public Waypoint(int id, String latitude, String longitude) {
        super();
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public double getLatitudeD(){
        return Double.parseDouble(latitude);
    }

    public double getLongitudeD(){
        return Double.parseDouble(longitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public static double distance(Coordinate pos1, Coordinate pos2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(pos2.getLatitude() - pos1.getLatitude());
        double lonDistance = Math.toRadians(pos2.getLongitude() - pos1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(pos1.getLatitude())) * Math.cos(Math.toRadians(pos2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }
}