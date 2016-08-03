package com.ouj.library.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.ouj.library.BaseApplication;

import java.io.File;
import java.util.Map;

//@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class KLWebView extends WebView {
    private long oldTime = 0;
    private final long OutTime = 250;
    public Handler m_handler;
    private KLPluginManager manager;
    private KLWebViewClient mWebViewClient;
    private KLWebChromeClient mWebChromeClient;
    public boolean isEmbedInViewPage = false;
    private boolean mIsDestroyed = false;
    private boolean mIsStoped = false;

    public OnPageListener mOnPageListener;
    
    public int scrollToBottomEventId = 0;
    //public boolean isBusy = false;
    
    public KLWebView(Context context) {
        this(context, null);
    }

    public KLWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSetting();
        manager = new KLPluginManager(this);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void initSetting() {
        //setBackgroundColor(0);
        
        mWebViewClient = new KLWebViewClient();
        setWebViewClient(mWebViewClient);

        mWebChromeClient = new KLWebChromeClient();
        setWebChromeClient(mWebChromeClient);

        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setInitialScale(0);
        setVerticalScrollBarEnabled(true);
        requestFocusFromTouch();

        WebSettings settings = super.getSettings();
    	settings.setJavaScriptEnabled(true);
    	
    	// 设置数据库路径，这样才能使用localStorage
    	settings.setDomStorageEnabled(true);
    	settings.setDatabaseEnabled(true);
    	File file = BaseApplication.app.getDatabasePath("database");
    	String databasePath = file.getParent();
    	settings.setDatabasePath(databasePath);
    	
    	settings.setGeolocationEnabled(true);
    	settings.setAllowFileAccess(true);
    	
    	if (VERSION.SDK_INT > 7) {
    		settings.setPluginState(PluginState.ON);
    	}
    	
    	settings.setAllowFileAccess(true);
    	// settings.setAllowContentAccess(true);
    	// settings.setAllowUniversalAccessFromFileURLs(true);
    

        addJavascriptInterface(new KLPlugin(this), "KLPlugin");
/*
        // 设置长按事件
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SystemUtils.d(GlobalData.TAG, "trigerLongTap");
                loadUrl("javascript:BDY.trigerLongTap();");
                return true;
            }
        });
        */
    }
    
    public void loadUrl(String url) {
        if (!mIsDestroyed) {
            addJavascriptInterface();
            super.loadUrl(url);
        }
    }

    public KLPlugin getPlugin(String name) {
        return manager.getPlugin(name);
    }
    
    /**
     * 允许webview放大缩小
     */
    public void setZoomEnabled() {
        setOnTouchListener(new ZoomTouchListener(this));
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        onScrollToBottom();
    }

    private void onScrollToBottom() {
        if (scrollToBottomEventId > 0) {
            @SuppressWarnings("deprecation")
            float span = (getContentHeight() * getScale() - (getHeight() + getScrollY()));
            if (span <= 1) {
                // 如果当前滑动到页面底部
                long curTime = System.currentTimeMillis();
                long timeSpan = curTime - oldTime;
                if (timeSpan > OutTime) {
                    oldTime = curTime;
                    KLPluginResult result = new KLPluginResult(this);
                    result.setRequestId(scrollToBottomEventId);
                    result.sendToJavaScript();
                }
            }
        }
    }
    
