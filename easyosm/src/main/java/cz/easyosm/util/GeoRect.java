package cz.easyosm.util;

/**
 * Created by martinjr on 4/4/14.
 */
public class GeoRect {
    public double left, top, right, bottom;

    public GeoRect(double left, double top, double right, double bottom) {
        this.left=left;
        this.top=top;
        this.right=right;
        this.bottom=bottom;
    }

    public GeoRect(GeoPoint tl, GeoPoint br) {
        left=tl.lon;
        top=tl.lat;
        right=br.lon;
        bottom=br.lat;
    }

    public boolean contains(GeoPoint p) {
        return bottom<=p.lat && p.lat<=top && left<=p.lon && p.lon<=right;
    }
}