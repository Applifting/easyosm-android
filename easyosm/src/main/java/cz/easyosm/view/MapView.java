package cz.easyosm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import java.io.File;

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
    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;

    private TileProviderBase tileProvider;

    private int x=0, y=0;
    private float zoomLevel=10;

    private boolean isScaling=false;

    private Paint textPaint, testPaint;

    private int bgcolor=0xff00ff00;

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

        testPaint=new Paint();
        testPaint.setColor(Color.BLACK);

        textPaint=new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLACK);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), scaleGestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgcolor);

        drawTiles(canvas);

        canvas.drawText(String.format("x: %d; y: %d; z: %.3f", x, y, zoomLevel), 10, 40, textPaint);
    }

    private void drawTiles(Canvas canvas) {
        Point tl=TileMath.PixelXYToTileXY(x, y, zoomLevel, null);
        Point br=TileMath.PixelXYToTileXY(x+canvas.getWidth(), y+canvas.getHeight(), zoomLevel, null);
        Point tileTL=new Point();
        Drawable d;

        MapTile curr;

        for (int i=tl.y; i<=br.y; i++) {
            for (int j=tl.x; j<=br.x; j++) {
                curr=new MapTile(j, i, (int) zoomLevel);

                d=tileProvider.getTile(curr);
                tileTL=TileMath.TileXYToPixelXY(j, i, zoomLevel, tileTL);
                d.setBounds(tileTL.x-x, tileTL.y-y,
                        (int)(tileTL.x-x+MyMath.tileSize(256, zoomLevel)+1),
                        (int)(tileTL.y-y+MyMath.tileSize(256, zoomLevel)+1)
                    );
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
        double dz=MyMath.pow2(zoomLevel-newZoom);
        double dx=getWidth()/2;
        double dy=getHeight()/2;

        zoomLevel=newZoom;
        x=(int) ((x+dx)*dz-dx);
        y=(int) ((y+dy)*dz-dy);

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

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector sgd) {
            bgcolor=0xff0000ff;
            isScaling=true;
            postInvalidate();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector sgd) {
            Log.d("iPass", "onScale");
            double dz=sgd.getScaleFactor();
            double dx=sgd.getFocusX();
            double dy=sgd.getFocusY();

            zoomLevel+=MyMath.log2(dz);
            x=(int) ((x+dx)*dz-dx);
            y=(int) ((y+dy)*dz-dy);

            postInvalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector sgd) {
            bgcolor=0xff00ff00;
            isScaling=false;
            postInvalidate();
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
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("iPass", "onScroll");
            if (isScaling) return false;
            x+=distanceX;
            y+=distanceY;
            postInvalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling(x, y, (int) -velocityX, (int) -velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            return true;
        }
    };
}
