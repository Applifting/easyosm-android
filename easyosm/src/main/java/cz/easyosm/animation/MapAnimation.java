package cz.easyosm.animation;

import android.view.animation.Interpolator;

/**
 * Created by martinjr on 4/1/14.
 */
public abstract class MapAnimation {
    protected MapChoreographer choreographer;
    protected long duration=-1, elapsed=0;
    protected Interpolator interpolator=null;

    public final void frame(long milisElapsed) {
        elapsed+=milisElapsed;

        float interpolated;
        if (duration>0) {
            if (interpolator!=null) interpolated=interpolator.getInterpolation((float)elapsed/duration);
            else interpolated=(float)elapsed/duration;
        }
        else interpolated=0;

        applyTransformation(interpolated, milisElapsed);

        if (duration>0 && elapsed>=duration) {
            end();
            abort();
        }
    }

    public abstract void init();

    /**
     * Apply the animation transformation
     * @param milisElapsed
     */
    public abstract void applyTransformation(float interpolated, long milisElapsed);

    public abstract void end();

    /**
     * End the animation
     */
    public final void abort() {
        choreographer.stopAnimation(this);
    }

    public void setChoreographer(MapChoreographer choreographer) {
        this.choreographer=choreographer;
    }

    protected void setDuration(long duration) {
        this.duration=duration;
    }

    protected void setInterpolator(Interpolator interpolator) {
        this.interpolator=interpolator;
    }
}
