package com.ouj.library.webview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ouj.library.BaseApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KLWebViewClient extends WebViewClient {
    public String url;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.contains("http://") || url.contains("https://")) {
            if (!url.endsWith(".apk") && !url.endsWith(".pdf") && !url.endsWith(".doc") && !url.endsWith(".ipa")) {
                view.loadUrl(url);
            } else {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                BaseApplication.app.startActivity(intent);
            }
        }

        return super.shouldOverrideUrlLoading(view, url);
    }


    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        // SystemUtils.d(GlobalData.TAG, "onPageStarted, url:" + url);
        this.url = url;
        /*
        //初始化js
        String js = String.format("javascript:BDY.uuid = '%s';", GlobalData.getUuid());
        js += "BDY.network = '%s';";
        view.loadUrl(js);
        */
        KLWebView webview = (KLWebView) view;
        if (webview.mOnPageListener != null) {
            webview.mOnPageListener.onPageStarted(view, url);
        }
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        KLWebView webview = (KLWebView) view;
        if (webview.mOnPageListener != null) {
            webview.mOnPageListener.onPageFinished(view, url);
        }

        // webview.isBusy = false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.indexOf("file:///android_asset") == 0 && (url.contains("#") || url.contains("?"))) {
            Pattern p = Pattern.compile("file:///android_asset/([^?#]*)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String filePath = m.group(1);
                try {
                    AssetManager am = BaseApplication.app.getAssets();
                    InputStream is = am.open(filePath);
                    if (filePath.matches(".*\\.html")) {
                        return new WebResourceResponse("text/html", "UTF-8", is);
                    } else if (filePath.matches(".*\\.css")) {
                        return new WebResourceResponse("text/css", "UTF-8", is);
                    } else if (filePath.matches(".*\\.js")) {
                        return new WebResourceResponse("text/javascript", "UTF-8", is);
                    }
                } catch (IOException e) {
                    return null;
                }
            }
        }

        return null;

    }

}
