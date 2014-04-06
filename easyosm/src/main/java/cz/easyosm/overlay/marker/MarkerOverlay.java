package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.easyosm.overlay.MapOverlayBase;
import cz.easyosm.tile.TileMath;
import cz.easyosm.util.MyMath;
import cz.easyosm.util.SpatialMap;
import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 4/4/14.
 */
public class MarkerOverlay extends MapOverlayBase implements MapView.MapListener {
    private List<Marker> markers;
    private SpatialMap<MarkerBase> clustered;

    private MarkerListener listener;

    public MarkerOverlay(MapView parent) {
        super(parent);
        parent.addMapListener(this);
        markers=new ArrayList<Marker>();
        clustered=new SpatialMap<MarkerBase>();
    }

    @Override
    public void onDraw(Canvas c) {
        for (MarkerBase marker : clustered.getInGeoRect(parent.getViewGeoRect())) {
            marker.onDraw(c, getPoint(marker.getPoint()));
        }
    }

    @Override
    public boolean onSingleTap(Point touch) {
        for (MarkerBase marker : clustered) {
            if (MyMath.euclidDist(getPoint(marker.getPoint()), touch)<16) {
                if (listener!=null) return listener.onMarkerTap(marker);
                else return false;
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(Point touch) {
        for (MarkerBase marker : clustered) {
            if (MyMath.euclidDist(getPoint(marker.getPoint()), touch)<16) {
                if (listener!=null) return listener.onMarkerDoubleTap(marker);
                else return false;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(Point touch) {
        for (MarkerBase marker : clustered) {
            if (MyMath.euclidDist(getPoint(marker.getPoint()), touch)<16) {
                if (listener!=null) listener.onMarkerLongPress(marker);
            }
        }
    }

    public void addMarkers(List<Marker> addMarkers) {
        markers.addAll(addMarkers);
        recluster(parent.getZoomLevel());
    }

    @Override
    public void onZoom(float newZoom) {
        reclusterFast(newZoom);
    }

    @Override
    public void onZoomFinished(float zoomLevel) {
        recluster(zoomLevel);
    }

    private void recluster(float zoomLevel) {
        Log.d("easyosm", "Reclustering to "+zoomLevel);
        int markerHeight=32;

        Marker a, b;
        Cluster mc=null;

        int magicalBound=(int) (markerHeight*0.8); // permit 10% overlap

        // reset all markers
        for (Marker m : markers) {
            m.clustered=false;
        }

        clustered.clear();

        for (int i=0; i<markers.size(); i++) {
            mc=null;
            a=markers.get(i);

//            Log.d("iPass", "start marker "+a.id);

            if (/*a.isActive() || */!a.isClusterable()) {
//                Log.d("iPass", a.id+" not clusterable");
                clustered.add(a);
                continue;
            }

            if (a.clustered) {
//                Log.d("iPass", a.id+" already merged");
                continue; // skip already merged
            }

            for (int j=i+1; j<markers.size(); j++) {
                b=markers.get(j);
//                Log.d("iPass", "compare to "+b.id);
                if (b.clustered/* || b.isActive()*/ || !b.isClusterable()) {
//                    Log.d("iPass", b.id+" already clustered");
                    continue;
                }

                if (mc!=null && TileMath.pointDistancePx(mc.getPoint(), b.getPoint(), zoomLevel)<magicalBound) {
//                    Log.d("iPass", "add "+b.id+" to current cluster");
                    mc.add(b);
                    continue;
                }

                if (TileMath.pointDistancePx(a.getPoint(), b.getPoint(), zoomLevel)<magicalBound) {
//                    Log.d("iPass", "merge to a new cluster");
                    mc=new Cluster(a, b);
                    clustered.add(mc);
                }
            }

            if (!a.clustered) {
                clustered.add(a);
//                Log.d("iPass", a.id+" will not be clustered");
            }
        }
    }

    private void reclusterFast(float zoomLevel) {

    }

    public void setListener(MarkerListener listener) {
        this.listener=listener;
    }

    public interface MarkerListener {
        public boolean onMarkerTap(MarkerBase m);
        public boolean onMarkerDoubleTap(MarkerBase m);
        public boolean onMarkerLongPress(MarkerBase m);

    }
}
