package cz.easyosm.tile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 3/25/14.
 */
public class OfflineTileProvider extends TileProviderBase {
    private File file;

    private MBTilesArchive archive;
    private MapView parent;

    private TileCache cache;

    public OfflineTileProvider(MapView parent, File f) {
        cache=new TileCache();

        this.file=f;
        this.archive=MBTilesArchive.getDatabaseFileArchive(f);
        this.parent=parent;
    }

    public void setFile(File f) {
        file=f;
        archive=MBTilesArchive.getDatabaseFileArchive(f);
    }

    @Override
    public Drawable getTile(MapTile tile) {
        //Log.d("easyosm", "Get tile "+tile);
        Drawable bd;
        if (cache.contains(tile)) {
            bd=cache.get(tile);
        }
        else {
            bd=makeUpTile(tile);
            fetchTileAsync(tile, bd);
        }

        if (bd!=null) return bd;
        else return blankTile(tile);
    }

    public void onTileAnimationDone(MapTile tile, Drawable original, Drawable replace) {
        cache.put(tile, replace);
    }

    @Override
    public int getMinZoomLevel() {
        return archive.getMinDataLevel();
    }

    @Override
    public int getMaxZoomLevel() {
        return archive.getMaxDataLevel();
    }

    private Drawable makeUpTile(MapTile tile) {
        MapTile aux;
        MapTile[] aux2;

        if (cache.contains(aux=tileToUpscale(tile, tile.zoom-1))) return scaleUpFromTile(tile, aux, cache.get(aux));

        aux2=tilesToDownscale(tile, tile.zoom+1);
        return scaleDownFromTiles(tile, aux2, cache.get(aux2));

        //return blankTile(tile);
    }

    private void fetchTileAsync(MapTile tile, Drawable tmp) {
        (new Thread(new DBTileLoader(tile, tmp))).start();
    }

    private class DBTileLoader implements Runnable {
        private MapTile tile;
        private Drawable tmp;

        public DBTileLoader(MapTile toLoad, Drawable tmp) {
            tile=toLoad;
            this.tmp=tmp;
        }

        @Override
        public void run() {
            Drawable ret=fetchTile(tile);

//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            if (ret!=null) {
                parent.onTileLoaded(tile, tmp, ret);
            }
        }
    }

    private Drawable fetchTile(MapTile tile) {
        if (getMinZoomLevel()<=tile.zoom && tile.zoom<=getMaxZoomLevel()) { // true data available
            return getTileFromDb(tile);
        }
        else {
            if (getMinZoomLevel()>tile.zoom) { // scale down
                MapTile[] load=tilesToDownscale(tile, getMinZoomLevel());

                Drawable[] toScale=new Drawable[load.length];

                for (int i=0; i<toScale.length; i++) {
                    if (cache.contains(load[i])) toScale[i]=cache.get(load[i]);
                    else toScale[i]=getTileFromDb(load[i]);
                }

                return scaleDownFromTiles(tile, load, toScale);
            }
            else { //scale up
                MapTile load=tileToUpscale(tile, getMaxZoomLevel());

                Drawable toScale;
                if (cache.contains(load)) {
                    toScale=cache.get(load);
                }
                else {
                    toScale=getTileFromDb(load);
                    if (toScale==null) return null;
                    else cache.put(load, toScale);
                }

                return scaleUpFromTile(tile, load, toScale);
            }
        }
    }


    private Drawable getTileFromDb(MapTile tile) {
        InputStream stream=archive.getInputStream(tile);
        if (stream!=null) return new BitmapDrawable(BitmapFactory.decodeStream(stream));
        else return null;
    }

//    private boolean justScaleUp()

    private MapTile tileToUpscale(MapTile target, int baseZoom) {
        int dZoom=target.zoom-baseZoom,
            newX=target.x>>dZoom,
            newY=target.y>>dZoom;

        return new MapTile(newX, newY, baseZoom);
    }

    private Drawable scaleUpFromTile(MapTile target, MapTile base, Drawable baseDrawable) {
        //Log.d("easyosm", "Upscaling tile "+target+" from "+base);
        int dZoom=target.zoom-base.zoom;
        int tileSize=256;
        int offX=target.x-(base.x<<dZoom),
                offY=target.y-(base.y<<dZoom);

        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        synchronized (baseDrawable) {
            baseDrawable.setBounds(-offX*tileSize, -offY*tileSize, -offX*tileSize+(256 << dZoom), -offY*tileSize+(256 << dZoom));
            baseDrawable.draw(canvas);
        }

//        Paint p=new Paint();
//        p.setColor(Color.BLACK);
//        p.setTextSize(20);
//        canvas.drawText(target.x+":"+target.y, 30, 128, p);
//        canvas.drawText(""+target.zoom, 100, 170, p);

        return new BitmapDrawable(bitmap);
    }

    private MapTile[] tilesToDownscale(MapTile target, int baseZoom) {
        int dZoom=baseZoom-target.zoom,
                newX=target.x<<dZoom,
                newY=target.y<<dZoom;
        int inewX=newX;
        int M=1<<dZoom, N=M*M;

        MapTile[] ret=new MapTile[N];

        for (int i=0; i<N; i++) {
            ret[i]=new MapTile(inewX, newY, baseZoom);
            inewX++;
            if (inewX-newX>=M) {
                inewX=newX;
                newY++;
            }
        }

        return ret;
    }

    private Drawable scaleDownFromTiles(MapTile target, MapTile[] bases, Drawable[] baseDrawables) {
        int dZoom=bases[0].zoom-target.zoom;
        int ix=0, iy=0;
        int M=1<<dZoom, N=M*M;

//        Log.d("easyosm", "Downscaling: target zoom="+target.zoom+", base zoom="+bases[0].zoom+"; dZoom="+dZoom+", M="+M+", N="+N);

        int tileSize=256/(1<<dZoom);

        Bitmap b=Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas c=new Canvas(b);

        c.drawColor(0xffbbbbbb);

        for (int i=0; i<N; i++) {
            if (baseDrawables[i]==null) continue;
//            Log.d("easyosm", "Draw tile "+i+" to "+ix*tileSize+" "+iy*tileSize+" "+(ix+1)*tileSize+" "+(iy+1)*tileSize);

            synchronized (baseDrawables[i]) {
                baseDrawables[i].setBounds(ix*tileSize, iy*tileSize, (ix+1)*tileSize, (iy+1)*tileSize);
                baseDrawables[i].setAlpha(255);
                baseDrawables[i].draw(c);
            }

            ix++;
            if (ix>=M) {
                ix=0;
                iy++;
            }
        }

        return new BitmapDrawable(b);
    }

    private Drawable blankTile(MapTile tile) {
        Rect mTileRect=new Rect(0, 0, 255, 255);
        Paint p=new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(30);

        Bitmap b=Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas c=new Canvas(b);

        c.drawColor(0xffbbbbbb);

//        c.drawText(tile.x+":"+tile.y, 30, 128, p);
//        c.drawText(""+tile.zoom, 100, 170, p);

        return new BitmapDrawable(b);
    }
}
