package com.ouj.library.net;

import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.ouj.library.net.body.GzipResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Created by liqi on 2016-1-30.
 */
public class OKHttp {

    private static boolean DEBUG = true;
    private static OkHttpClient client = null;
    private int cacheType = CacheType.NETWORK_ELSE_CACHED;
    private String tag;
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .header("Cache-Control", "max-age=2419200")
                    .build();
        }
    };
    private static Handler mainHandler = null;

    public static void init(Context context, List<Interceptor> netWorkinterceptors, List<Interceptor> interceptors, boolean isGzip, long timeout, int cacheSize) {
        mainHandler = new Handler(context.getMainLooper());

        int totalCacheSize = cacheSize * 1024 * 1024;
        Cache cache = new Cache(new File(context.getCacheDir(), "okhttp"), totalCacheSize);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache).connectTimeout(timeout, TimeUnit.MILLISECONDS).readTimeout(timeout, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR);
        if (DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        if (netWorkinterceptors != null && !netWorkinterceptors.isEmpty()) {
            builder.networkInterceptors().addAll(netWorkinterceptors);
        }
        if (interceptors != null && !interceptors.isEmpty()) {
            builder.interceptors().addAll(interceptors);
        }
        if (isGzip) {
            builder.addInterceptor(new GzipRequestInterceptor());
        }
        client = builder.build();
    }

    public static OkHttpClient getClient() {
        return client;
    }

    public static void cancelTag(Object o) {
        if (o == null)
            return;
        if (client == null)
            return;
        String tag = o.toString();
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    private OKHttp(int cacheType, String tag) {
        this.cacheType = cacheType;
        this.tag = tag;
    }

    public Call enqueue(final Request request, final ResponseCallback callback) {
        if (client == null)
            return null;
        Call call = null;
        switch (cacheType) {
            case CacheType.ONLY_CACHED:
                call = client.newCall(request.newBuilder().tag(tag).cacheControl(CacheControl.FORCE_CACHE).build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, final IOException e) {
                        if (mainHandler != null)
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(call, e);
                                    callback.onFinish();
                                }
                            });
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException {
                        if (mainHandler != null)
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callback.onResponse(call, response);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    callback.onFinish();
                                }
                            });
                    }
                });
                break;
            case CacheType.ONLY_NETWORK:
                callback.onStart();
                call = client.newCall(request.newBuilder().tag(tag).cacheControl(CacheControl.FORCE_NETWORK).build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, final IOException e) {
                        if (mainHandler != null)
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onFailure(call, e);
                                    callback.onFinish();
                                }
                            });
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException {
                        if (mainHandler != null)
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callback.onResponse(call, response);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    callback.onFinish();
                                }
                            });
                    }
                });
                break;
            case CacheType.CACHED_ELSE_NETWORK:
                callback.onStart();
                call = client.newCall(request.newBuilder().tag(tag).cacheControl(CacheControl.FORCE_CACHE).build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        enqueueRequest(request, CacheControl.FORCE_NETWORK, callback);
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException {
                        if (response.isSuccessful()) {
                            if (mainHandler != null)
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            callback.onResponse(call, response);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        callback.onFinish();
                                    }
                                });
                        } else {
                            enqueueRequest(request, CacheControl.FORCE_NETWORK, callback);
                        }
                    }
                });
                break;
            case CacheType.NETWORK_ELSE_CACHED:
                callback.onStart();
                call = client.newCall(request.newBuilder().tag(tag).cacheControl(CacheControl.FORCE_NETWORK).build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        enqueueRequest(request, CacheControl.FORCE_CACHE, callback);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            callback.onResponse(call, response);
                            callback.onFinish();
                        } else {
                            enqueueRequest(request, CacheControl.FORCE_CACHE, callback);
                        }
                    }
                });
                break;
            case CacheType.CACHED_AND_NETWORK:
                callback.onStart();
                call = client.newCall(request.newBuilder().tag(tag).cacheControl(CacheControl.FORCE_CACHE).build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        enqueueRequest(request, CacheControl.FORCE_NETWORK, callback);
                    }

                    @Override
                    public void onResponse(final Call call, final Response cacheResponse) throws IOException {
                        if (cacheResponse.isSuccessful()) {
                            if (mainHandler != null)
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            callback.onResponse(call, cacheResponse);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        callback.onFinish();
                                    }
                                });
                            //  Log.i("TEST", "cacheResponse: " + cacheResponse.body().string());
                            client.newCall(request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build()).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    if (mainHandler != null)
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onFinish();
                                            }
                                        });
                                }

                                @Override
                                public void onResponse(final Call call, final Response networkResponse) throws IOException {
                                    if (mainHandler != null)
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (networkResponse.isSuccessful()) {
                                                        callback.onResponse(call, networkResponse);
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                callback.onFinish();
                                            }
                                        });
                                }
                            });
                        } else {
                            enqueueRequest(request, CacheControl.FORCE_NETWORK, callback);
                        }
                    }
                });
                break;
        }
        return call;
    }

    public Response execute(Request request) {
        if (client == null)
            return null;
        Response responseCache, responseNetwork;
        try {
            switch (cacheType) {
                case CacheType.ONLY_CACHED:
                    return executeCacheRequest(request);
                case CacheType.ONLY_NETWORK:
                    return executeNetworkRequest(request);
                case CacheType.CACHED_ELSE_NETWORK:
                    responseCache = executeCacheRequest(request);
                    if (responseCache.isSuccessful()) {
                        return responseCache;
                    }
                    responseNetwork = executeNetworkRequest(request);
                    return responseNetwork;
                case CacheType.NETWORK_ELSE_CACHED:
                case CacheType.CACHED_AND_NETWORK:
                    responseNetwork = executeNetworkRequest(request);
                    if (responseNetwork.isSuccessful()) {
                        return responseNetwork;
                    }
                    responseCache = executeCacheRequest(request);
                    return responseCache;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T execute(Request request, Class classType) {
        if (client == null)
            return null;
        Response response = execute(request);
        if (response != null && response.isSuccessful()) {
            try {
                return (T) JSON.parseObject(response.body().string(), classType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private Response executeCacheRequest(Request request) throws IOException {
        Response response = client.newCall(request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()).execute();
        return response;
    }

    private Response executeNetworkRequest(Request request) throws IOException {
        Response response = client.newCall(request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build()).execute();
        return response;
    }

    private void enqueueRequest(Request request, CacheControl cacheControl, final ResponseCallback callback) {
        client.newCall(request.newBuilder().cacheControl(cacheControl).build()).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (mainHandler != null)
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(call, e);
                            callback.onFinish();
                        }
                    });
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (mainHandler != null)
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callback.onResponse(call, response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            callback.onFinish();
                        }
                    });
            }
        });
    }

    public static interface CacheType {
        int ONLY_NETWORK = 0;
        int ONLY_CACHED = 1;
        int CACHED_ELSE_NETWORK = 2;
        int NETWORK_ELSE_CACHED = 3;
        int CACHED_AND_NETWORK = 4;
    }

    static class GzipRequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
//            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
//                return chain.proceed(originalRequest);
//            }

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Accept-Encoding", "gzip")
//                    .header("Content-Encoding", "gzip")
//                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            Response response = chain.proceed(compressedRequest);
            if(response.header("Content-Encoding") != null && response.header("Content-Encoding").contains("gzip")){
                return response.newBuilder().body(new GzipResponseBody(response.body())).build();
            }
            return response;
//            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }

    public static class Builder {

        private int cacheType = CacheType.NETWORK_ELSE_CACHED;
        private String tag;

        public Builder(Object tag) {
            if (tag != null) {
                this.tag = tag.toString();
            }
        }

        public OKHttp build() {
            return new OKHttp(cacheType, tag);
        }

        public Builder cacheType(int cacheType) {
            this.cacheType = cacheType;
            return this;
        }

    }
}
