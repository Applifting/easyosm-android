package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.Placeable;

/**
 * Created by martinjr on 4/4/14.
 */
public class Marker extends MarkerBase {
    public static final int STATE_NORMAL=0,
        STATE_ACTIVE=1;

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
    public void onDraw(Canvas canvas, Point p) {
        canvas.save();
        canvas.translate(p.x, p.y);
        if (currentTransition!=null && currentTransition.active) drawTransition(canvas, currentTransition);
        else drawState(canvas, state);
        canvas.restore();
    }

    protected void drawTransition(Canvas canvas, MarkerTransition transition) {
        if (transition.stateFrom==STATE_NORMAL && transition.stateTo==STATE_ACTIVE) {
            canvas.drawCircle(0, 0, 16+10*transition.transition, paint);
            canvas.drawText(""+id, 0, 0, textPaint);
        }
        else if (transition.stateFrom==STATE_ACTIVE && transition.stateTo==STATE_NORMAL) {
            canvas.drawCircle(0, 0, 26-10*transition.transition, paint);
            canvas.drawText(""+id, 0, 0, textPaint);
        }
    }

    protected void drawState(Canvas canvas, int state) {
        if (state==STATE_NORMAL) {
            canvas.drawCircle(0, 0, 16, paint);
            canvas.drawText(""+id, 0, 0, textPaint);
        }
        else if (state==STATE_ACTIVE) {
            canvas.drawCircle(0, 0, 26, paint);
            canvas.drawText(""+id, 0, 0, textPaint);
        }
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    public void setClusterable(boolean clusterable) {
        this.clusterable=clusterable;
    }

    public boolean isClusterable() {
        return clusterable;
    }

}
