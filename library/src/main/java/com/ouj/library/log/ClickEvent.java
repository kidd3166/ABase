package com.ouj.library.log;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.ouj.library.BaseApplication;
import com.ouj.library.event.OnForegroundEvent;
import com.ouj.library.net.OKHttp;
import com.ouj.library.util.DeviceUtils;
import com.ouj.library.util.FileUtils;
import com.ouj.library.util.PackageUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.zip.GZIPOutputStream;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Liqi
 */
class ClickEvent extends Thread {

    public static String UPLOAD_URL = "http://" + (BaseApplication.APP_DEBUG ? "test." : "") + "www.hiyd.com/data/batchEvent";

    private final PriorityBlockingQueue<ClickLog> mQueue = new PriorityBlockingQueue<ClickLog>();

    // Save File info
    private long mFileSize = 2 * 1024 * 1024;
    private int mFileCount = 10;

    private String mFilePath = null;
    private String mExternalStoragePath = "Logs" + File.separator + "click";

    private String mLogName = null;
    private String mLogFilePathName = null;

    private FileWriter mFileWriter = null;

    private int line = 0;
    private long start = 0;

    /**
     * LogFile
     *
     * @param count file count
     * @param size  file size
     * @param path  save path
     */
    protected ClickEvent(int count, float size, String path) {

        mFileSize = (long) (size * 1024 * 1024);
        mFileCount = count;
        mFilePath = path;

        init();

        this.setName(ClickEvent.class.getName());
        this.setDaemon(true);
        this.start();

        start = System.currentTimeMillis();
        EventBus.getDefault().register(this);
    }

    public void onEvent(OnForegroundEvent event) {
        uploadLog(BaseApplication.APP_UID);
    }

    public void onEvent(ClickLogEvent event) {
        if (!TextUtils.isEmpty(event.userId)) {
            ClickLog.close();
            uploadLog(event.userId);
        }
    }

