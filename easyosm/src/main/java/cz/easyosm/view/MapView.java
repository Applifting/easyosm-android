package cz.easyosm.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
import java.security.cert.LDAPCertStoreParameters;
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
    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;
    private MapChoreographer choreographer;

    private TileProviderBase tileProvider;
    private Map<MapTile, TileFadeAnimation> fades;
    private List<MapOverlayBase> overlays;

    private int x=0, y=0;
    private float zoomLevel=10;

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

        canvas.drawRect(0f, 0f, (float)getWidth(), 40f, testPaint);
        canvas.drawText(String.format("x: %d; y: %d; z: %.3f", x, y, zoomLevel), 10, 35, textPaint);

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

//                    canvas.drawText(""+curr, tileRect.left+20, tileRect.top+20, textPaint);
                }


//                Paint p=new Paint();
//                p.setColor(Color.BLACK);
//
//                canvas.drawLines(new float[] {
//                        tileRect.left, tileRect.top,
//                        tileRect.right, tileRect.top,
//                        tileRect.right, tileRect.top,
//                        tileRect.right, tileRect.bottom,
//                        tileRect.right, tileRect.bottom,
//                        tileRect.left, tileRect.bottom,
//                        tileRect.left, tileRect.bottom,
//                        tileRect.left, tileRect.top}, p);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.computeScrollOffset()) {
            x=scroller.getCurrX();
            y=scroller.getCurrY();
            postInvalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event) |
            gestureDetector.onTouchEvent(event) |
            super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Callback from another thread - will NOT run on UI thread
     */
    public void onTileLoaded(final MapTile tile, final Drawable original, final Drawable replace) {
        animationHandler.post(new Runnable() {
            @Override
            public void run() {
                //Log.d("iPass", "Run tile fade for "+tile);
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

    public int getOffsetX() {
        return x;
    }

    public int getOffsetY() {
        return y;
    }

    public MapChoreographer getChoreographer() {
        return choreographer;
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
        double dz=MyMath.pow2(newZoom-zoomLevel);

        zoomLevel=newZoom;
        x=(int) ((x+fixX)*dz-fixX);
        y=(int) ((y+fixY)*dz-fixY);

        for (MapListener listener : listeners) {
            listener.onZoom(newZoom);
        }

        postInvalidate();
    }


    public void setZoomLimits(int minZoomLevel, int maxZoomLevel, int minDataLevel, int maxDataLevel) {
        tileProvider.setZoomLimits(minZoomLevel, maxZoomLevel);
        tileProvider.setDataLimits(minDataLevel, maxDataLevel);
    }

    public void setViewCenter(GeoPoint point) {
        Point newCenter=TileMath.LatLongToPixelXY(point.lat, point.lon, zoomLevel, null);

        x=newCenter.x-getWidth()/2;
        y=newCenter.y-getHeight()/2;

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
            setZoomLevel(zoomLevel+0.02f);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            choreographer.runAnimation(new MapAnimation() {
                private float originalZoom=zoomLevel;
                private int duration=500, elapsed=0;

                @Override
                public void applyTransformation(long milisElapsed) {
                    float add=((float)elapsed)/duration;
                    Log.d("iPass", "Add "+add+" to zoomLevel");
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
            x+=distanceX;
            y+=distanceY;
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
}
