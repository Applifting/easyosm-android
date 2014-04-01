package cz.easyosm.animation;

/**
 * Created by martinjr on 4/1/14.
 */
public abstract class MapAnimation {
    private MapChoreographer choreographer;

    public void abort() {
        choreographer.stopAnimation(this);
    }

    public abstract void applyTransformation(long milisElapsed);

    public void setChoreographer(MapChoreographer choreographer) {
        this.choreographer=choreographer;
    }
}
