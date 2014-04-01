package cz.easyosm.animation;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 4/1/14.
 */
public class MapChoreographer {
    private List<MapAnimation> activeAnimations;

    private long prevTimeMilis=-1;

    private MapView parent;

    public MapChoreographer(MapView parent) {
        activeAnimations=new LinkedList<MapAnimation>();

        this.parent=parent;
    }

    public void runAnimation(MapAnimation animation) {
        Log.d("iPass", "Running animation");
        if (!isAnimating()) prevTimeMilis=getTime();

        animation.setChoreographer(this);
        activeAnimations.add(animation);
    }

    public void stopAnimation(MapAnimation animation) {
        activeAnimations.remove(animation);
    }

    public void applyTransformations() {
        Log.d("iPass", "Applying transformations");
        for (MapAnimation animation : activeAnimations) {
            animation.applyTransformation(getTime()-prevTimeMilis);
        }

        prevTimeMilis=getTime();
    }

    public boolean isAnimating() {
        Log.d("iPass", "isAnimating: "+!activeAnimations.isEmpty());
        return !activeAnimations.isEmpty();
    }

    private long getTime() {
        return System.currentTimeMillis();
    }
}
