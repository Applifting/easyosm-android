package cz.easyosm.demo;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import cz.easyosm.overlay.location.LocationOverlay;
import cz.easyosm.overlay.marker.Marker;
import cz.easyosm.overlay.marker.MarkerOverlay;
import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.GeoRect;
import cz.easyosm.util.MapCopier;
import cz.easyosm.view.MapView;

public class MainActivity extends ActionBarActivity {
    private MapView map;

    private MarkerOverlay markers;
    private LocationOverlay location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(null);

        final GeoPoint home=new GeoPoint(50.0859031, 14.4181083);

        MapCopier copier=new MapCopier(getResources(), R.raw.map, new File(Environment.getExternalStorageDirectory()+"/easyosm"), "map.example", new MapCopier.CopyProgressListener() {
            @Override
            public void onProgressUpdate(int percent) {
                Log.d("iPass", "copy progress "+percent+"B");
            }
        });

        if (copier.needsRunning()) copier.execute();

        map=(MapView) findViewById(R.id.map);
        map.setTileFile(new File(Environment.getExternalStorageDirectory().getPath()+"/osmdroid/map.mbtiles"));
        map.setZoomLimits(13, 19);
        map.setBounds(new GeoRect(50.1140122, 14.3720592, 50.0638497, 14.4552053));
        map.setZoomLevel(15);

        List<Marker> list=new LinkedList();

        list.add(new Marker(new GeoPoint(50.0876850, 14.4210361), 1));
        list.add(new Marker(new GeoPoint(50.0862617, 14.4161050), 2));
        list.add(new Marker(new GeoPoint(50.0909300, 14.4161600), 3));
        list.add(new Marker(new GeoPoint(50.0870928, 14.4070786), 4));
        list.add(new Marker(new GeoPoint(50.0847550, 14.4178567), 5));
        list.add(new Marker(new GeoPoint(50.0871742, 14.4205253), 6));
        list.add(new Marker(new GeoPoint(50.0868592, 14.4222694), 7));
        list.add(new Marker(new GeoPoint(50.0872314, 14.4211814), 8));

        markers=new MarkerOverlay(map);
        markers.addMarkers(list);
        map.addOverlay(markers);

        location=new LocationOverlay(map);
        map.addOverlay(location);

        map.addMapListener(new MapView.MapListener() {
            @Override
            public void onZoom(float newZoom) {
            }

            @Override
            public void onZoomFinished(float zoomLevel) {

            }
        });

        ViewTreeObserver vto=map.getViewTreeObserver();
        if (vto != null) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() { // will run once after the first layout
                    map.setViewCenter(home);
                    map.resetOverscroll();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        location.enableLocation(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        location.enableLocation(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
