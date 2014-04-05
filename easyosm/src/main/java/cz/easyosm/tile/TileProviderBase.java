package cz.easyosm.tile;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

/**
 * Created by martinjr on 3/24/14.
 */
public abstract class TileProviderBase {
    public abstract Drawable getTile(MapTile tile);
    public abstract void onTileAnimationDone(MapTile tile, Drawable original, Drawable replace);

    public abstract int getMinZoomLevel();
    public abstract int getMaxZoomLevel();

}
