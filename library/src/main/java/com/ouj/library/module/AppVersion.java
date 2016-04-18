package com.ouj.library.module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.ouj.library.BaseApplication;
import com.ouj.library.net.body.ProgressResponseBody;
import com.ouj.library.util.Tool;

import org.json.JSONObject;

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

    public AppVersion(Activity activity, boolean needLoading) {
        this.activity = activity;
        this.needLoading = needLoading;
    }

    public void destory() {
        activity = null;
    }

    public void checkVersion() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).readTimeout(10000, TimeUnit.MILLISECONDS).build();
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
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                destory();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if(activity == null)
                        return;

                    String responseString = response.body().string();
                    try {
                        final JSONObject responseJson = new JSONObject(responseString);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateResult(activity, responseJson);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                            progressDialog = null;
                        }
                    });
                }
            }
        });
    }

    private void updateResult(final Activity activity, JSONObject responseJson) {
        JSONObject data = responseJson.optJSONObject("data");
        int haveNewVersion = data.optInt("haveNewVersion", 0);
        if (haveNewVersion > 0) {
            String versionTitle = String.format("发现新版本 v%s", data.optString("versionName"));
            String updateContent = data.optString("updateContent");
            final String apkUrl = data.optString("apkUrl");
            int mustUpdate = data.optInt("mustUpdate", 0);
            if (mustUpdate == 1) { // 必须升级
                new AlertDialog.Builder(activity).setTitle(versionTitle).setMessage(updateContent).setNeutralButton("立即升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAPK(apkUrl);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.moveTaskToBack(true);
                    }
                }).setOnDismissListener(this).show();
            } else {
                new AlertDialog.Builder(activity).setTitle(versionTitle).setMessage(updateContent).setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAPK(apkUrl);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setOnDismissListener(this).show();
            }
        }
    }

    private void downloadAPK(String apkUrl) {
        File fileDir = new File(activity.getFilesDir(), "download");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        final File filePath = new File(fileDir, UUID.nameUUIDFromBytes(apkUrl.getBytes()).toString());

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).readTimeout(60 * 2 * 10000, TimeUnit.MILLISECONDS).addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), new ProgressResponseBody.ProgressListener() {
                            @Override
                            public void update(long bytesRead, long contentLength, boolean done) {
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
