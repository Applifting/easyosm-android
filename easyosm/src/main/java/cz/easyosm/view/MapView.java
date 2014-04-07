package cz.easyosm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.easyosm.animation.MapAnimation;
import cz.easyosm.animation.MapChoreographer;
import cz.easyosm.animation.TileFadeAnimation;
import cz.easyosm.overlay.MapOverlayBase;
import cz.easyosm.tile.MapTile;
import cz.easyosm.tile.OfflineTileProvider;
import cz.easyosm.tile.OnlineTileProvider;
import cz.easyosm.tile.TileMath;
import cz.easyosm.tile.TileProviderBase;
import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.GeoRect;
import cz.easyosm.util.MyMath;

/**
 * Created by martinjr on 3/24/14.
 */
public class MapView extends View {
    static final int OVERSCROLL_WIDTH=100;
    static final int OVERCOLOR=Color.argb(100, 0, 0x99, 0xCC);
    static final int NOCOLOR=Color.argb(0, 0, 0, 0);

    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;
    private MapChoreographer choreographer;

    private TileProviderBase tileProvider;
    private Map<MapTile, TileFadeAnimation> fades;
    private List<MapOverlayBase> overlays;

    private int x=0, y=0;
    private float zoomLevel=10;
    private int minZoomLevel=0, maxZoomLevel=Integer.MAX_VALUE;
    private GeoRect geoBounds=new GeoRect(-180., 86, 180, -86);
    private Rect bounds=new Rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private double overscrollX=0, overscrollY=0;
    private Paint overscrollPaint;

    private boolean isScaling=false;

    private Paint textPaint, testPaint;

    private Point focus;

    private Handler animationHandler;

    private List<MapListener> listeners;

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scroller=new OverScroller(getContext());
        animationHandler=new Handler();
        fades=new HashMap<MapTile, TileFadeAnimation>();
        listeners=new LinkedList<MapListener>();

        overscrollPaint=new Paint();

        testPaint=new Paint();
        testPaint.setColor(0x55ffffff);

