package cz.easyosm.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import cz.easyosm.tile.TileMath;
import cz.easyosm.util.GeoPoint;
import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 3/24/14.
 */
public abstract class MapOverlayBase {
    public abstract void onDraw(Canvas c);

    public abstract boolean onSingleTap(Point touch);
    public abstract boolean onDoubleTap(Point touch);
    public abstract void onLongPress(Point touch);

    protected MapView parent;

    public MapOverlayBase(MapView parent) {
        this.parent=parent;
    }

    /**
     * Translate x from map pixels to view canvas pixels
     * @param x
     * @return
     */
    protected int getX(int x) {
        return x-parent.getOffsetX();
    }

    /**
     * Translate y from map pixels to view canvas pixels
     * @param y
     * @return
     */
    protected int getY(int y) {
        return parent.getOffsetY();
    }

    /**
     * Translate point from map pixels to view canvas pixels
     * @param p
     * @return
     */
    protected Point getPoint(Point p) {
        p.x-=parent.getOffsetX();
        p.y-=parent.getOffsetY();
        return p;
    }

    /**
     * Translate GeoPoint from map latlon to view canvas pixels
     * @param p
     * @return
     */
    protected Point getPoint(GeoPoint p) {
        return getPoint(TileMath.LatLongToPixelXY(p.lat, p.lon, parent.getZoomLevel(), null));
    }

    /**
     * Translate rect from map pixels to view canvas pixels
     * @param r
     * @return
     */
    protected Rect getRect(Rect r) {
        r.left-=parent.getOffsetX();
        r.top-=parent.getOffsetY();
        r.right-=parent.getOffsetX();
        r.bottom-=parent.getOffsetY();

        return r;
    }

}
