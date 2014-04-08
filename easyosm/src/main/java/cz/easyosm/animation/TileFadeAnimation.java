package cz.easyosm.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Map;

import cz.easyosm.tile.MapTile;
import cz.easyosm.tile.TileMath;

/**
 * Created by martinjr on 4/3/14.
 */
public class TileFadeAnimation extends MapAnimation {
    private Map<MapTile, TileFadeAnimation> fades;
    private MapTile tile;
    private Drawable original, replace;

    private int alpha=0;

    public TileFadeAnimation(Map<MapTile, TileFadeAnimation> fades, MapTile tile, Drawable original, Drawable replace) {
        this.fades=fades;
        this.tile=tile;
        this.original=original;
        this.replace=replace;

        fades.put(tile, this);
    }

    @Override
    public void init() {
        setDuration(250);
    }

    @Override
    public void applyTransformation(float interpolated, long milisElapsed) {
        alpha=(int) (255*((float)elapsed/duration));
    }

    @Override
    public void end() {
        //Log.d("easyosm", "End tile fade for "+tile);
        fades.remove(tile);
        original.setAlpha(255);
        replace.setAlpha(255);

        choreographer.parent.onTileAnimationDone(tile, original, replace);
    }

    public void drawTile(Canvas c, Rect target) {
        original.setBounds(target);
        replace.setBounds(target);
        replace.setAlpha(alpha);

        original.draw(c);
        replace.draw(c);
    }
}