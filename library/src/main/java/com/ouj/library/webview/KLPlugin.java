package com.ouj.library.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.webkit.JavascriptInterface;

import com.ouj.library.BaseApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

//import android.webkit.JavascriptInterface;

public class KLPlugin {
    public final static String TAG = "KLPlugin";
    public static int mCurRequestId;

    protected KLWebView mWebview;
    protected Context mContext;
    protected Activity mActivity;

    public KLPlugin(KLWebView webview) {
        if (webview != null) {
            mContext = webview.getContext();
            mWebview = webview;
            if (mContext instanceof Activity) {
                mActivity = (Activity) mContext;
            }

        }

        if (mContext == null) {
            mContext = BaseApplication.app;
        }
        
        onCreate(null);
    }

    public KLPlugin(Context ctx) {
        mContext = ctx;
        if (mContext instanceof Activity) {
            mActivity = (Activity) mContext;
        }
        onCreate(null);
    }

    Handler handler = new Handler(Looper.getMainLooper(), new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String[] args = (String[]) msg.obj;

            // 反射调用消息的方法
            int i = 0;
            String strRequestId = args[i++];
            if (strRequestId != null) {
                mCurRequestId = Integer.parseInt(strRequestId);
            }

            String className = args[i++];
            String funcName = args[i++];
            String strParams = args[i++];

            String[] params = null;
            Class<?>[] paramClasses;
            if (strParams != null) {
                JSONArray arr;
                try {
                    arr = new JSONArray(strParams);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }

                int len = arr.length();
                params = new String[len];
                paramClasses = new Class[len];
                for (int j = 0; j < len; j++) {
                    paramClasses[j] = String.class;
                    params[j] = arr.optString(j);
                }
            } else {
                paramClasses = new Class[0];
            }

            try {
                KLPlugin plugin = mWebview.getPlugin(className);
                Method method = plugin.getClass().getMethod(funcName, paramClasses);

                // KLPluginResult result = (KLPluginResult)
                // method.invoke(plugin, (Object[]) params);
                Object objResult = method.invoke(plugin, (Object[]) params);
                if (objResult != null) {
                    KLPluginResult result = null;
                    if (objResult instanceof KLPluginResult) {
                        result = (KLPluginResult) objResult;
                    } else {
                        result = new KLPluginResult(mWebview);
                        result.setStatus(ResultStatus.SUCCES);
                        if (objResult instanceof String) {
                            result.setResult(JSONObject.quote(objResult.toString()));
                        } else {
                            result.setResult(objResult.toString());
                        }
                    }

                    // 有requestId的时候才需要回调给js
                    if (result != null) {
                        result.setRequestId(mCurRequestId);
                        result.sendToJavaScript();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    });

    @JavascriptInterface
    public void exec(String requestId, String className, String func, String params) {
        Message msg = new Message();
        String[] args = { requestId, className, func, params };
        msg.obj = args;
        handler.sendMessage(msg);
    }

    protected void error(Object msg) {
        error(mCurRequestId, msg);
    }

    protected void error(int requestId, Object msg) {
        if (msg instanceof String) {
            result(requestId, ResultStatus.ERROR, "'" + msg + "'");
        } else {
            result(requestId, ResultStatus.ERROR, msg);
        }
    }

    protected void success(Object msg) {
        success(mCurRequestId, msg);
    }

    protected void success(int requestId, Object... msg) {
    	String js = "";
    	for (int i = 0; i < msg.length; i++) {
            if (msg[i] instanceof String) {
                js += JSONObject.quote(msg[i].toString());
            } else {
            	js += msg[i].toString();
            }
            
            if (i != msg.length - 1) {
            	js += ", ";
            }
    	}
    	
        result(requestId, ResultStatus.SUCCES, js);
    }

    private void result(int requestId, ResultStatus status, Object msg) {
        KLPluginResult result = new KLPluginResult(mWebview, status, msg.toString());
        result.setRequestId(requestId);
        result.sendToJavaScript();
    }
    
    public void run(String className, String funcName) {
        run(className, funcName, new Class[0], new Object[0]);
    }
    
    public void run(String className, String funcName, Class<?>[] paramClasses, Object... params) {
        try {
            Class<?> classObj = Class.forName(className);
            Constructor<?> con = classObj.getConstructor(KLWebView.class);

            Object plugin = con.newInstance(mWebview);
            Method method = classObj.getMethod(funcName, paramClasses);
            method.invoke(plugin, params);
        } catch (Exception e) {
            // 插件初始化失败则跳过
            e.printStackTrace();
        }
    }

    public void onCreate(Bundle savedInstanceState) {

    }
    

    public void onSaveInstanceState(Bundle outState) {
        
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    
    public void onPause() {

    }
    
    public void onResume() {

    }
    
    public void onStart() {

    }

    public void onStop() {

    }
    
    public void onDestroy() {
        mContext = null;
    }

}
