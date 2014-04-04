package cz.easyosm.tile;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by martinjr on 3/31/14.
 */
public class TileCache {
    private static final int CAPACITY=100;
    private static final float LOAD_FACTOR=.75f;

    private LinkedHashMap<MapTile, Drawable> cache;

    public TileCache() {
        cache=new LinkedHashMap<MapTile, Drawable>((int) (CAPACITY/LOAD_FACTOR), LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Entry<MapTile, Drawable> eldest) {
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

    public void put(MapTile tile, Drawable drawable) {
        cache.put(tile, drawable);
    }

    public Drawable get(MapTile tile) {
        return cache.get(tile);
    }

    public Drawable[] get(MapTile[] tiles) {
        Drawable[] ret=new Drawable[tiles.length];

        for (int i=0; i<tiles.length; i++) {
            ret[i]=cache.get(tiles[i]);
        }

        return ret;
    }

    public void clear() {
        cache.clear();
    }
}
