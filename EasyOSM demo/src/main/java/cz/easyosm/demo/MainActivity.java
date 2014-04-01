package cz.easyosm.demo;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import java.io.File;

import cz.easyosm.util.GeoPoint;
import cz.easyosm.view.MapView;

public class MainActivity extends ActionBarActivity {
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(null);

        GeoPoint home=new GeoPoint(50.087, 14.420);

        map=(MapView) findViewById(R.id.map);
        map.setTileFile(new File(Environment.getExternalStorageDirectory().getPath()+"/osmdroid/map.mbtiles"));
        map.setZoomLimits(10, 19, 13, 17);
        map.setZoomLevel(15);
        map.setViewCenter(home);

        SeekBar zoom=(SeekBar)findViewById(R.id.zoom);
        zoom.setMax(1000);
        zoom.setProgress(750);
        zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                map.setZoomLevel(progress/50f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
