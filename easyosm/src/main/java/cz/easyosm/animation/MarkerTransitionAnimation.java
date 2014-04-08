package cz.easyosm.animation;

import android.view.animation.AccelerateDecelerateInterpolator;

import cz.easyosm.overlay.marker.MarkerBase;
import cz.easyosm.overlay.marker.MarkerTransition;

/**
 * Created by martinjr on 4/8/14.
 */
public class MarkerTransitionAnimation extends MapAnimation {
    private MarkerBase m;

    public MarkerTransitionAnimation(MarkerBase m, int state) {
        this.m=m;
        m.setCurrentTransition(new MarkerTransition(m.getState(), state, 0));
    }

    @Override
    public void init() {
        setDuration(150);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void applyTransformation(float interpolated, long milisElapsed) {
        m.getCurrentTransition().transition=interpolated;
    }

    @Override
    public void end() {
        m.getCurrentTransition().active=false;
        m.setState(m.getCurrentTransition().stateTo);
    }
}
