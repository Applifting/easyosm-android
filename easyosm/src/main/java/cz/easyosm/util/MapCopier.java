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

public class MapCopier {
    public static AsyncTask getAsyncCopier(Resources resources, int resource, File destinationDir, String destinationFilename, CopyListener listener) {
        return new MapCopierAsync(resources, resource, destinationDir,destinationFilename, listener);
    }

    public static void copyMap(Resources resources, int resource, File destinationDir, String destinationFilename) {
        if (!destinationDir.exists()) {
            if (!destinationDir.mkdirs()) Log.e("easyosm", "Failed to create storage directory ("+
                    destinationDir.getAbsolutePath()+") on sdcard");
        }

        File maps = new File(destinationDir.getAbsolutePath()+"/"+destinationFilename);
        if (!maps.exists() || maps.length()==0) {
            InputStream in;
            OutputStream out;
            try {
                in=resources.openRawResource(resource);
                out = new FileOutputStream(maps);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
            }
            catch (FileNotFoundException e) {}
            catch (IOException e) {}
        }
    }


    public static class MapCopierAsync extends AsyncTask<Void, Integer, Void> {
        private Resources resources;
        private int resource;
        private File destinationDir;
        private String destinationFilename;
        private CopyListener listener;

        MapCopierAsync(Resources resources, int resource, File destinationDir, String destinationFilename, CopyListener listener) {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            if (listener!=null) listener.onCopyDone();
        }
    }


    public static interface CopyListener {
        public void onProgressUpdate(int percent);
        public void onCopyDone();
    }
}