        textPaint=new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLACK);

        focus=new Point();

        scaleGestureDetector = new ScaleGestureDetector(getContext(), scaleGestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        choreographer=new MapChoreographer(this);

        overlays=new ArrayList<MapOverlayBase>(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTiles(canvas);

        for (MapOverlayBase overlay : overlays) {
            overlay.onDraw(canvas);
        }

        drawOverscroll(canvas);

        canvas.drawRect(0f, 0f, (float)getWidth(), 40f, testPaint);
        canvas.drawText(String.format("x: %d; y: %d; z: %.3f", x, y, zoomLevel), 10, 35, textPaint);

        tileProvider.runAsyncTasks();

//        if (isScaling) canvas.drawCircle(focus.x, focus.y, 50, testPaint);
    }

    private void drawTiles(Canvas canvas) {
        Point tl=TileMath.PixelXYToTileXY(x, y, zoomLevel, null);
        Point br=TileMath.PixelXYToTileXY(x+canvas.getWidth(), y+canvas.getHeight(), zoomLevel, null);
        Drawable d;

        double tileSize=TileMath.tileSize(256, zoomLevel);

        MapTile curr;
        Rect tileRect=new Rect();

        for (int i=tl.y; i<=br.y; i++) {
            for (int j=tl.x; j<=br.x; j++) {
                curr=new MapTile(j, i, (int) zoomLevel);

                tileRect.set(MyMath.ceil(j*tileSize)-x, MyMath.ceil(i*tileSize)-y,
                        MyMath.ceil((j+1)*tileSize)-x, MyMath.ceil((i+1)*tileSize)-y);

                if (fades.containsKey(curr)) {
                    fades.get(curr).drawTile(canvas, tileRect);
                }
                else {
                    d=tileProvider.getTile(curr);
                    d.setBounds(tileRect);
                    d.setAlpha(255);
                    d.draw(canvas);
                }
            }
        }
    }

    private void drawOverscroll(Canvas canvas) {
        if (overscrollX>0) { // left overscroll
            overscrollPaint.setShader(new LinearGradient(0, 0, (float) overscrollX, 0, new int[]{OVERCOLOR, NOCOLOR}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, (float) overscrollX, getHeight(), overscrollPaint);
        }
        else if (overscrollX<0) { // right
            overscrollPaint.setShader(new LinearGradient(canvas.getWidth(), 0, (float) (canvas.getWidth()+overscrollX), 0, new int[]{OVERCOLOR, NOCOLOR}, null, Shader.TileMode.CLAMP));
            canvas.drawRect((float) (getWidth()+overscrollX), 0, getWidth(), getHeight(), overscrollPaint);
        }

        if (overscrollY>0) { // top
            overscrollPaint.setShader(new LinearGradient(0, 0, 0, (float) overscrollY, new int[]{OVERCOLOR, NOCOLOR}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, getWidth(), (float) overscrollY, overscrollPaint);
        }
        else if (overscrollY<0) { // bottom
            overscrollPaint.setShader(new LinearGradient(0, getHeight(), 0, (float) (getHeight()+overscrollY), new int[]{OVERCOLOR, NOCOLOR}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, (float) (getHeight()+overscrollY), getWidth(), getHeight(), overscrollPaint);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.computeScrollOffset()) {
            setPosition(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getActionMasked()==MotionEvent.ACTION_UP) releaseOverscroll();

        return scaleGestureDetector.onTouchEvent(event) |
            gestureDetector.onTouchEvent(event) |
            super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Callback from another thread - will NOT run on the UI thread
     */
    public void onTileLoaded(final MapTile tile, final Drawable original, final Drawable replace) {
        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                //Log.d("easyosm", "Run tile fade for "+tile);
                TileFadeAnimation fade=new TileFadeAnimation(fades, tile, original, replace);
                choreographer.runAnimation(fade);
            }
        });
//        onTileAnimationDone(tile, original, replace);
//        postInvalidate();
    }

    public void onTileAnimationDone(MapTile tile, Drawable original, Drawable replace) {
        tileProvider.onTileAnimationDone(tile, original, replace);
    }

    @Override
    public void clearAnimation() {
        super.clearAnimation();
        fades.clear();
    }

    public void setTileFile(File f) {
        tileProvider=new OfflineTileProvider(this, f);
    }

    public void setTileURL(String url) {
        tileProvider=new OnlineTileProvider();
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float newZoom) {
        setZoomLevelFixing(newZoom, getWidth()/2, getHeight()/2);
    }

    public void setZoomLevelFixing(float newZoom, int fixX, int fixY) {
        if (newZoom>maxZoomLevel || newZoom<minZoomLevel) return;

        double dz=MyMath.pow2(newZoom-zoomLevel);

        zoomLevel=newZoom;
        setPosition((int) ((x+fixX)*dz-fixX), (int) ((y+fixY)*dz-fixY));

        for (MapListener listener : listeners) {
            listener.onZoom(newZoom);
        }

        postInvalidate();
    }

    public void setZoomLimits(int minZoomLevel, int maxZoomLevel) {
        this.minZoomLevel=minZoomLevel;
        this.maxZoomLevel=maxZoomLevel;
    }

    public void setViewCenter(GeoPoint point) {
        Point newCenter=TileMath.LatLongToPixelXY(point.lat, point.lon, zoomLevel, null);

        setPosition(newCenter.x-getWidth()/2, newCenter.y-getHeight()/2);

        postInvalidate();
    }

    public GeoRect getViewGeoRect() {
        GeoPoint tl, br;
        tl=TileMath.PixelXYToLatLong(x, y, zoomLevel, null);
        br=TileMath.PixelXYToLatLong(x+getWidth(), y+getHeight(), zoomLevel, null);

        return new GeoRect(tl, br);
    }

    public void addOverlay(MapOverlayBase overlay) {
        overlays.add(overlay);
    }

    public void addMapListener(MapListener listener) {
        this.listeners.add(listener);
    }

    public int getOffsetX() {
        return x;
    }

    public int getOffsetY() {
        return y;
    }

    public MapChoreographer getChoreographer() {
        return choreographer;
    }

    public void setBounds(GeoRect bounds) {
        this.geoBounds=bounds;
    }

    /**
     * Set view position on the map, determine if an overscroll occured
     * @param nx
     * @param ny
     * @return true if everything is all right, false on overscroll
     */
    private boolean setPosition(int nx, int ny) {
        bounds=geoBounds.toMap(zoomLevel, bounds);

        x=MyMath.clip(nx, bounds.left, bounds.right-getWidth());
        y=MyMath.clip(ny, bounds.top, bounds.bottom-getHeight());
        Log.d("easyosm", "bound view to "+bounds);
        Log.d("easyosm", nx+":"+ny+" == "+x+":"+y);

        if (x==nx && y==ny) {
            if (overscrollX!=0 || overscrollY!=0) releaseOverscroll();
            return true;
        }
        else {
            Log.d("easyosm", "OVERSCROLL");

            if (!scroller.isFinished()) { // fling in progress
                scroller.abortAnimation();
                overscrollFling(x-nx, y-ny);
            }
            else {
                overscrollX+=(x-nx)*(MyMath.clip((int) (OVERSCROLL_WIDTH-Math.abs(overscrollX)), 0, OVERSCROLL_WIDTH)/(double)OVERSCROLL_WIDTH);//*Math.pow(1-1e-2, Math.abs(overscrollX));
                overscrollY+=(y-ny)*(MyMath.clip((int) (OVERSCROLL_WIDTH-Math.abs(overscrollY)), 0, OVERSCROLL_WIDTH)/(double)OVERSCROLL_WIDTH);//*Math.pow(1-1e-2, Math.abs(overscrollY));
            }

            return false;
        }
    }

    private void overscrollFling(int dx, int dy) {
        choreographer.runAnimation(new FlingOverscrollAnimation(dx, dy));
    }

    private void releaseOverscroll() {
        if (overscrollX==0 && overscrollY==0) return;

        choreographer.runAnimation(new ReleaseOverscrollAnimation());
    }

    public void resetOverscroll() {
        overscrollX=0;
        overscrollY=0;
    }

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector sgd) {
            isScaling=true;
            invalidate();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector sgd) {
            double dz=sgd.getScaleFactor();
            setZoomLevelFixing((float) (zoomLevel+MyMath.log2(dz)), (int) sgd.getFocusX(), (int) sgd.getFocusY());

            focus.x=(int) sgd.getFocusX();
            focus.y=(int) sgd.getFocusY();

            invalidate();

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector sgd) {
            isScaling=false;

            for (MapListener listener : listeners) {
                listener.onZoomFinished(zoomLevel);
            }

            invalidate();
        }
    };

    private final GestureDetector.SimpleOnGestureListener gestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            scroller.abortAnimation();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Point touch=new Point((int)e.getRawX(), (int) e.getRawY());
            for (MapOverlayBase overlay : overlays) {
                overlay.onLongPress(touch);
            }
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            boolean consumed=false;
            Point touch=new Point((int)e.getRawX(), (int) e.getRawY());
            for (MapOverlayBase overlay : overlays) {
                consumed|=overlay.onSingleTap(touch);
            }

            return consumed;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean consumed=false;
            Point touch=new Point((int)e.getRawX(), (int) e.getRawY());
            for (MapOverlayBase overlay : overlays) {
                consumed|=overlay.onDoubleTap(touch);
            }

            if (consumed) return true;

            choreographer.runAnimation(new MapAnimation() {
                private float originalZoom=zoomLevel;
                private int duration=500, elapsed=0;

                @Override
                public void applyTransformation(long milisElapsed) {
                    float add=((float)elapsed)/duration;
                    setZoomLevel(originalZoom+add);

                    elapsed+=milisElapsed;
                    if (elapsed>duration) abort();
                }
            });

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScaling) return false;

            setPosition((int)(x+distanceX), (int)(y+distanceY));

            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling(x, y, (int) -velocityX, (int) -velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            return true;
        }
    };

    public interface MapListener {
        public void onZoom(float newZoom);

        void onZoomFinished(float zoomLevel);
    }

    private class ReleaseOverscrollAnimation extends MapAnimation {
        long duration=500;

        double dX=-overscrollX/duration;
        double dY=-overscrollY/duration;

        @Override
        public void applyTransformation(long milisElapsed) {
            overscrollX=(overscrollX*dX<0) ? (int) (overscrollX+milisElapsed*dX) : 0;
            overscrollY=(overscrollY*dY<0) ? (int) (overscrollY+milisElapsed*dY) : 0;

            if (overscrollX==0 && overscrollY==0) abort();
        }
    }

    private class FlingOverscrollAnimation extends MapAnimation {
        long duration=50, elapsed=0;

        double dX, dY;

        public FlingOverscrollAnimation(int difx, int dify) {
            dX=5.*difx/duration;
            dY=5.*dify/duration;
        }

        @Override
        public void applyTransformation(long milisElapsed) {
            overscrollX=(int) (overscrollX+milisElapsed*dX);
            overscrollY=(int) (overscrollY+milisElapsed*dY);

            elapsed+=milisElapsed;

            if (elapsed>=duration) {
                abort();
                releaseOverscroll();
            }
        }
    }
}
