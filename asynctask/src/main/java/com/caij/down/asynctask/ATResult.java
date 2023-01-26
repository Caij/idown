package com.caij.down.asynctask;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import com.caij.down.core.Callback;
import com.caij.down.core.CoreDowner;
import com.caij.down.core.DataSource;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Progress;


public class ATResult extends Result {

    private static final int SUCCESS = 200;

    private Logger mLogger;
    private Engine mEngine;
    private String mUrl;

    private DataSource mDataSource;


    private AsyncTask mAsyncTask;

    private CoreDowner mCoreDowner;

    private long mTimeInterval;


    ATResult(Engine engine, Logger logger, String url, DataSource dataSource, long timeInterval) {
        mLogger = logger;
        mEngine = engine;
        mUrl = url;
        mDataSource = dataSource;
        mTimeInterval = timeInterval;
    }


    @Override
    public void cancel() {
        if (mCoreDowner != null) {
            mCoreDowner.cancel();
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Result execute(final Callback callback) {
        mAsyncTask = new AsyncTask<Void, Progress, Object>(){

            Object result;

            @Override
            protected Integer doInBackground(Void... voids) {
                if (mLogger != null) mLogger.log(Thread.currentThread().getName() + " start down url " + mUrl);

                final Progress progress = new Progress();
                progress.read = 0;
                progress.total = 100;

                if (isCancelled()) return null;

                mCoreDowner = new CoreDowner(mUrl, mEngine, new CoreDowner.Listener() {

                    private long mPreTime;

                    @Override
                    public boolean isCancel() {
                        return isCancelled();
                    }

                    @Override
                    public void onProgress(long total, long read) {
                        progress.total = total;
                        progress.read = read;

                        if (SystemClock.elapsedRealtime() - mPreTime > mTimeInterval) {

                            publishProgress(progress);

                            mPreTime = SystemClock.elapsedRealtime();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        result = e;
                    }

                    @Override
                    public void onComplete(long total) {
                        if (!isCancelled()) {
                            progress.total = total;
                            progress.read = total;
                            publishProgress(progress);
                        }
                        result = SUCCESS;
                    }

                }, mLogger, mDataSource);

                mCoreDowner.start();

                return SUCCESS;
            }

            @Override
            protected void onProgressUpdate(Progress... values) {
                super.onProgressUpdate(values);
                if (!isCancelled()) {
                    callback.onProgress(values[0]);
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                if (result instanceof Integer && result.equals(SUCCESS)) {
                    if (!isCancelled()) {
                        callback.onComplete();
                    }
                } else {
                    if (!isCancelled()) {
                        callback.onError(result == null ? null : (Throwable) result);
                    }
                }
            }
        };
        return this;
    }
}
