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
public class Marker extends MarkerBase {
    private static Paint paint=new Paint();
    private static Paint textPaint=new Paint();

    static {
        paint.setColor(Color.RED);
        paint.setAlpha(190);
        textPaint.setColor(Color.WHITE);
    }

    protected GeoPoint point;

    boolean clustered=false;
    int id;

    private boolean clusterable=true;

    public Marker(GeoPoint point) {
        this.point=point;
    }

    public Marker(GeoPoint point, int id) {
        this.point=point;
        this.id=id;
    }

    @Override
    public GeoPoint getPoint() {
        return point;
    }

    @Override
    public void onDraw(Canvas c, Point p) {
        c.drawCircle(p.x, p.y, 16, paint);
        c.drawText(""+id, p.x, p.y, textPaint);
    }

    public void setClusterable(boolean clusterable) {
        this.clusterable=clusterable;
    }

    public boolean isClusterable() {
        return clusterable;
    }
}
