package cz.easyosm.tile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MBTilesArchive {
    private final SQLiteDatabase db;

    //	TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB);
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    private int minDataLevel=-1, maxDataLevel=-1;

    private MBTilesArchive(final SQLiteDatabase database) {
        db=database;
    }

    public static MBTilesArchive getDatabaseFileArchive(final File file) throws SQLiteException {
        Log.d("easyosm", "Opening file "+file.getAbsolutePath());
        return new MBTilesArchive(
                SQLiteDatabase.openDatabase(
                        file.getAbsolutePath(),
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY));
    }

    public InputStream getInputStream(MapTile tile) {
        Cursor cur=null;
        try {
            InputStream ret = null;
            final String[] dataColumn = { COL_TILES_TILE_DATA };
            final String[] xyz = {
                    Integer.toString(tile.x)
                    , Integer.toString((1<<tile.zoom) - tile.y - 1)  // Use Google Tiling Spec
                    , Integer.toString(tile.zoom)
            };

            cur = db.query(TABLE_TILES, dataColumn, "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }

            return ret;
        } catch(final Throwable e) {
            Log.w("Error getting db stream: "+tile, e);
        } finally {
            cur.close();
        }

        return null;
    }

    public int getMinDataLevel() {
        if (minDataLevel==-1) fetchDataLevels();

        return minDataLevel;
    }

    public int getMaxDataLevel() {
        if (maxDataLevel==-1) fetchDataLevels();

        return maxDataLevel;
    }

    private void fetchDataLevels() {
        Cursor cur = db.query(TABLE_TILES, new String[] {"min(zoom_level)", "max(zoom_level)"}, null, null, null, null, null);

        if (cur.getCount() != 0) {
            cur.moveToFirst();
            minDataLevel=cur.getInt(0);
            maxDataLevel=cur.getInt(1);
        }
    }

    @Override
    public String toString() {
        return "DatabaseFileArchive [db=" + db.getPath() + "]";
    }

}
