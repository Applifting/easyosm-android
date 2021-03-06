package cz.easyosm.util;

import android.graphics.Point;

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

    public static int ceil(double x) {
        return (int) Math.ceil(x);
    }

    public static double log2(double x) {
        return Math.log(x)/Math.log(2);
    }

    public static double pow2(double x) {
        return Math.pow(2, x);
    }

    public static float euclidDist(Point a, Point b) {
        return (float) Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
    }

    public static int clip(int x, int min, int max) {
        if (x<min) return min;
        else if (x>max) return max;
        else return x;
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
