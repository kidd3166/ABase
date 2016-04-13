package com.ouj.library.webview;

import android.annotation.SuppressLint;

public class KLPluginResult {
    private int mRequestId;
    private ResultStatus mResultStatus;
    private String mResult;
    private KLWebView mWebview;
    
    public KLPluginResult(KLWebView webview) {
        mWebview = webview;
    }
    
    public KLPluginResult(KLWebView webview, ResultStatus status, String result) {
        mResultStatus = status;
        mResult = result;
        mWebview = webview;
    }
    
    public void setStatus(ResultStatus status) {
        mResultStatus = status;
    }
    
    public void setResult(String result) {
        mResult = result;
    }
            
    public void setRequestId(int requestId) {
        mRequestId = requestId;
    }
    
    @SuppressLint("DefaultLocale")
    public void sendToJavaScript() {
        if (mResultStatus == null) {
            mResultStatus = ResultStatus.SUCCES;
        }
        
        String js = String.format("javascript:KLPlugin.%s[%d] && KLPlugin.%s[%d](%s); ", mResultStatus.val, mRequestId, mResultStatus.val, mRequestId, mResult);
        if (mWebview != null && !mWebview.isDestroyed()) {
            mWebview.loadUrl(js);
        }
    }

}


