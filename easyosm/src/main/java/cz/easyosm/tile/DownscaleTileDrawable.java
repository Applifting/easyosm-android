package cz.easyosm.tile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by martinjr on 4/7/14.
 */
public class DownscaleTileDrawable extends Drawable {
    private MapTile target;
    private MapTile[] bases;
    private Drawable[] baseDrawables;

    private int alpha;

    public DownscaleTileDrawable(MapTile target, MapTile[] bases, Drawable[] baseDrawables) {
        this.target=target;
        this.bases=bases;
        this.baseDrawables=baseDrawables;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect clip=getBounds();

        canvas.save();
        canvas.clipRect(getBounds());

        int dZoom=bases[0].zoom-target.zoom;
        int x=clip.left, y=clip.top;
        int ix=0, iy=0;
        int M=1<<dZoom, N=M*M;

        double tileSize=clip.width()/M;

        canvas.drawColor(0xffbbbbbb);

        for (int i=0; i<N; i++) {
            if (baseDrawables[i]==null) continue;

            synchronized (baseDrawables[i]) {
                baseDrawables[i].setBounds((int)(x+ix*tileSize), (int)(y+iy*tileSize), (int)(x+(ix+1)*tileSize), (int)(y+(iy+1)*tileSize));
                baseDrawables[i].setAlpha(alpha);
                baseDrawables[i].draw(canvas);
            }

            ix++;
            if (ix>=M) {
                ix=0;
                iy++;
            }
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