/*
    public static final String SCROLL_ACTION_UP = "com.kalagame.klwebview.SCROLL_ACTION_UP";
    public static final String SCROLL_ACTION_DOWN = "com.kalagame.klwebview.SCROLL_ACTION_DOWN";
    private static int SCROLL_OFFSET = 50;
    private int mLastScroll;
    private boolean status = true;
*/
    /**
     * 修复JellyBean and ViewPager + fragments that contains
     * WebViews的情况下，touchend事件失效的问题
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEmbedInViewPage) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int temp_ScrollY = getScrollY();
                scrollTo(getScrollX(), getScrollY() + 1);
                scrollTo(getScrollX(), temp_ScrollY);
            }
        }
/*
        // 传递滑动方向到外部。
        Intent intent = new Intent();
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if ((getScrollY() - mLastScroll) > SCROLL_OFFSET && status) {
                Log.i(TAG, "hide");
                status = false;
                intent.setAction(SCROLL_ACTION_DOWN);
                mContext.sendBroadcast(intent);
            }
            if ((mLastScroll - getScrollY()) > SCROLL_OFFSET && !status) {
                Log.i(TAG, "show");
                status = true;
                intent.setAction(SCROLL_ACTION_UP);
                mContext.sendBroadcast(intent);
            }
            mLastScroll = getScrollY();
            break;
        case MotionEvent.ACTION_UP:
            break;
        default:
            break;
        }
*/
        
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            goBack();
            return true;
        }
        
        return super.onKeyUp(keyCode, event);
    }

    public void execPageEvent(String eventName, String param) {
        String curPage = PageUtil.getPage(getUrl());
        if (curPage != null && curPage.length() > 0) {
            // 重新初始化页面，然后显示页面
            String path = "../" + curPage.replace("-", "/js/") + ".js";
            String js = String.format("javascript:seajs.use('%s', function(page) { page && page.%s && page.%s(%s); });", path,
                    eventName, eventName, param);
            loadUrl(js);
        }
    }

    public String getCurPage() {
        return PageUtil.getPage(getUrl());
    }
    
    public void setOnPageListener(OnPageListener listener) {
        mOnPageListener = listener;
    }
    
    public interface OnPageListener {
        void onPageStarted(WebView webView, String url);
        void onPageFinished(WebView webView, String url);
        void onPageReady(WebView webView, String url);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        manager.onCreate(savedInstanceState);
    }
    
    public void onStart() {
        if (mIsStoped) {
            execPageEvent("init", ""); 
            mIsStoped = false;          
        }
        
        manager.onStart();
    }
    
    @SuppressLint("NewApi")
    public void onPause() {
        mIsStoped = true;
        if (VERSION.SDK_INT >= 11) {
            super.onPause();            
        }
        
        manager.onPause();
    }
    
    @SuppressLint("NewApi")
    public void onResume() {
        if (VERSION.SDK_INT >= 11) {
            super.onResume();            
        }
        
        manager.onResume();
    }

    public void onStop() {
        mIsStoped = true;
        execPageEvent("onStop", "");
        manager.onStop();
    }
    
    public void onDestroy() {
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeAllViews();

        removeAllViews();
        
        manager.onDestroy();
        destroy();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        manager.onActivityResult(requestCode, resultCode, intent);
    }
    
    public void onSaveInstanceState(Bundle outState) {
        manager.onSaveInstanceState(outState);
    }
    
    @Override
    public void destroy() {
        mIsDestroyed = true;
        if (manager != null) {
            manager.onDestroy();
        }
        if (mWebChromeClient != null) {
            mWebChromeClient.onDestroy();
        }
        
        ViewParent parent = getParent();
        if (parent != null) {
            if (parent instanceof LinearLayout) {
                ((LinearLayout) parent).removeView(this);
            } else if (parent instanceof RelativeLayout) {
                ((RelativeLayout) parent).removeView(this);
            } else if (parent instanceof FrameLayout) {
                ((FrameLayout) parent).removeView(this);
            } else if (parent instanceof ScrollView) {
                ((ScrollView) parent).removeView(this);
            }
        }
        
        removeAllViews();
        super.destroy();
    }

    public boolean isDestroyed() {
        return mIsDestroyed;
    }

    @SuppressWarnings("unused")
    public boolean isVideoFullscreen()
    {
        return videoEnabledWebChromeClient != null && videoEnabledWebChromeClient.isVideoFullscreen();
    }

    /**
     * Pass only a VideoEnabledWebChromeClient instance.
     */
    @Override @SuppressLint("SetJavaScriptEnabled")
    public void setWebChromeClient(WebChromeClient client)
    {
        getSettings().setJavaScriptEnabled(true);

        if (client instanceof KLWebChromeClient)
        {
            this.videoEnabledWebChromeClient = (KLWebChromeClient) client;
        }

        super.setWebChromeClient(client);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding)
    {
        addJavascriptInterface();
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl)
    {
        addJavascriptInterface();
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders)
    {
        addJavascriptInterface();
        super.loadUrl(url, additionalHttpHeaders);
    }

    private void addJavascriptInterface()
    {
        if (!addedJavascriptInterface)
        {
            // Add javascript interface to be called when the video ends (must be done before page load)
            //noinspection all
            addJavascriptInterface(new JavascriptInterface(), "_VideoEnabledWebView"); // Must match Javascript interface name of VideoEnabledWebChromeClient

            addedJavascriptInterface = true;
        }
    }

    public class JavascriptInterface
    {
        @android.webkit.JavascriptInterface @SuppressWarnings("unused")
        public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("___", "GOT IT");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (videoEnabledWebChromeClient != null)
                    {
                        videoEnabledWebChromeClient.onHideCustomView();
                    }
                }
            });
        }
    }

    private KLWebChromeClient videoEnabledWebChromeClient;
    private boolean addedJavascriptInterface;
}
