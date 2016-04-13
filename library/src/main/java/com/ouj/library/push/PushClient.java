package com.ouj.library.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ouj.library.BaseApplication;
import com.ouj.library.util.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;


/**
 * Created by liqi on 2015-10-12.
 */
public abstract class PushClient implements WebSocketListener {
    protected static final String TAG = "Push";

    private int ping;
    private boolean connecting;

    private OkHttpClient client;
    private WebSocket mConnection;
    private ScheduledExecutorService scheduledExecutorService;
    private final Executor writeExecutor = Executors.newSingleThreadExecutor();

    public PushClient() {
    }

    /**
     * 接收服务器的内容
     *
     * @param context
     * @param payload
     */
    protected abstract void onTextMessage(Context context, String payload);

    /**
     * 连接服务器地址
     *
     * @param context
     * @return
     */
    protected abstract String serverURL(Context context);

    /**
     * 与推送服务器连接后再与业务服务器关联uid,token等信息
     *
     * @param context
     * @param type
     */
    protected abstract void onBind(Context context, String type);

    public WebSocket getWebSocket() {
        if (isConnected())
            return mConnection;
        return null;
    }

    public void sendMessage(final String message) {
        if (TextUtils.isEmpty(message))
            return;
        if (mConnection == null)
            return;
        writeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnection.sendMessage(RequestBody.create(okhttp3.ws.WebSocket.TEXT, message));
                } catch (IOException e) {
                    log("Unable to send messages: " + e.getMessage());
                }
            }
        });
    }

    public boolean isConnected() {
        return mConnection != null;
    }

    public synchronized void connect(final Context context) {

        if (TextUtils.isEmpty(Push.getPushId(context))) {
            return;
        }

        final Context applicationContext = context.getApplicationContext();

        log("Status: Connecting... is connecting " + connecting + " " + this);

        if (connecting)
            return;

        log("Status: Connecting... connection is " + isConnected() + " " + this);
        if (isConnected())
            return;

        boolean connectAvailable = NetworkUtils.isAvailable();
        log("Status: Connecting... network available " + connectAvailable + " " + this);
        if (connectAvailable) {
            final String wsuri = serverURL(applicationContext);
            if (wsuri == null)
                return;

            if (!Push.isEnable(applicationContext)) {
                return;
            }

            log("Status: Connecting... to " + wsuri + " ..");

            connecting = true;
            stopHeartbeat();

            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    heartbeat(BaseApplication.app);
                }
            }, 10000, 58000, TimeUnit.MILLISECONDS);

            client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(70, TimeUnit.SECONDS).writeTimeout(70, TimeUnit.SECONDS).build();

            Request request = new Request.Builder()
                    .url(wsuri)
                    .build();

            WebSocketCall.create(client, request).enqueue(this);
        }
    }

    public void stop(Context context) {
        stopHeartbeat();
        mConnection = null;
        if (client != null)
            client.dispatcher().executorService().shutdown();
    }

    protected void onClose(Context context, int code, String reason) {
        stop(context);
    }

    private void heartbeat(Context context) {
        if (isConnected()) {
            try {
                mConnection.sendMessage(RequestBody.create(okhttp3.ws.WebSocket.TEXT, "p"));
                ping++;
                if (ping >= 3) {
                    ping = 0;
                    mConnection.close(-999, "ping max");
                }
            } catch (Throwable e) {
                try {
                    mConnection.close(-998, e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                ping = 0;
            }
            try {
                if (!isConnected())
                    connect(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            connect(context);
        }
        log(this + " heartbeat " + isConnected() + " " + System.currentTimeMillis());
    }

    private void stopHeartbeat() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    private void log(String log) {
        if (Push.DEBUG) {
            Log.i(TAG, log);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        ping = 0;
        connecting = false;
        this.mConnection = webSocket;
        Context context = BaseApplication.app;
        log("Status: Connected " + serverURL(context));
        onBind(context, "bind");
    }

    @Override
    public void onFailure(IOException e, Response response) {
        ping = 0;
        connecting = false;
        mConnection = null;
        log(String.format("Status: Connection lost (%d, %s)  ", -1000, e.getMessage()) + (response != null ? response.message() : ""));
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        ping = 0;
        if (message != null) {
            String payload = null;
            if (message.contentType() == okhttp3.ws.WebSocket.TEXT) {
                payload = message.string();
            } else {
                payload = message.source().readByteString().hex();
            }
            if (payload != null) {
                log("Status: onTextMessage: " + payload);
                Context context = BaseApplication.app;
                PushClient.this.onTextMessage(context, payload);
            }
        }

    }

    @Override
    public void onPong(Buffer payload) {
        heartbeat(BaseApplication.app);
    }

    @Override
    public void onClose(int code, String reason) {
        mConnection = null;
        ping = 0;
        connecting = false;
        log(String.format("Status: Connection lost (%d, %s)  ", code, reason));
        PushClient.this.onClose(BaseApplication.app, code, reason);
    }
}
