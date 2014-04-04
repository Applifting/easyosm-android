package cz.easyosm.tile;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

/**
 * Created by martinjr on 3/24/14.
 */
public abstract class TileProviderBase {
    protected int minZoomLevel, maxZoomLevel, minDataLevel, maxDataLevel;

    public abstract Drawable getTile(MapTile tile);
    public abstract void onTileAnimationDone(MapTile tile, Drawable original, Drawable replace);

    public void setZoomLimits(int minZoomLevel, int maxZoomLevel) {
        this.minZoomLevel=minZoomLevel;
        this.maxZoomLevel=maxZoomLevel;
    }

    public void setDataLimits(int minDataLevel, int maxDataLevel) {
        this.minDataLevel=minDataLevel;
        this.maxDataLevel=maxDataLevel;
    }

    public int getMinZoomLevel() {
        return minZoomLevel;
    }

    public void setMinZoomLevel(int minZoomLevel) {
        this.minZoomLevel=minZoomLevel;
    }

    public int getMaxZoomLevel() {
        return maxZoomLevel;
    }

    public void setMaxZoomLevel(int maxZoomLevel) {
        this.maxZoomLevel=maxZoomLevel;
    }

    public int getMaxDataLevel() {
        return maxDataLevel;
    }

    public void setMaxDataLevel(int maxDataLevel) {
        this.maxDataLevel=maxDataLevel;
    }
}
