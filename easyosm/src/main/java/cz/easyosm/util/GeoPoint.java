package cz.easyosm.util;

/**
 * Created by martinjr on 3/24/14.
 */
public class GeoPoint {
    public double lat, lon;

    public GeoPoint(double lat, double lon) {
        this.lat=lat;
        this.lon=lon;
    }

    public void set(double lat, double lon) {
        this.lat=lat;
        this.lon=lon;
    }
}
