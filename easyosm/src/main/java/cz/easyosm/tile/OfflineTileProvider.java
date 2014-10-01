package cz.easyosm.tile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 3/25/14.
 */
public class OfflineTileProvider extends TileProviderBase {
    private File file;

    private MBTilesArchive archive;
    private MapView parent;

    private TileCache cache;

    private MapTile aux;
    private MapTile[] aux2;

    private ThreadPoolExecutor executor;
    private Queue<Runnable> toRun;

    private BitmapDrawable blank;

    public OfflineTileProvider(MapView parent, File f) {
        cache=new TileCache();
        executor=new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        toRun=new LinkedList<Runnable>();

        this.file=f;
        this.archive=MBTilesArchive.getDatabaseFileArchive(f);
        this.parent=parent;
    }

    public void setFile(File f) {
        Log.d("iPass", "Loaded file "+f.getAbsolutePath());
        file=f;
        archive=MBTilesArchive.getDatabaseFileArchive(f);
    }

    @Override
    public Drawable getTile(MapTile tile) {
//        Log.d("easyosm", "Get tile "+tile);
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
        cache.put(tile, replace, 10);
    }

    @Override
    public int getMinZoomLevel() {
        return archive.getMinDataLevel();
    }

    @Override
    public int getMaxZoomLevel() {
        return archive.getMaxDataLevel();
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    private Drawable makeUpTile(MapTile tile) {
        Drawable d;
        if (cache.contains(aux=tileToUpscale(tile, tile.zoom-1))) {
            d=new UpscaleTileDrawable(tile, aux, cache.get(aux));
            cache.put(tile, d, 0);
            return d;
        }
        else {
            aux2=tilesToDownscale(tile, tile.zoom+1);
            d=new DownscaleTileDrawable(tile, aux2, cache.get(aux2));
            cache.put(tile, d, 0);
            return d;
        }
    }

    private void fetchTileAsync(MapTile tile, Drawable tmp) {
        toRun.add(new DBTileLoader(tile, tmp));
    }

    public void runAsyncTasks() {
        while (!toRun.isEmpty()) {
            executor.execute(toRun.poll());
        }
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
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);

            Drawable ret=fetchTile(tile);

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
                    toScale[i]=retrieveTile(load[i]);
                }

                return scaleDownFromTiles(tile, load, toScale);
            }
            else { //scale up
                MapTile load=tileToUpscale(tile, getMaxZoomLevel());

                return scaleUpFromTile(tile, load, retrieveTile(load));
            }
        }
    }

    private Drawable retrieveTile(MapTile tile) {
        Drawable ret;
        if (cache.containsTrue(tile)) {
            ret=cache.get(tile);
//            Log.d("iPass", "cache hit");
            return ret;
        }
        else {
            ret=getTileFromDb(tile);
//            Log.d("iPass", "got "+ret+" from db");
            if (ret!=null)  cache.put(tile, ret, 10);
            return ret;
        }
    }


    private Drawable getTileFromDb(MapTile tile) {
        InputStream stream=archive.getInputStream(tile);
        if (stream!=null) return new BitmapDrawable(BitmapFactory.decodeStream(stream));
        else {
            Log.d("iPass", "ERROR: tile "+tile+" not in DB!");
            return blankTile(tile);
        }
    }

    private MapTile tileToUpscale(MapTile target, int baseZoom) {
        int dZoom=target.zoom-baseZoom,
            newX=target.x>>dZoom,
            newY=target.y>>dZoom;

        return new MapTile(newX, newY, baseZoom);
    }

    private Drawable scaleUpFromTile(MapTile target, MapTile base, Drawable baseDrawable) {
//        Log.d("iPass", "Scale "+base+" to "+target+" from "+baseDrawable);
        int dZoom=target.zoom-base.zoom;
        int tileSize=256;
        int offX=target.x-(base.x<<dZoom),
                offY=target.y-(base.y<<dZoom);

        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        synchronized (baseDrawable) {
            baseDrawable.setBounds(-offX*tileSize, -offY*tileSize, -offX*tileSize+(256 << dZoom), -offY*tileSize+(256 << dZoom));
            baseDrawable.setAlpha(255);
            baseDrawable.draw(canvas);
        }

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

        int tileSize=256>>dZoom;

        Bitmap bitmap=Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(bitmap);

        canvas.drawColor(0xffbbbbbb);

        for (int i=0; i<N; i++) {
            if (baseDrawables[i]==null) continue;

            synchronized (baseDrawables[i]) {
                baseDrawables[i].setBounds(ix*tileSize, iy*tileSize, (ix+1)*tileSize, (iy+1)*tileSize);
                baseDrawables[i].setAlpha(255);
                baseDrawables[i].draw(canvas);
            }

            ix++;
            if (ix>=M) {
                ix=0;
                iy++;
            }
        }

        return new BitmapDrawable(bitmap);
    }

    private Drawable blankTile(MapTile tile) {
        if (blank!=null) return blank;

        Rect mTileRect=new Rect(0, 0, 255, 255);
        Paint p=new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(30);

        Bitmap b=Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        Canvas c=new Canvas(b);

        c.drawColor(0xffbbbbbb);

//        c.drawText(tile.x+":"+tile.y, 30, 128, p);
//        c.drawText(""+tile.zoom, 100, 170, p);

        return blank=new BitmapDrawable(b);
    }
}
