package com.ouj.library.webview;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;

public class ZoomTouchListener implements OnTouchListener {
    private WebView mWebview;
    PointF start = new PointF();
    PointF mid = new PointF();
    int mode;
    float oldDist;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    
    public ZoomTouchListener(WebView view) {
        mWebview = view;
    }

    public boolean onTouch(View v, MotionEvent event) {
        // Handle touch events here...
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        // 设置拖拉模式
        case MotionEvent.ACTION_DOWN:
            start.set(event.getX(), event.getY());
            //SystemUtils.d(GlobalData.TAG, "mode=DRAG");
            mode = DRAG;
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            //SystemUtils.d(GlobalData.TAG, "mode=NONE");
            break;
        // 设置多点触摸模式
        case MotionEvent.ACTION_POINTER_DOWN:
            oldDist = spacing(event);
            if (oldDist > 10f) {
                midPoint(mid, event);
                mode = ZOOM;
                //SystemUtils.d(GlobalData.TAG, "mode=ZOOM");
            }
            break;
        // 若为DRAG模式，则点击移动图片
        case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {

                // event.getX() - start.x);
            } else if (mode == ZOOM) {
             // 若为ZOOM模式，则多点触摸缩放
                float newDist = spacing(event);
                //SystemUtils.d(GlobalData.TAG, "newDist=" + newDist);
                if (newDist > 10f) {
                    double scale = newDist / oldDist;
                    if (scale < 1.5 && scale > 0.66) {
                        return false;
                    }
                    
                    oldDist = newDist;
                    boolean flag = scale > 1;
                    //SystemUtils.d(GlobalData.TAG, "onTouch flag:" + flag);
                    
                    mWebview.loadUrl("javascript:BDY.changeFontSize && BDY.changeFontSize(" + flag + ")");
                    return true;
                }
            }
            break;
        }

        return false; // indicate event was handled
    }

    // 计算移动距离
    @SuppressLint("FloatMath")
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    // 计算中点位置
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
