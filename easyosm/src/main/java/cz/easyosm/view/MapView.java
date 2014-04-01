package cz.easyosm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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
import java.util.Map;

import cz.easyosm.animation.MapAnimation;
import cz.easyosm.animation.MapChoreographer;
import cz.easyosm.tile.MapTile;
import cz.easyosm.tile.OfflineTileProvider;
import cz.easyosm.tile.OnlineTileProvider;
import cz.easyosm.tile.TileMath;
import cz.easyosm.tile.TileProviderBase;
import cz.easyosm.util.GeoPoint;
import cz.easyosm.util.MyMath;

/**
 * Created by martinjr on 3/24/14.
 */
public class MapView extends View {
    private static final int FRAMERATE=10;

    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;
    private MapChoreographer choreographer;

    private TileProviderBase tileProvider;

    private int x=0, y=0;
    private float zoomLevel=10;

    private boolean isScaling=false;

    private Paint textPaint, testPaint;

    private Point focus;

    private Handler animationHandler;

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

        testPaint=new Paint();
        testPaint.setColor(0xaa00FFFF);

        textPaint=new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLACK);

        focus=new Point();

        scaleGestureDetector = new ScaleGestureDetector(getContext(), scaleGestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        choreographer=new MapChoreographer(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTiles(canvas);

        canvas.drawText(String.format("x: %d; y: %d; z: %.3f", x, y, zoomLevel), 10, 40, textPaint);

        if (isScaling) canvas.drawCircle(focus.x, focus.y, 50, testPaint);
    }

    private void drawTiles(Canvas canvas) {
        Point tl=TileMath.PixelXYToTileXY(x, y, zoomLevel, null);
        Point br=TileMath.PixelXYToTileXY(x+canvas.getWidth(), y+canvas.getHeight(), zoomLevel, null);
        Drawable d;

        double tileSize=TileMath.tileSize(256, zoomLevel);

        MapTile curr;

        for (int i=tl.y; i<=br.y; i++) {
            for (int j=tl.x; j<=br.x; j++) {
                curr=new MapTile(j, i, (int) zoomLevel);

                d=tileProvider.getTile(curr);

                d.setBounds(MyMath.ceil(j*tileSize)-x, MyMath.ceil(i*tileSize)-y,
                        MyMath.ceil((j+1)*tileSize)-x, MyMath.ceil((i+1)*tileSize)-y);
                d.draw(canvas);
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

    public void setTileFile(File f) {
        tileProvider=new OfflineTileProvider(f);
    }

    public void setTileURL(String url) {
        tileProvider=new OnlineTileProvider();
    }

    public void setZoomLevel(float newZoom) {
        setZoomLevelFixing(newZoom, getWidth()/2, getHeight()/2);
    }

    public void setZoomLevelFixing(float newZoom, int fixX, int fixY) {
        double dz=MyMath.pow2(newZoom-zoomLevel);

        zoomLevel=newZoom;
        x=(int) ((x+fixX)*dz-fixX);
        y=(int) ((y+fixY)*dz-fixY);

        postInvalidate();
    }


    public void setZoomLimits(int minZoomLevel, int maxZoomLevel, int minDataLevel, int maxDataLevel) {
        tileProvider.setZoomLimits(minZoomLevel, maxZoomLevel);
        tileProvider.setDataLimits(minDataLevel, maxDataLevel);
    }

    public void setViewCenter(GeoPoint point) {
        Point newCenter=TileMath.LatLongToPixelXY(point.getLat(), point.getLon(), zoomLevel, null);

        x=newCenter.x-getWidth()/2;
        y=newCenter.y-getHeight()/2;

        postInvalidate();
    }

    private void runAnimations() {
        animationHandler.postDelayed(redraw, FRAMERATE);
    }

    private void applyAnimation() {
        choreographer.applyTransformations();
        invalidate();
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
                private int elapsed=0;

                @Override
                public void applyTransformation(long milisElapsed) {
                    Log.d("iPass", "apply: elapsed "+milisElapsed);
                    setZoomLevel(zoomLevel+0.05f);
                    elapsed+=milisElapsed;
                    if (elapsed>1000) abort();
                }
            });
            runAnimations();

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

    private Runnable redraw=new Runnable() {
        @Override
        public void run() {
            applyAnimation();

            if (choreographer.isAnimating()) {
                Log.d("iPass", "Schedule redraw");
                animationHandler.postDelayed(redraw, FRAMERATE);
            }
        }
    };
}
