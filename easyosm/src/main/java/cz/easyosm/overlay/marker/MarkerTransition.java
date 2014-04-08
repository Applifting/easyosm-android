package cz.easyosm.overlay.marker;

/**
 * Created by martinjr on 4/8/14.
 */
public class MarkerTransition {
    public boolean active=true;
    public int stateFrom, stateTo;
    public float transition;

    public MarkerTransition(int stateFrom, int stateTo, float transition) {
        this.stateFrom=stateFrom;
        this.stateTo=stateTo;
        this.transition=transition;
    }
}
