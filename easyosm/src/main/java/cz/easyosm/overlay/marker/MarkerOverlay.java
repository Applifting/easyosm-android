package cz.easyosm.overlay.marker;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.easyosm.animation.MarkerTransitionAnimation;
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
    private ClusterFactory cf=new ClusterFactory();

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
                if (listener==null) return false;
                else {
                    if (marker.isCluster()) return listener.onClusterTap((Cluster) marker);
                    else return listener.onMarkerTap((Marker) marker);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(Point touch) {
        for (MarkerBase marker : clustered) {
            if (MyMath.euclidDist(getPoint(marker.getPoint()), touch)<16) {
                if (listener==null) return false;
                else {
                    if (marker.isCluster()) return listener.onClusterDoubleTap((Cluster) marker);
                    else return listener.onMarkerDoubleTap((Marker) marker);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onLongPress(Point touch) {
        for (MarkerBase marker : clustered) {
            if (MyMath.euclidDist(getPoint(marker.getPoint()), touch)<16) {
                if (listener==null) return false;
                else {
                    if (marker.isCluster()) return listener.onClusterLongPress((Cluster) marker);
                    else return listener.onMarkerLongPress((Marker) marker);
                }
            }
        }

        return false;
    }

    public void addMarkers(List<Marker> addMarkers) {
        markers.addAll(addMarkers);
        recluster(parent.getZoomLevel());
    }

    public void setMarkerState(MarkerBase m, int state) {
        m.setState(state);
    }

    public void animateMarkerState(MarkerBase m, int state) {
        parent.getChoreographer().runAnimation(new MarkerTransitionAnimation(m, state));
    }

    @Override
    public void onZoom(float newZoom) {
        reclusterFast(newZoom);
    }

    @Override
    public void onZoomFinished(float zoomLevel) {
        recluster(zoomLevel);
    }

    @Override
    public boolean onMapTap() {
        return false;
    }

    private void recluster(float zoomLevel) {
        Log.d("easyosm", "Reclustering to "+zoomLevel);
        int markerHeight=30;

        Marker a, b;
        Cluster mc=null;

        int magicalBound=(int) (markerHeight*0.9); // permit 10% overlap

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
                    mc=cf.newCluster(a, b);
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

    public void clearMarkers() {
        markers.clear();
        recluster(parent.getZoomLevel());
    }

    public void setClusterFactory(ClusterFactory cf) {
        this.cf=cf;
    }

    public interface MarkerListener {
        public boolean onMarkerTap(Marker m);
        public boolean onClusterTap(Cluster c);

        public boolean onMarkerDoubleTap(Marker m);
        public boolean onClusterDoubleTap(Cluster c);

        public boolean onMarkerLongPress(Marker m);
        public boolean onClusterLongPress(Cluster c);
    }
}
