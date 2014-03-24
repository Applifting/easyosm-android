package cz.easyosm.util;

/**
 * Created by martinjr on 3/24/14.
 */
public class GeoPoint {
    private double lat, lon;

    public GeoPoint(double lat, double lon) {
        this.lat=lat;
        this.lon=lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat=lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon=lon;
    }
}
