package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Point;

import cz.easyosm.util.Placeable;

/**
 * Created by martinjr on 4/6/14.
 */
public abstract class MarkerBase implements Placeable {
    public abstract void onDraw(Canvas canvas, Point p);
}
