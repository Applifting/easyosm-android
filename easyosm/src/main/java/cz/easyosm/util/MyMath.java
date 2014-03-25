package cz.easyosm.util;

import java.util.Arrays;

/**
 * Created by martinjr on 3/24/14.
 */
public class MyMath {
    /**
     * Get the fractional part of a floating point number
     * @param f
     * @return
     */
    public static float fractionalPart(float f) {
        return f-((int)f);
    }

    /**
     * Calculate tile size for a fractional zoom level
     * @param tileSize base tile size
     * @param zoomLevel desired zoom level
     * @return
     */
    public static double tileSize(int tileSize, float zoomLevel) {
        return tileSize*pow2(fractionalPart(zoomLevel));
    }

    /**
     * Adjust zoom to make tile size integer
     * @param zoomIn desired zoom
     * @param tileSize base map tile size
     * @return  adjusted zoom level
     */
    public static float adjustZoom(float zoomIn, int tileSize) {
        float newSize=Math.round(tileSize(tileSize, zoomIn));

        return (float) ((int)zoomIn+log2(newSize/tileSize));
    }

    public static double log2(double x) {
        return Math.log(x)/Math.log(2);
    }

    public static double pow2(double x) {
        return Math.pow(2, x);
    }

    /**
     * Compute a median of the first 'length' values of an array of floats
     * @param values
     * @param length
     * @return
     */
    public static float median(float[] values, int length) {
        float[] copy=new float[length];

        for (int i=0; i<length; i++) {
            copy[i]=values[i];
        }

        Arrays.sort(copy);

        if (length%2==0) return (values[length/2]+values[length/2-1])/2;
        else return values[length/2];
    }
}
