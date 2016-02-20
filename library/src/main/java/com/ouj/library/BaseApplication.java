package com.ouj.library;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ouj.library.event.ActivityEvent;
import com.ouj.library.event.OnForegroundEvent;
import com.ouj.library.util.ApplicationUtils;
import com.ouj.library.util.PackageUtils;
import com.ouj.library.util.SharedPrefUtils;
import com.ouj.library.util.UIUtils;

import de.greenrobot.event.EventBus;


public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String SP_KEY_APPID = "appId";
    public static final String SP_KEY_APPVERSION = "appVersion";
    public static final String SP_KEY_WEBVERSION = "webVersion";
    public static final String SP_KEY_CHANNEL = "channel";
    public static final String SP_KEY_UUID = "uuid";
    public static final String SP_KEY_DEBUG = "debug";
    public static final String SP_KEY_TOKEN = "token";

    public static String APP_ID;
    public static String APP_CHANNEL;
    public static String APP_VERSION;
    public static boolean APP_DEBUG;

    public static Application app;
    private boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private Runnable check;


    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.app = this;
        baseInit(this);
        registerActivityLifecycleCallbacks(this);
    }

    public static void baseInit(Application context) {
        APP_ID = ApplicationUtils.getMetadataString(context, "appid");
        APP_CHANNEL = ApplicationUtils.getMetadataString(context, "UMENG_CHANNEL");
        APP_VERSION = String.valueOf(PackageUtils.getVersionCode(context));
        SharedPrefUtils.put(SP_KEY_APPID, APP_ID);
        String channel = SharedPrefUtils.get(SP_KEY_CHANNEL);
        if (TextUtils.isEmpty(channel)) {
            SharedPrefUtils.put(SP_KEY_CHANNEL, APP_CHANNEL);
        } else {
            APP_CHANNEL = channel;
        }
        String environment = ApplicationUtils.getMetadataString(context, "environment");
        APP_DEBUG = true;
        if (TextUtils.isEmpty(environment))
            APP_DEBUG = false;

        UIUtils.init(context);
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;
        if (check != null && handler != null) {
            handler.removeCallbacks(check);
        }
        if (wasBackground) {
            EventBus.getDefault().post(new OnForegroundEvent());
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;
        if (check != null) {
            handler.removeCallbacks(check);
        }
        handler.postDelayed(check = new Runnable() {
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.d("APP", "App went background");
                    EventBus.getDefault().post(new ActivityEvent(false));
                } else {
                    Log.d("APP", "App still foreground");
                }
            }
        }, 1000);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
