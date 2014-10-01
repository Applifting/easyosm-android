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
    public static final int STATE_NORMAL=0,
        STATE_ACTIVE=1;

    private static Paint paint=new Paint();
    private static Paint textPaint=new Paint();

    static {
        paint.setColor(Color.BLUE);
        paint.setAlpha(190);
        textPaint.setColor(Color.WHITE);
    }

    protected List<Marker> markers;
    protected GeoPoint point;

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
        canvas.save();
        canvas.translate(p.x, p.y);
        if (currentTransition!=null && currentTransition.active) drawTransition(canvas, currentTransition);
        else drawState(canvas, state);
        canvas.restore();
    }

    protected void drawTransition(Canvas canvas, MarkerTransition transition) {
        if (transition.stateFrom==STATE_NORMAL && transition.stateTo==STATE_ACTIVE) {
            canvas.drawCircle(0, 0, 20+10*transition.transition, paint);
            canvas.drawText(""+markers.size(), 0, 0, textPaint);
        }
        else if (transition.stateFrom==STATE_ACTIVE && transition.stateTo==STATE_NORMAL) {
            canvas.drawCircle(0, 0, 30-10*transition.transition, paint);
            canvas.drawText(""+markers.size(), 0, 0, textPaint);
        }
    }

    protected void drawState(Canvas canvas, int state) {
        if (state==STATE_NORMAL) {
            canvas.drawCircle(0, 0, 20, paint);
            canvas.drawText(""+markers.size(), 0, 0, textPaint);
        }
        else if (state==STATE_ACTIVE) {
            canvas.drawCircle(0, 0, 30, paint);
            canvas.drawText(""+markers.size(), 0, 0, textPaint);
        }
    }

    @Override
    public boolean isCluster() {
        return true;
    }
}
