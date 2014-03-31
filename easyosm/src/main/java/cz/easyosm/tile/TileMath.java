package cz.easyosm.tile;

import android.graphics.Point;

import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.MyMath;

/**
 * Created by martinjr on 3/24/14.
 */
public class TileMath {
    protected static int mTileSize = 256;
    private static final double EarthRadius = 6378137;
    private static final double MinLatitude = -85.05112878;
    private static final double MaxLatitude = 85.05112878;
    private static final double MinLongitude = -180;
    private static final double MaxLongitude = 180;

    public static void setTileSize(final int tileSize) {
        mTileSize = tileSize;
    }

    public static int getTileSize() {
        return mTileSize;
    }

    /**
     * Clips a number to the specified minimum and maximum values.
     *
     * @param n
     *            The number to clip
     * @param minValue
     *            Minimum allowable value
     * @param maxValue
     *            Maximum allowable value
     * @return The clipped value.
     */
    private static double Clip(final double n, final double minValue, final double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /**
     * Determines the map width and height (in pixels) at a specified level of detail.
     *
     * @param levelOfDetail
     *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The map width and height in pixels
     */

    public static int MapSize(final float levelOfDetail) {
        return (int) (mTileSize*Math.pow(2, levelOfDetail));
    }

    /**
     * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
     * detail.
     *
     * @param latitude
     *            Latitude (in degrees) at which to measure the ground resolution
     * @param levelOfDetail
     *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The ground resolution, in meters per pixel
     */
    public static double GroundResolution(double latitude, final float levelOfDetail) {
        latitude = Clip(latitude, MinLatitude, MaxLatitude);
        return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * EarthRadius
                / MapSize(levelOfDetail);
    }

    /**
     * Converts a point from latitude/longitude WGS-84 coordinates (in degrees) into pixel XY
     * coordinates at a specified level of detail.
     *
     * @param latitude
     *            Latitude of the point, in degrees
     * @param longitude
     *            Longitude of the point, in degrees
     * @param levelOfDetail
     *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param reuse
     *            An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the X and Y coordinates in pixels
     */
    public static Point LatLongToPixelXY(double latitude, double longitude,
                                         final float levelOfDetail, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        latitude = Clip(latitude, MinLatitude, MaxLatitude);
        longitude = Clip(longitude, MinLongitude, MaxLongitude);

        final double x = (longitude + 180) / 360;
        final double sinLatitude = Math.sin(latitude * Math.PI / 180);
        final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        final int mapSize = MapSize(levelOfDetail);
        out.x = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
        out.y = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
        return out;
    }

    /**
     * Converts a pixel from pixel XY coordinates at a specified level of detail into
     * latitude/longitude WGS-84 coordinates (in degrees).
     *
     * @param pixelX
     *            X coordinate of the point, in pixels
     * @param pixelY
     *            Y coordinate of the point, in pixels
     * @param levelOfDetail
     *            Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param reuse
     *            An optional GeoPoint to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the latitude and longitude in degrees.
     */
    public static GeoPoint PixelXYToLatLong(final int pixelX, final int pixelY,
                                            final float levelOfDetail, final GeoPoint reuse) {
        final GeoPoint out = (reuse == null ? new GeoPoint(0, 0) : reuse);

        final double mapSize = MapSize(levelOfDetail);
        final double x = (Clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        final double y = 0.5 - (Clip(pixelY, 0, mapSize - 1) / mapSize);

        final double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        final double longitude = 360 * x;

        out.setLat(latitude);
        out.setLon(longitude);
        return out;
    }

    /**
     * Converts pixel XY coordinates into tile XY coordinates of the tile containing the specified
     * pixel.
     *
     * @param pixelX
     *            Pixel X coordinate
     * @param pixelY
     *            Pixel Y coordinate
     * @param reuse
     *            An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the tile X and Y coordinates
     */
    public static Point PixelXYToTileXY(final int pixelX, final int pixelY, final float zoomLevel, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = (int) (pixelX / tileSize(mTileSize, zoomLevel));
        out.y = (int) (pixelY / tileSize(mTileSize, zoomLevel));
        return out;
    }

    /**
     * Converts tile XY coordinates into pixel XY coordinates of the upper-left pixel of the
     * specified tile.
     *
     * @param tileX
     *            Tile X coordinate
     * @param tileY
     *            Tile X coordinate
     * @param reuse
     *            An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the pixel X and Y coordinates
     */
    public static Point TileXYToPixelXY(final int tileX, final int tileY, final float zoomLevel, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = (int) (tileX * tileSize(mTileSize, zoomLevel));
        out.y = (int) (tileY * tileSize(mTileSize, zoomLevel));
        return out;
    }

    /**
     * Calculate tile size for a fractional zoom level
     * @param tileSize base tile size
     * @param zoomLevel desired zoom level
     * @return
     */
    public static double tileSize(int tileSize, float zoomLevel) {
        return tileSize*MyMath.pow2(MyMath.fractionalPart(zoomLevel));
    }

    /**
     * Adjust zoom to make tile size integer
     * @param zoomIn desired zoom
     * @param tileSize base map tile size
     * @return  adjusted zoom level
     */
    public static float adjustZoom(float zoomIn, int tileSize) {
        float newSize=Math.round(tileSize(tileSize, zoomIn));

        return (float) ((int)zoomIn+MyMath.log2(newSize/tileSize));
    }
}
