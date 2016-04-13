package com.ouj.library.webview;

import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class KLWebChromeClient extends WebChromeClient {

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return super.onJsBeforeUnload(view, url, message, result);
    }
    
//------------------- 视频相关功能 begin -------------------------------
    
    private CustomViewCallback mCustomViewCallback;
    private View mCustomView;
    private KLCustomViewCallback cvCallback;
    
    public interface KLCustomViewCallback {
        public void onShowCustomView(View view);
        public void onHideCustomView();
    }
    
    public void setCustomViewCallback(KLCustomViewCallback callback) {
        cvCallback = callback;
    }
    
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        
        mCustomView = view;
        mCustomViewCallback = callback;
        
        if (cvCallback != null) {
            cvCallback.onShowCustomView(mCustomView);
        }
    }
    
    public boolean hasShownCustomView() {
        return mCustomView != null;
    }
    
    @Override
    public void onHideCustomView() {
        if (!hasShownCustomView()) {
            return;        
        }
        
        // Hide the custom view.
        mCustomView.setVisibility(View.GONE);
        
        // Remove the custom view from its container.
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        
        if (cvCallback != null) {
            cvCallback.onHideCustomView();
        }
    }
    
//------------------- 视频相关功能 end -------------------------------
    
    public void onDestroy() {
        onHideCustomView();
    }
/*
    @SuppressWarnings("deprecation")
    public void onConsoleMessage(String message, int lineNumber, String sourceID)
    {
        Log.d(GlobalData.TAG, String.format("%s: Line %d : %s", sourceID, lineNumber, message));
        super.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message() != null)
            Log.d(GlobalData.TAG, consoleMessage.message());
        return super.onConsoleMessage(consoleMessage);
    }
*/
}
