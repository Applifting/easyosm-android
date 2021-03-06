package cz.easyosm.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

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
        animation.init();
        activeAnimations.add(animation);
    }

    public void stopAnimation(MapAnimation animation) {
        activeAnimations.remove(animation);

        if (!isAnimating()) {
            parent.clearAnimation();
        }
    }

    public void applyTransformations() {
        for (MapAnimation animation : activeAnimations) {
            animation.frame(getTime()-prevTimeMilis);
        }

        prevTimeMilis=getTime();
    }

    public boolean isAnimating() {
//        Log.d("easyosm", "isAnimating: "+!activeAnimations.isEmpty());
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
