package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import cz.easyosm.util.GeoPoint;

/**
 * Created by martinjr on 4/4/14.
 */
public class Cluster extends MarkerBase {
    private static Paint paint=new Paint();
    private static Paint textPaint=new Paint();

    static {
        paint.setColor(Color.BLUE);
        paint.setAlpha(190);
        textPaint.setColor(Color.WHITE);
    }

    private List<Marker> markers;
    private GeoPoint point;

    public Cluster(Marker a, Marker b) {
        point=new GeoPoint((a.getPoint().lat+b.getPoint().lat)/2, (a.getPoint().lon+b.getPoint().lon)/2);
        markers=new ArrayList<Marker>();
        markers.add(a);
        markers.add(b);

        a.clustered=true;
        b.clustered=true;
    }

    public void add(Marker m) {
        point.set((point.lat*markers.size()+m.getPoint().lat)/(markers.size()+1),
                (point.lon*markers.size()+m.getPoint().lon)/(markers.size()+1));
        m.clustered=true;
        markers.add(m);
    }

    public void remove(Marker m) {
        point.set((point.lat*markers.size()-m.getPoint().lat)/(markers.size()-1),
                (point.lon*markers.size()-m.getPoint().lon)/(markers.size()-1));
        m.clustered=false;
        markers.remove(m);
    }

    @Override
    public GeoPoint getPoint() {
        return point;
    }

    @Override
    public void onDraw(Canvas canvas, Point p) {
        canvas.drawCircle(p.x, p.y, 20, paint);
        canvas.drawText(String.valueOf(markers.size()), p.x, p.y+5, textPaint);
    }
}
