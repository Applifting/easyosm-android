package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Point;

import cz.easyosm.util.Placeable;

/**
 * Created by martinjr on 4/6/14.
 */
public abstract class MarkerBase implements Placeable {
    protected int state=0;
    protected MarkerTransition currentTransition;

    public abstract void onDraw(Canvas canvas, Point p);
    public abstract boolean isCluster();

    public void setCurrentTransition(MarkerTransition currentTransition) {
        this.currentTransition=currentTransition;
    }

    public MarkerTransition getCurrentTransition() {
        return currentTransition;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state=state;
    }

}
