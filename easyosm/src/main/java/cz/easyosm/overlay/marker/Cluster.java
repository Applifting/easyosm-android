package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;

import cz.easyosm.util.GeoPoint;

/**
 * Created by martinjr on 4/4/14.
 */
public class Cluster extends Marker {
    private static Paint paint=new Paint();

    static {
        paint.setColor(Color.BLUE);
        paint.setAlpha(190);
    }

    private List<Marker> markers;

    public Cluster(GeoPoint point) {
        super(point);
    }

    @Override
    public void onDraw(Canvas canvas, Point p) {
        canvas.drawCircle(p.x, p.y, 20, paint);
        canvas.drawText(String.valueOf(markers.size()), p.x, p.y+5, null);
    }
}
