package com.ouj.library.module;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ouj.library.BaseApplication;
import com.ouj.library.R;
import com.ouj.library.net.OKHttp;
import com.ouj.library.net.body.ProgressResponseBody;
import com.ouj.library.net.extend.ResponseCallback;
import com.ouj.library.util.SharedPrefUtils;
import com.ouj.library.util.ToastUtils;
import com.ouj.library.util.Tool;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private int progress;
    private boolean isChecking;
    private String time;
    private ProgressListener progressListener;

    private final String PREF = "AppVersionTime";

    public AppVersion(Activity activity, boolean needLoading) {
        this.activity = activity;
        this.needLoading = needLoading;
        this.time = new SimpleDateFormat("MMdd").format(new Date());
    }

    public void destory() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        activity = null;
        progressDialog = null;
        progressListener = null;
    }

    public void checkVersion() {
        if (!needLoading) { // 一般是在启动时调用，点暂不更新时当天不重复检测
            String prefTime = SharedPrefUtils.get(PREF, "");
            if (time.equals(prefTime))
                return;
        }
        if (isChecking)
            return;
        isChecking = true;
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
                isChecking = false;
            }
        });

    }

    protected Dialog createUpdateDialog(Activity activity) {
        return new AlertDialog.Builder(activity).setView(R.layout.base__view_version).setCancelable(false).create();
    }

    protected void updateResult(final Activity activity, final UpdateResponse response) {
        if (response.haveNewVersion > 0) {
            if(TextUtils.isEmpty(response.versionName))
                return;
            if (!needLoading) {
                int times = SharedPrefUtils.get(response.versionName, 1);
                if (times >= 3) {
                    return;
                }
            }
            String versionTitle = String.format("发现新版本 v%s", response.versionName);
            String updateContent = response.updateContent;
            int mustUpdate = response.mustUpdate;
            final Dialog dialog = createUpdateDialog(activity);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            TextView title, content;
            TextView update, cancel;
            title = (TextView) dialog.findViewById(R.id.title);
            content = (TextView) dialog.findViewById(R.id.content);
            update = (TextView) dialog.findViewById(R.id.update);
            cancel = (TextView) dialog.findViewById(R.id.cancel);
            title.setText(versionTitle);
            content.setText(updateContent);
            update.setText("马上更新");

            update.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    downloadAPK(activity, response);
                    dialog.dismiss();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!needLoading) {
                        SharedPrefUtils.put(PREF, time);
                        int times = SharedPrefUtils.get(response.versionName, 1);
                        SharedPrefUtils.put(response.versionName, ++times);
                    }
                    dialog.dismiss();
                }
            });
            if (mustUpdate == 1 && !needLoading) { // 必须升级
                cancel.setVisibility(View.GONE);
            }
        } else {
            if (this.needLoading)
                ToastUtils.showToast("已经是最新版本");
        }
    }

    protected ProgressListener createProgressListener(Activity activity) {
        final DownloadDialog downloadProgressDialog = new DownloadDialog(activity);
        downloadProgressDialog.setCancelable(false);
        downloadProgressDialog.setCanceledOnTouchOutside(false);
        downloadProgressDialog.show();
        return downloadProgressDialog;
    }


    protected void downloadAPK(final Activity context, final UpdateResponse response) {
        if (progressListener != null)
            return;
        File fileDir = context.getExternalFilesDir("download");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        final String apkUrl = response.apkUrl;
        final long apkSize = response.apkSize;
        final File filePath = new File(fileDir, UUID.nameUUIDFromBytes(String.format("%s-%d", response.versionName, apkSize).getBytes()).toString() + ".apk");
        boolean hasDownloaded = filePath.exists() && filePath.length() >= apkSize;
        if (hasDownloaded) {
            Tool.installApk(filePath.getAbsolutePath());
            return;
        }

        File[] files = fileDir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (!file.getName().equals(filePath.getName())) {
                    file.delete();
                }
            }
        }

        progressListener = createProgressListener(activity);

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).readTimeout(60 * 5 * 10000, TimeUnit.MILLISECONDS).addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), new ProgressResponseBody.ProgressListener() {
                            @Override
                            public void update(long bytesRead, long contentLength, boolean done) {
                                if (progressListener == null)
                                    return;
                                int p = (int) (bytesRead * 1.f / contentLength * 100);
                                if (p > progress) {
                                    progress = p;
                                    progressListener.onProgress(progress);
                                }

                                if (done)
                                    progressListener.onDestory();
                            }
                        }))
                        .build();
            }
        }).build();

        Request request = new Request.Builder().url(apkUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (progressListener != null)
                    progressListener.onDestory();
                progressListener = null;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    BufferedSource source = body.source();
                    BufferedSink sink = Okio.buffer(Okio.sink(filePath));
                    sink.writeAll(source);
                    sink.close();
                    if (progressListener != null)
                        progressListener.onDestory();
                    progressListener = null;
                    Tool.installApk(filePath.getAbsolutePath());
                }
            }
        });
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        destory();
    }

    public static interface ProgressListener {
        public void onProgress(int progress);

        public void onDestory();
    }

    class DownloadDialog extends AlertDialog implements ProgressListener {

        ProgressBar progressBar;
        TextView progressTv;

        public DownloadDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.base__view_version_download, null);
            setView(view);
            super.show();
            progressBar = (ProgressBar) findViewById(com.ouj.library.R.id.progressBar);
            progressTv = (TextView) findViewById(com.ouj.library.R.id.progress);
        }

        @Override
        public void onProgress(final int progress) {
            if (isShowing()) {
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progress);
                            progressTv.setText(progress + "%");
                        }
                    });
            }
        }

        @Override
        public void onDestory() {
            if (isShowing()) {
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    });
            }
        }
    }
}
