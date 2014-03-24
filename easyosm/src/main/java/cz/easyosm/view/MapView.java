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

/**
 * Created by martinjr on 3/24/14.
 */
public class MapView extends View {
    private OverScroller scroller;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat gestureDetector;

    private int x=0, y=0;
    private float zoomLevel=1;

    private Paint textPaint;

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

        textPaint=new Paint();
        textPaint.setTextSize(30);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), scaleGestureListener);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xff00ff00);
        canvas.drawText(String.format("x: %d; y: %d", x, y), 10, 40, textPaint);

        int d=(int) (100*zoomLevel);

        int lx=d-x%d, ly=d-y%d;

        while (lx<canvas.getWidth()) {
            canvas.drawLine(lx, 0, lx, canvas.getHeight(), textPaint);
            lx+=d;
        }


        while (ly<canvas.getHeight()) {
            canvas.drawLine(0, ly, canvas.getWidth(), ly, textPaint);
            ly+=d;
        }

        canvas.drawCircle(drawX((int) (500*zoomLevel)), drawY((int) (500*zoomLevel)), 50*zoomLevel, textPaint);
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
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            bgcolor=0xff0000ff;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            zoomLevel*=scaleGestureDetector.getScaleFactor();

            Log.d("iPass", "Zoom is "+zoomLevel);
            postInvalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            bgcolor=0xff00ff00;
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
