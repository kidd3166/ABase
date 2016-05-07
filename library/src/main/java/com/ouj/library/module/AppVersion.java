package com.ouj.library.module;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ouj.library.BaseApplication;
import com.ouj.library.net.OKHttp;
import com.ouj.library.net.body.ProgressResponseBody;
import com.ouj.library.net.extend.ResponseCallback;
import com.ouj.library.util.ToastUtils;
import com.ouj.library.util.Tool;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by liqi on 2016-2-23.
 */
public class AppVersion implements DialogInterface.OnDismissListener {

    private Activity activity;
    private boolean needLoading;
    private ProgressDialog progressDialog;
    private ProgressDialog downloadProgressDialog;
    private int progress;

    public AppVersion(Activity activity, boolean needLoading) {
        this.activity = activity;
        this.needLoading = needLoading;
    }

    public void destory() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (downloadProgressDialog != null && downloadProgressDialog.isShowing())
            downloadProgressDialog.dismiss();


        activity = null;
        progressDialog = null;
        downloadProgressDialog = null;
    }

    public void checkVersion() {
        RequestBody formBody = new FormBody.Builder()
                .add("appid", BaseApplication.APP_ID)
                .add("version", BaseApplication.APP_VERSION)
                .add("cid", BaseApplication.APP_CHANNEL)
                .add("platform", "android")
                .build();
        Request request = new Request.Builder()
                .url("http://" + (BaseApplication.APP_DEBUG ? "test.base.api.oxzj.net" : "base.api.ouj.com") + "/upgrade/checkVersion.do")
                .post(formBody)
                .build();
        if (needLoading) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("检测新版本...");
            progressDialog.show();
        }

        new OKHttp.Builder(activity).cacheType(OKHttp.CacheType.ONLY_NETWORK).build().enqueue(request, new ResponseCallback<UpdateResponse>() {
            @Override
            public void onResponse(int i, UpdateResponse o) throws Exception {
                updateResult(activity, o);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
            }
        });

    }

    private void updateResult(final Activity activity, final UpdateResponse response) {
        if (response.haveNewVersion > 0) {
            String versionTitle = String.format("发现新版本 v%s", response.versionName);
            String updateContent = response.updateContent;
            final String apkUrl = response.apkUrl;
            final long apkSize = response.apkSize;
            int mustUpdate = response.mustUpdate;
            if (needLoading) {
                Dialog dialog = new AlertDialog.Builder(activity).setTitle(versionTitle).setMessage(updateContent).setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAPK(activity.getApplicationContext(), response);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setCancelable(false).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                if (mustUpdate == 1) { // 必须升级
                    Dialog dialog = new AlertDialog.Builder(activity).setTitle(versionTitle).setMessage(updateContent).setNeutralButton("立即升级", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadAPK(activity, response);
                            dialog.dismiss();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            activity.moveTaskToBack(true);
                        }
                    }).setCancelable(false).create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                } else {
                    Dialog dialog = new AlertDialog.Builder(activity).setTitle(versionTitle).setMessage(updateContent).setPositiveButton("升级", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadAPK(activity, response);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setCancelable(false).create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            }
        } else {
            if (this.needLoading)
                ToastUtils.showToast("已经是最新版本");
        }
    }

    public void downloadAPK(final Context context, final UpdateResponse response) {
        if (downloadProgressDialog != null && downloadProgressDialog.isShowing())
            return;
        File fileDir = new File(context.getFilesDir(), "download");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        final String apkUrl = response.apkUrl;
        final long apkSize = response.apkSize;
        final File filePath = new File(fileDir, UUID.nameUUIDFromBytes(String.format("%s-%s-%d", apkUrl, response.versionName, apkSize).getBytes()).toString() + ".apk");
        boolean hasDownloaded = filePath.exists() && filePath.length() >= apkSize;
        if (hasDownloaded) {
            Tool.installApk(filePath.getAbsolutePath());
            return;
        }

        downloadProgressDialog = new ProgressDialog(context);
        downloadProgressDialog.setMessage("下载中，请稍候...");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setCancelable(false);
        downloadProgressDialog.setCanceledOnTouchOutside(false);
        downloadProgressDialog.show();

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).readTimeout(60 * 5 * 10000, TimeUnit.MILLISECONDS).addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), new ProgressResponseBody.ProgressListener() {
                            @Override
                            public void update(long bytesRead, long contentLength, boolean done) {
                                if (downloadProgressDialog == null)
                                    return;
                                int p = (int) (bytesRead * 1.f / contentLength * 100);
                                if (p > progress) {
                                    progress = p;
                                    downloadProgressDialog.setProgress(progress);
                                }

                                if (done)
                                    if (downloadProgressDialog != null)
                                        downloadProgressDialog.dismiss();
                            }
                        }))
                        .build();
            }
        }).build();

        Request request = new Request.Builder().url(apkUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    BufferedSource source = body.source();
                    BufferedSink sink = Okio.buffer(Okio.sink(filePath));
                    sink.writeAll(source);
                    sink.close();
                    Tool.installApk(filePath.getAbsolutePath());
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        destory();
    }
}
