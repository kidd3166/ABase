package com.ouj.library.push;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class PushService extends Service {

    public static final String ACTION_START = "com.iojia.push.PushService.start";
    public static final String ACTION_STOP = "com.iojia.push.PushService.stop";

    private PushClient pushClient;

    protected abstract PushClient newClient();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(networkReceiver, filter);
        if (pushClient == null)
            pushClient = newClient();
        pushClient.connect(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null)
            unregisterReceiver(networkReceiver);
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_STOP)) {
                if (pushClient != null) {
                    pushClient.stop(getApplicationContext());
                }
                pushClient = null;
                stopSelf();
            } else {
                if (pushClient == null)
                    pushClient = newClient();
                pushClient.connect(getApplicationContext());
            }
        } else {
            if (pushClient == null)
                pushClient = newClient();
            pushClient.connect(getApplicationContext());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (pushClient == null)
                pushClient = newClient();
            pushClient.connect(getApplicationContext());
        }
    };
}