    private void uploadLog(final String userId) {
        Log.d("APP", "App uploadLog " + userId);
        if (TextUtils.isEmpty(userId))
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = FileUtils.getFileDir(BaseApplication.app, "Logs" + File.separator + "click");
                    if (file == null || !file.isDirectory())
                        return;

                    File[] files = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return !filename.endsWith(".olt");
                        }
                    });
                    if (files != null && files.length > 0) {
                        for (File f : files) {
                            File outputFile = null;
                            if (f.getName().endsWith(".ol")) {
                                BufferedReader in = new BufferedReader(new FileReader(f));
                                outputFile = new File(file, userId + "_" + f.getName() + ".gz");
                                BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
                                Context context = BaseApplication.app;
                                String head = String.format("userId=%s&sysOS=Android&channel=%s&deviceToken=%s&SysVersion=%s&appVersion=%s\r\n", userId, BaseApplication.APP_CHANNEL, DeviceUtils.deviceToken(context), String.valueOf(Build.VERSION.SDK_INT), PackageUtils.getVersion(context));
                                out.write(head.getBytes());
                                String line = null;
                                while ((line = in.readLine()) != null) {
                                    out.write(line.getBytes());
                                    out.write("\r\n".getBytes());
                                }
                                out.flush();
                                in.close();
                                out.close();
                                f.delete();
                            } else {
                                outputFile = f;
                            }
                            if (outputFile == null)
                                return;
                            Log.d("APP", "App uploadLog " + outputFile.getAbsolutePath());

                            MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                    .addFormDataPart("dataFile", outputFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), outputFile)).build();
                            Request request = new Request.Builder()
                                    .url(UPLOAD_URL)
                                    .post(body)
                                    .build();
                            try {
                                Response response = new OKHttp.Builder(this).cacheType(OKHttp.CacheType.ONLY_NETWORK).build().execute(request);
                                if (response.isSuccessful()) {
                                    outputFile.delete();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();

    }

    /**
     * *********************************************************************************************
     * private methods
     * *********************************************************************************************
     */
    /**
     * init
     *
     * @return status
     */
    private boolean init() {
        boolean bFlag = false;

        if (initFilePath() && initLogNameSize()) {
//            deleteOldLogFile();
            bFlag = true;
            checkLogLength();
        }

        return bFlag;
    }

    /**
     * init FilePath
     *
     * @return status
     */
    private boolean initFilePath() {
        boolean bFlag;
        try {
            File file = new File(mFilePath);
            bFlag = file.isDirectory() || file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            bFlag = false;
        }
        return bFlag;
    }

    /**
     * init LogNameSize
     *
     * @return status
     */
    private boolean initLogNameSize() {
        boolean bFlag;
        //close fileWriter
        if (mFileWriter != null) {
            try {
                mFileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mFileWriter = null;
        }
        //init File
        try {
            File file = new File(mFilePath);
            File[] files = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".olt");
                }
            });
            if (files != null && files.length > 0) {
                File endFile = files[files.length - 1];
                mLogName = endFile.getName();
                mLogFilePathName = endFile.getAbsolutePath();
                BufferedReader reader = new BufferedReader(new FileReader(endFile));
                String l = null;
                while ((l = reader.readLine()) != null) {
                    line++;
                }
                reader.close();
                bFlag = true;
            } else {
                bFlag = createNewLogFile();
            }
            //init fileWriter
            try {
                mFileWriter = new FileWriter(mLogFilePathName, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            bFlag = false;
        }
        return bFlag;
    }

    /**
     * Create Log
     *
     * @return status
     */
    private boolean createNewLogFile() {
        mLogName = UUID.randomUUID().toString() + ".olt";
        File file = new File(mFilePath, mLogName);
        try {
            if (file.createNewFile()) {
                mLogFilePathName = file.getAbsolutePath();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete OldLogFile
     *
     * @return status
     */
//    private boolean deleteOldLogFile() {
//        if (mFilePath == null) return false;
//        boolean bFlag = false;
//        try {
//            File file = new File(mFilePath);
//            if (file.isDirectory() && file.listFiles() != null) {
//                int count = file.listFiles().length - mFileCount;
//                if (count > 0) {
//                    File[] files = file.listFiles();
//                    for (int i = 0; i < count; i++) {
//                        bFlag = files[i].delete();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return bFlag;
//    }

    /**
     * check Log Length
     */
    public void checkLogLength() {
        checkLogLength(false);
    }

    public void checkLogLength(boolean close) {
        File file = new File(mLogFilePathName);
//        LogUtils.d("file create time: " + file.lastModified() + " " + (System.currentTimeMillis() - file.lastModified()));
        if (close || (System.currentTimeMillis() - file.lastModified()) > 60 * 60 * 1000 || (System.currentTimeMillis() - start) > 30 * 60 * 1000 || line > 500 || file.length() >= mFileSize) {
            line = 0;
            start = System.currentTimeMillis();
            try {
                file.renameTo(new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 1)));
            } catch (Exception e) {
                e.printStackTrace();
                file.delete();
            }
            createNewLogFile();
            initLogNameSize();
//            deleteOldLogFile();
            if (!close)
                uploadLog(BaseApplication.APP_UID);
        }
    }

    /**
     * appendLogsTo File
     *
     * @param data Log
     */
    private void appendLogs(ClickLog data) {
        if (mFileWriter != null) {
            try {
                mFileWriter.append(data.toString());
                mFileWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
                initLogNameSize();
//                deleteOldLogFile();
            } finally {
            }
            line++;
            checkLogLength();
        } else {
            initLogNameSize();
//            deleteOldLogFile();
        }
    }


    /**
     * *********************************************************************************************
     * Public methods
     * *********************************************************************************************
     */

    /**
     * add Log
     *
     * @param data Log
     */
    protected void addLog(ClickLog data) {
        try {
            mQueue.put(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get log file path
     */
    protected static String getDefaultLogPath() {
        try {
            File file = FileUtils.getFileDir(BaseApplication.app, "Logs" + File.separator + "click");
            if (file != null) {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thread
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            try {
                ClickLog data = mQueue.take();
                if (data != null) {
                    appendLogs(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}