package cz.easyosm.tile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by martinjr on 3/25/14.
 */
public class OfflineTileProvider extends TileProviderBase {
    private File file;

    private MBTilesArchive archive;

    private TileCache cache;

    public OfflineTileProvider(File f) {
        cache=new TileCache();

        file=f;
        archive=MBTilesArchive.getDatabaseFileArchive(f);
    }

    public void setFile(File f) {
        file=f;
        archive=MBTilesArchive.getDatabaseFileArchive(f);
    }

    @Override
    public Drawable getTile(MapTile tile) {
        //Log.d("iPass", "Get tile "+tile);
        Drawable bd;
        if (cache.contains(tile)) {
            bd=cache.get(tile);
        }
        else {
            bd=fetchTile(tile);
            cache.put(tile, bd);
        }

        if (bd!=null) return bd;

        Rect mTileRect=new Rect(0, 0, 255, 255);
        Paint p=new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(30);

        Bitmap b=Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas c=new Canvas(b);

        c.drawColor(0xffbbbbbb);

        c.drawLines(new float[] {
                    mTileRect.left, mTileRect.top,
                    mTileRect.right, mTileRect.top,
                    mTileRect.right, mTileRect.top,
                    mTileRect.right, mTileRect.bottom,
                    mTileRect.right, mTileRect.bottom,
                    mTileRect.left, mTileRect.bottom,
                    mTileRect.left, mTileRect.bottom,
                    mTileRect.left, mTileRect.top}, p);

        c.drawText(tile.x+":"+tile.y, 30, 128, p);
        c.drawText(""+tile.zoom, 100, 170, p);

        return new BitmapDrawable(b);
    }

    private Drawable fetchTile(MapTile tile) {
        if (minDataLevel<=tile.zoom && tile.zoom<=maxDataLevel) { // true data available
            return new BitmapDrawable(BitmapFactory.decodeStream(archive.getInputStream(tile)));
        }
        else {
            if (minDataLevel>tile.zoom) { // TODO: scale down
                Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.RED);
                return new BitmapDrawable(bitmap);
            }
            else { //scale up
                int dZoom=tile.zoom-maxDataLevel;
                int newX=tile.x>>dZoom,
                        newY=tile.y>>dZoom,
                        offX=tile.x-(newX<<dZoom),
                        offY=tile.y-(newY<<dZoom);

                MapTile load=new MapTile(newX, newY, maxDataLevel);

                Drawable toScale;
                if (cache.contains(load)) toScale=cache.get(load);
                else toScale=new BitmapDrawable(BitmapFactory.decodeStream(archive.getInputStream(tile)));

                int o=256/(1<<dZoom);
                toScale.setBounds(-offX*o, -offY*o, -offX*o+256, -offY*o+256);

                Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                canvas.scale(1<<dZoom, 1<<dZoom);

                toScale.draw(canvas);

                return new BitmapDrawable(bitmap);
            }
        }
    }
}
