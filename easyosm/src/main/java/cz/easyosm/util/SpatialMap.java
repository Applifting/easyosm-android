package cz.easyosm.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martinjr on 4/4/14.
 */
public class SpatialMap<T extends Placeable> {
    private List<T> points;

    public SpatialMap() {
        points=new ArrayList<T>();
    }

    public List<T> getInGeoRect(GeoRect r) { // TODO: MUST find something less naive
        List<T> ret=new ArrayList<T>();
        for (T point : points) {
            if (r.contains(point.getPoint())) ret.add(point);
        }

        return ret;
    }

    public void add(T point) {
        points.add(point);
    }

    public void add(List<T> pointList) {
        for (T point : pointList) {
            points.add(point);
        }
    }

    public void remove(T point) {
        points.remove(point);
    }
}
