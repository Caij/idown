package com.caij.down.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CoreDowner {

    private static final int BUF_LONG = 8 * 1024;

    private final Engine mEngine;
    private final String mUrl;
    private Connection mConnection;
    private final Listener mListener;
    private final Logger mLogger;
    private final DataSource mDataSource;

    public CoreDowner(String url, Engine engine, Listener listener, Logger logger, DataSource dataSource) {
        mEngine = engine;
        mUrl = url;
        mListener = listener;
        mLogger = logger;
        mDataSource = dataSource;
    }

    public void start() {
        InputStream serverInputStream = null;
        OutputStream outputStream = null;
        try {
            mConnection = mEngine.createConnection(mUrl);

            mConnection.connect();

            checkState();

            int responseCode = mConnection.getResponseCode();

            if (mLogger != null) mLogger.log("responseBody success code " + mConnection.getResponseCode());

            if (!isSuccessful(responseCode)) throw new IOException("Unexpected code " + responseCode);

            long total = mConnection.getContentLength();
            long read = 0;

            mListener.onProgress(total, read);

            serverInputStream = mConnection.getInputStream();

            outputStream = mDataSource.getOutputStream();

            save(serverInputStream, outputStream, total);

            if (!mListener.isCancel()) {
                mListener.onProgress(total, total);
            }

            close(outputStream, serverInputStream);

            checkState();

            mDataSource.onSuccess();

            if (!mListener.isCancel()) {
                if (mLogger != null) mLogger.log("url onCompleted " + mUrl);
            } else {
                if (mLogger != null) mLogger.log("url disposable isDisposed " + mUrl);
            }

            mListener.onComplete(total);
        } catch (Exception e) {
            close(outputStream, serverInputStream);

            mDataSource.onError(e);

            if (!mListener.isCancel()) {
                mListener.onError(e);
            }

            if (mLogger != null) mLogger.log("url Exception " + mUrl + " " + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    private void checkState() throws InterruptedException {
        if (mListener.isCancel()) {
            throwCancelException();
        }
    }

    private void save(InputStream inputStream, OutputStream outputStream, long total) throws IOException, InterruptedException {
        byte[] buf = new byte[BUF_LONG];
        int readLength;
        long writeTotalLength = 0;

        if (mListener.isCancel()) {
            throwCancelException();
        }

        while ((readLength = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, readLength);
            writeTotalLength += readLength;

            if (!mListener.isCancel()) {
                mListener.onProgress(total, writeTotalLength);
            }

            checkState();
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


    private boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    private void throwCancelException() throws InterruptedException {
        throw new InterruptedException("No subscription, cancel download");
    }

    public void cancel() {
        if (mConnection != null) {
            mConnection.cancel();
        }
    }

    public interface Listener {

        boolean isCancel();

        void onProgress(long total, long read);

        void onError(Exception e);

        void onComplete(long total);
    }
}
