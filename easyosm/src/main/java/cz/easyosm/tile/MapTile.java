package cz.easyosm.tile;

/**
 * Created by martinjr on 3/25/14.
 */
public class MapTile {
    public int x, y, zoom;

    public MapTile() {};

    public MapTile(int x, int y, int zoom) {
        this.x=x;
        this.y=y;
        this.zoom=zoom;
    }

    public MapTile(MapTile original) {
        this.x=original.x;
        this.y=original.y;
        this.zoom=original.zoom;
    }

    @Override
    public String toString() {
        return "["+x+
                ","+y+
                "], zoom="+zoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null || getClass()!=o.getClass()) return false;

        MapTile mapTile=(MapTile) o;

        if (x!=mapTile.x) return false;
        if (y!=mapTile.y) return false;
        if (zoom!=mapTile.zoom) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result=x;
        result=31*result+y;
        result=31*result+zoom;
        return result;
    }
}
