package cz.easyosm.util;

import android.graphics.Point;
import android.graphics.Rect;

import cz.easyosm.tile.TileMath;

/**
 * Created by martinjr on 4/4/14.
 */
public class GeoRect {
    public double left, top, right, bottom;

    public GeoRect(double top, double left, double bottom, double right) {
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

    public Rect toMap(float zoomLevel, Rect reuse) {
        Point p;
        Rect ret=(reuse==null)?new Rect():reuse;

        p=TileMath.LatLongToPixelXY(top, left, zoomLevel, null);
        ret.left=p.x;
        ret.top=p.y;

        p=TileMath.LatLongToPixelXY(bottom, right, zoomLevel, p);
        ret.right=p.x;
        ret.bottom=p.y;

        return ret;
    }

    @Override
    public String toString() {
        return "GeoRect{"+
                "left="+left+
                ", top="+top+
                ", right="+right+
                ", bottom="+bottom+
                '}';
    }
}
