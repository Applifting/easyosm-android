package cz.easyosm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import cz.easyosm.util.MyMath;

/**
 * Created by martinjr on 3/24/14.
 */
public class MapView extends View {
    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;

    private int x=0, y=0;
    private float zoomLevel=1;

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
        testPaint.setColor(0xff000000);

        textPaint=new Paint();
        textPaint.setTextSize(30);
        textPaint.setColor(0xffffffff);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), scaleGestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgcolor);

        canvas.drawCircle(drawX((int) (128*MyMath.pow2(zoomLevel))), drawY((int) (128*MyMath.pow2(zoomLevel))), (float) (50*MyMath.pow2(zoomLevel)), testPaint);

        canvas.drawText(String.format("x: %.3f; y: %.3f", x/MyMath.pow2(zoomLevel), y/MyMath.pow2(zoomLevel)), 10, 40, textPaint);
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

    private int drawX(int x) {
        return x-this.x;
    }

    private int drawY(int y) {
        return y-this.y;
    }

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector sgd) {
            bgcolor=0xff0000ff;
            postInvalidate();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector sgd) {
            float dz=sgd.getScaleFactor();
            float dx=sgd.getFocusX();
            float dy=sgd.getFocusY();

            Log.d("iPass", "Zoom by "+dz+" on "+dx+", "+dy);

            zoomLevel+=MyMath.log2(dz);
            x=(int) ((x+dx)*dz-dx);
            y=(int) ((y+dy)*dz-dy);

            postInvalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector sgd) {
            bgcolor=0xff00ff00;
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
            x+=distanceX;
            y+=distanceY;
            postInvalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling(x, y, (int) -velocityX, (int) -velocityY, -1000000, 1000000, -1000000, 1000000);
            return true;
        }
    };
}
