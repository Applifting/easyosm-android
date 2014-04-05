package cz.easyosm.util;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by martinjr on 4/5/14.
 */
public class MapCopier extends AsyncTask<Void, Integer, Void> {
    private Resources resources;
    private int resource;
    private File destinationDir;
    private String destinationFilename;
    private CopyProgressListener listener;

    public MapCopier(Resources resources, int resource, File destinationDir, String destinationFilename, CopyProgressListener listener) {
        this.resources=resources;
        this.resource=resource;
        this.destinationDir=destinationDir;
        this.destinationFilename=destinationFilename;
        this.listener=listener;
    }

    public boolean needsRunning() {
        if (!destinationDir.exists()) return true;

        File maps = new File(destinationDir.getAbsolutePath()+"/"+destinationFilename);
        return (!maps.exists() || maps.length()==0);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("easyosm", "copying map");

        if (!destinationDir.exists()) {
            Log.d("easyosm", "create containing dir");
            if (!destinationDir.mkdirs()) Log.e("easyosm", "Failed to create storage directory ("+
                    destinationDir.getAbsolutePath()+") on sdcard");
        }

        File maps = new File(destinationDir.getAbsolutePath()+"/"+destinationFilename);
        if (!maps.exists() || maps.length()==0) {
            Log.d("easyosm", "Map file ("+maps.getAbsolutePath()+") not found, copy!");

            int progress=0;

            InputStream in;
            OutputStream out;
            try {
                in=resources.openRawResource(resource);
                out = new FileOutputStream(maps);
                Log.d("easyosm", "Copying map archive");
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                    publishProgress(progress+=len);
                }
                out.close();
                Log.d("easyosm", "Done");
            }
            catch (FileNotFoundException e) {
                Log.d("easyosm", "File not found");
            }
            catch (IOException e) {
                Log.d("easyosm", "IO ex");
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (listener!=null) listener.onProgressUpdate(values[0]);
    }

    public static interface CopyProgressListener {
        public void onProgressUpdate(int percent);
    }
}
