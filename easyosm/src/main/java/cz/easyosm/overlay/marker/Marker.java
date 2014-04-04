package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.Placeable;

/**
 * Created by martinjr on 4/4/14.
 */
public class Marker implements Placeable {
    private static Paint paint=new Paint();

    static {
        paint.setColor(Color.RED);
        paint.setAlpha(190);
    }

    protected GeoPoint point;

    public Marker(GeoPoint point) {
        this.point=point;
    }

    @Override
    public GeoPoint getPoint() {
        return point;
    }

    public void onDraw(Canvas c, Point p) {
        c.drawCircle(p.x, p.y, 16, paint);
    }
}
