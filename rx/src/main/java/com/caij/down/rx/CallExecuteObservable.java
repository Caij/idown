package com.caij.down.rx;

import com.caij.down.core.Connection;
import com.caij.down.core.DataSource;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Progress;
import com.caij.down.core.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class CallExecuteObservable extends Observable<Progress> {

    private static final int BUF_LONG = 4 * 1024;

    private Logger mLogger;
    private Engine mEngine;
    private String mUrl;

    private Connection mConnection;

    private DataSource mDataSource;


    CallExecuteObservable(Engine engine, Logger logger, String url, DataSource dataSource) {
        mLogger = logger;
        mEngine = engine;
        mUrl = url;
        mDataSource = dataSource;
    }

    @Override
    protected void subscribeActual(Observer<? super Progress> observer) {
        CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);

        start(observer, disposable);
    }

    private void start(Observer<? super Progress> observer, CallDisposable disposable) {
        InputStream serverInputStream = null;
        OutputStream outputStream = null;
        try {
            if (mLogger != null) mLogger.log(Thread.currentThread().getName() + " start down url " + mUrl);

            Progress progress = new Progress();
            progress.read = 0;
            progress.total = 100;

            if (!disposable.isDisposed()) {
                observer.onNext(progress);
            }

            mConnection = mEngine.createConnection(mUrl);

            mConnection.connect();

            checkState(disposable);

            int responseCode = mConnection.getResponseCode();

            if (mLogger != null) mLogger.log("responseBody success code " + mConnection.getResponseCode());

            if (!isSuccessful(responseCode)) throw new IOException("Unexpected code " + responseCode);

            progress.total = mConnection.getContentLength();
            progress.read = 0;

            if (!disposable.isDisposed()) {
                observer.onNext(progress);
            }

            checkState(disposable);

            serverInputStream = mConnection.getInputStream();

            outputStream = mDataSource.getOutputStream();

            save(serverInputStream, outputStream, progress, observer, disposable);

            close(outputStream, serverInputStream);

            progress.read = progress.total;

            checkState(disposable);

            mDataSource.onSuccess();

            if (!disposable.isDisposed()) {
                observer.onNext(progress);
                observer.onComplete();
            }

            if (!disposable.isDisposed()) {
                if (mLogger != null) mLogger.log("url onCompleted " + mUrl);
            } else {
                if (mLogger != null) mLogger.log("url disposable isDisposed " + mUrl);
            }
        } catch (Exception e) {
            close(outputStream, serverInputStream);

            mDataSource.onError(e);

            if (!disposable.isDisposed()) {
                observer.onError(e);
            }

            if (mLogger != null) mLogger.log("url Exception " + mUrl + " " + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    private boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    private void save(InputStream inputStream, OutputStream outputStream, Progress progress,
                      Observer<? super Progress> observer, CallDisposable disposable) throws IOException, InterruptedException {
        byte[] buf = new byte[BUF_LONG];
        int readLength;
        long writeTotalLength = 0;

        checkState(disposable);

        while ((readLength = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, readLength);
            writeTotalLength += readLength;
            progress.read = writeTotalLength;

            if (!disposable.isDisposed()) {
                observer.onNext(progress);
            }

            checkState(disposable);
        }

        outputStream.flush();
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

    private void checkState(Disposable disposable) throws InterruptedException {
        if (disposable != null && disposable.isDisposed()) {
            throw new InterruptedException("No subscription, cancel download");
        }
    }

    private void cancel() {
        if (mConnection != null) {
            mConnection.cancel();
        }
    }


    private final class CallDisposable implements Disposable {
        private volatile boolean disposed;

        CallDisposable() {

        }

        @Override public void dispose() {
            disposed = true;
            cancel();
        }

        @Override public boolean isDisposed() {
            return disposed;
        }
    }
}