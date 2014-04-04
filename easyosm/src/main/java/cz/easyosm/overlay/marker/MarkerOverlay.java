package cz.easyosm.overlay.marker;

import android.graphics.Canvas;

import java.util.List;

import cz.easyosm.overlay.MapOverlayBase;
import cz.easyosm.util.SpatialMap;
import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 4/4/14.
 */
public class MarkerOverlay extends MapOverlayBase implements MapView.MapListener {
    private SpatialMap<Marker> markers;

    public MarkerOverlay(MapView parent) {
        super(parent);
        parent.addMapListener(this);
        markers=new SpatialMap<Marker>();
    }

    @Override
    public void onDraw(Canvas c) {
        for (Marker marker : markers.getInGeoRect(parent.getViewGeoRect())) {
            marker.onDraw(c, getPoint(marker.getPoint()));
        }
    }

    public void addMarkers(List<Marker> addMarkers) {
        markers.add(addMarkers);
    }

    @Override
    public void onZoom(float newZoom) {

    }

    @Override
    public void onZoomFinished(float zoomLevel) {
        recluster(zoomLevel);
    }

    private void recluster(float zoomLevel) {

    }
}
