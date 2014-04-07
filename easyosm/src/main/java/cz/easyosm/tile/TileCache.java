package cz.easyosm.tile;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by martinjr on 3/31/14.
 */
public class TileCache {
    private static final int CAPACITY=100;
    private static final float LOAD_FACTOR=.75f;

    private LinkedHashMap<MapTile, QualityAwareDrawable> cache;

    public TileCache() {
        cache=new LinkedHashMap<MapTile, QualityAwareDrawable>((int) (CAPACITY/LOAD_FACTOR), LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Entry<MapTile, QualityAwareDrawable> eldest) {
                return this.size()>=CAPACITY;
            }
        };
    }

    public boolean contains(MapTile tile) {
        return cache.containsKey(tile);
    }

    public boolean contains(MapTile[] tiles) {
        boolean ret=true;

        for (int i=0; i<tiles.length && ret; i++) {
            ret=ret && cache.containsKey(tiles[i]);
        }
        return ret;
    }

    public boolean containsTrue(MapTile tile) {
        return cache.containsKey(tile) && cache.get(tile).quality>0;
    }

    public void put(MapTile tile, Drawable drawable, int quality) {
        if (cache.containsKey(tile)) {
            QualityAwareDrawable cached=cache.get(tile);
            if (cached.quality<=quality) {
                cached.drawable=drawable;
                cached.quality=quality;
            }
        }
        else cache.put(tile, new QualityAwareDrawable(drawable, quality));
    }

    public Drawable get(MapTile tile) {
        if (!cache.containsKey(tile)) return null;
        else return cache.get(tile).drawable;
    }

    public Drawable[] get(MapTile[] tiles) {
        QualityAwareDrawable tmp;

        Drawable[] ret=new Drawable[tiles.length];

        for (int i=0; i<tiles.length; i++) {
            tmp=cache.get(tiles[i]);
            ret[i]=(tmp==null)?null:tmp.drawable;
        }

        return ret;
    }

    public void clear() {
        cache.clear();
    }

    private class QualityAwareDrawable {
        Drawable drawable;
        int quality;

        private QualityAwareDrawable(Drawable drawable, int quality) {
            this.drawable=drawable;
            this.quality=quality;
        }
    }
}
