package cz.easyosm.overlay.location;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import cz.easyosm.overlay.MapOverlayBase;
import cz.easyosm.util.GeoPoint;
import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 4/4/14.
 */
public class LocationOverlay extends MapOverlayBase {
    private static Paint paint=new Paint();
    static {
        paint.setColor(Color.GREEN);
        paint.setAlpha(180);
    }

    private LocationManager manager;

    private Location location;

    private LocationListener listener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationOverlay.this.location=location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public LocationOverlay(MapView parent) {
        super(parent);
        manager=(LocationManager) parent.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public void enableLocation(boolean enable) {
        if (enable) manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, listener);
        else manager.removeUpdates(listener);
    }

    @Override
    public void onDraw(Canvas c) {
        if (location==null) return;
        Point p=getPoint(new GeoPoint(location.getLatitude(), location.getLongitude()));
        c.drawCircle(p.x, p.y, 10, paint);
    }

    @Override
    public boolean onSingleTap(Point touch) {
        return false;
    }

    @Override
    public boolean onDoubleTap(Point touch) {
        return false;
    }

    @Override
    public boolean onLongPress(Point touch) {
        return false;
    }
}
