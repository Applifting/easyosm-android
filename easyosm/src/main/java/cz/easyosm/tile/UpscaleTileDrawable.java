package cz.easyosm.tile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by martinjr on 4/7/14.
 */
public class UpscaleTileDrawable extends Drawable {
    private MapTile target;
    private MapTile base;
    private Drawable baseDrawable;

    private int alpha;

    public UpscaleTileDrawable(MapTile target, MapTile base, Drawable baseDrawable) {
        this.target=target;
        this.base=base;
        this.baseDrawable=baseDrawable;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect clip=getBounds();
        canvas.save();
        canvas.clipRect(clip);

        int dZoom=target.zoom-base.zoom;
        int tileSize=clip.width();
        int offX=target.x-(base.x<<dZoom),
                offY=target.y-(base.y<<dZoom);

        int x=clip.left,
            y=clip.top;

        synchronized (baseDrawable) {
            baseDrawable.setBounds(x-offX*tileSize, y-offY*tileSize, x-offX*tileSize+(tileSize << dZoom), y-offY*tileSize+(tileSize << dZoom));
            baseDrawable.setAlpha(alpha);
            baseDrawable.draw(canvas);
        }

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha=alpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
