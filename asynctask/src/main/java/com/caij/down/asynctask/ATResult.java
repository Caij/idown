package com.caij.down.asynctask;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.caij.down.core.Callback;
import com.caij.down.core.Connection;
import com.caij.down.core.DataSource;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Progress;
import com.caij.down.core.Result;
import com.caij.down.core.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ATResult extends Result {

    private static final int SUCCESS = 200;

    private static final int BUF_LONG = 4 * 1024;

    private Logger mLogger;
    private Engine mEngine;
    private String mUrl;

    private Connection mConnection;

    private DataSource mDataSource;

    private boolean isCancel;

    private AsyncTask mAsyncTask;


    ATResult(Engine engine, Logger logger, String url, DataSource dataSource) {
        mLogger = logger;
        mEngine = engine;
        mUrl = url;
        mDataSource = dataSource;
    }


    @Override
    public void cancel() {
        isCancel = true;
        if (mConnection != null) {
            mConnection.cancel();
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Result execute(final Callback callback) {
        mAsyncTask = new AsyncTask<Void, Progress, Integer>(){

            @Override
            protected Integer doInBackground(Void... voids) {
                InputStream serverInputStream = null;
                OutputStream outputStream = null;
                try {
                    if (mLogger != null) mLogger.log(Thread.currentThread().getName() + " start down url " + mUrl);

                    Progress progress = new Progress();
                    progress.read = 0;
                    progress.total = 100;

                    if (isCancel) return null;

                    publishProgress(progress);

                    mConnection = mEngine.createConnection(mUrl);

                    mConnection.connect();

                    checkState();

                    int responseCode = mConnection.getResponseCode();

                    if (mLogger != null) mLogger.log("responseBody success code " + mConnection.getResponseCode());

                    if (!isSuccessful(responseCode)) throw new IOException("Unexpected code " + responseCode);

                    progress.total = mConnection.getContentLength();
                    progress.read = 0;

                    checkState();

                    publishProgress(progress);

                    serverInputStream = mConnection.getInputStream();

                    outputStream = mDataSource.getOutputStream();

                    byte[] buf = new byte[BUF_LONG];
                    int readLength;
                    long writeTotalLength = 0;

                    checkState();

                    while ((readLength = serverInputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, readLength);
                        writeTotalLength += readLength;
                        progress.read = writeTotalLength;

                        if (!isCancel) {
                            publishProgress(progress);
                        }

                        checkState();
                    }

                    outputStream.flush();

                    close(outputStream, serverInputStream);

                    progress.read = progress.total;

                    checkState();

                    mDataSource.onSuccess();

                    checkState();

                    publishProgress(progress);

                    if (!isCancel) {
                        if (mLogger != null) mLogger.log("url onCompleted " + mUrl);
                    } else {
                        if (mLogger != null) mLogger.log("url disposable isDisposed " + mUrl);
                    }

                    return SUCCESS;
                } catch (Exception e) {
                    close(outputStream, serverInputStream);

                    mDataSource.onError(e);

                    if (!isCancel) {
                        callback.onError(e);
                    }

                    if (mLogger != null) mLogger.log("url Exception " + mUrl + " " + e.getClass().getSimpleName() + ":" + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Progress... values) {
                super.onProgressUpdate(values);
                if (!isCancel) {
                    callback.onProgress(values[0]);
                }
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if(integer != null && integer == SUCCESS) {
                    if (!isCancel) {
                        callback.onComplete();
                    }
                }
            }
        };
        return this;
    }

    private boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    private void checkState() throws InterruptedException {
        if (isCancel) {
            throw new InterruptedException("No subscription, cancel download");
        }
    }

    private void close(OutputStream outputStream, InputStream serverInputStream) {
        try {
            if (outputStream != null) {
                Util.closeQuietly(outputStream);
            }

            if (serverInputStream != null) {
                Util.closeQuietly(serverInputStream);
            }

            if (mConnection != null) {
                mConnection.disconnect();
                mConnection = null;
            }

            if (mLogger != null) mLogger.log("url close " + mUrl);
        } catch (Exception e) {
            if (mLogger != null) mLogger.log("close Exception " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }
}
