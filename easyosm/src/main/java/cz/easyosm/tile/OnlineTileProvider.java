package cz.easyosm.tile;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * Created by martinjr on 3/25/14.
 */
public class OnlineTileProvider extends TileProviderBase {

    @Override
    public Drawable getTile(MapTile tile) {
        return null;
    }

    @Override
    public void onTileAnimationDone(MapTile tile, Drawable original, Drawable replace) {

    }

    @Override
    public void runAsyncTasks() {

    }

    @Override
    public int getMinZoomLevel() {
        return 0;
    }

    @Override
    public int getMaxZoomLevel() {
        return 0;
    }

    @Override
    public void clearCache() {

    }
}
