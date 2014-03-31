package cz.easyosm.tile;

import android.graphics.drawable.Drawable;

import java.util.LinkedHashMap;

/**
 * Created by martinjr on 3/31/14.
 */
public class TileCache {
    private LinkedHashMap<MapTile, Drawable> cache;
    private int maxSize;

    public TileCache() {
        cache=new LinkedHashMap<MapTile, Drawable>();
        maxSize=100;
    }

    public boolean contains(MapTile tile) {
        return cache.containsKey(tile);
    }

    public void put(MapTile tile, Drawable drawable) {
        if (cache.containsKey(tile)) return;

        if (cache.size()>=maxSize) {
            cache.remove(cache.keySet().iterator().next());
        }

        cache.put(tile, drawable);
    }

    public Drawable get(MapTile tile) {
        return cache.get(tile);
    }

    public void clear() {
        cache.clear();
    }
}
