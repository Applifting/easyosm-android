package cz.easyosm.animation;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cz.easyosm.view.MapView;

/**
 * Created by martinjr on 4/1/14.
 */
public class MapChoreographer {
    private Animation container;

    private List<MapAnimation> activeAnimations;

    private long prevTimeMilis=-1;

    MapView parent;

    public MapChoreographer(MapView parent) {
        container=new ContainerAnimation();

        activeAnimations=new CopyOnWriteArrayList<MapAnimation>();

        this.parent=parent;
    }

    public void runAnimation(MapAnimation animation) {
        if (!isAnimating()) {
            prevTimeMilis=getTime();
            parent.startAnimation(container);
        }

        animation.setChoreographer(this);
        activeAnimations.add(animation);
    }

    public void stopAnimation(MapAnimation animation) {
        activeAnimations.remove(animation);

        if (!isAnimating()) {
            Log.d("iPass", "Quit container animation");
            parent.clearAnimation();
        }
    }

    public void applyTransformations() {
        for (MapAnimation animation : activeAnimations) {
            animation.applyTransformation(getTime()-prevTimeMilis);
            }

        prevTimeMilis=getTime();
    }

    public boolean isAnimating() {
//        Log.d("iPass", "isAnimating: "+!activeAnimations.isEmpty());
        return !activeAnimations.isEmpty();
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    private class ContainerAnimation extends Animation {
        public ContainerAnimation() {
            super();
            setRepeatCount(INFINITE);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            applyTransformations();
            parent.postInvalidate();
        }
    }
}
