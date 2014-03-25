package cz.easyosm.tile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class MBTilesArchive {
    private final SQLiteDatabase db;

    //	TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB);
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    private MBTilesArchive(final SQLiteDatabase database) {
        db=database;
    }

    public static MBTilesArchive getDatabaseFileArchive(final File file) throws SQLiteException {
        Log.d("iPass", "Opening file "+file.getAbsolutePath());
        return new MBTilesArchive(
                SQLiteDatabase.openDatabase(
                        file.getAbsolutePath(),
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY));
    }

    public InputStream getInputStream(MapTile tile) {
        try {
            InputStream ret = null;
            final String[] dataColumn = { COL_TILES_TILE_DATA };
            final String[] xyz = {
                    Integer.toString(tile.x)
                    , Integer.toString((1<<tile.zoom) - tile.y - 1)  // Use Google Tiling Spec
                    , Integer.toString(tile.zoom)
            };
            //Log.d("iPass", "Fetching tile "+tile+" = ["+xyz[0]+","+xyz[1]+"], zoom="+xyz[2]);

            final Cursor cur = db.query(TABLE_TILES, dataColumn, "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

            if(cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
                //Log.d("iPass", "Fetched tile "+tile);
            }
            cur.close();
            if (ret != null) {
                return ret;
            }
        } catch(final Throwable e) {
            Log.w("Error getting db stream: "+tile, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return "DatabaseFileArchive [db=" + db.getPath() + "]";
    }

}
