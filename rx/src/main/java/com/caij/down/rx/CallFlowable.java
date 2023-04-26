package com.caij.down.rx;

import android.os.SystemClock;

import com.caij.down.core.CoreDowner;
import com.caij.down.core.DataSource;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Progress;

import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;


public class CallFlowable implements FlowableOnSubscribe<Progress> {

    private final Map<String, String> mHeaders;
    private Logger mLogger;
    private Engine mEngine;
    private String mUrl;

    private DataSource mDataSource;
    private CoreDowner mCoreDowner;

    private long mTimeInterval;

    CallFlowable(Engine engine, Logger logger, String url, Map<String, String> headers, DataSource dataSource, long timeInterval) {
        mLogger = logger;
        mUrl = url;
        mDataSource = dataSource;
        mEngine = engine;
        mTimeInterval = timeInterval;
        mHeaders = headers;
    }

    @Override
    public void subscribe(FlowableEmitter<Progress> emitter) {
        start(emitter);
    }

    private void start(final FlowableEmitter<Progress> emitter) {
        if (mLogger != null) mLogger.log(Thread.currentThread().getName() + " start down url " + mUrl);

        final Progress progress = new Progress();
        progress.read = 0;
        progress.total = 100;

        if (!emitter.isCancelled()) {
            emitter.onNext(progress);
        }

        mCoreDowner = new CoreDowner(mUrl, mHeaders, mEngine, new CoreDowner.Listener() {

            private long mPreTime;

            @Override
            public boolean isCancel() {
                return emitter.isCancelled();
            }

            @Override
            public void onProgress(long total, long read) {
                progress.total = total;
                progress.read = read;

                if (SystemClock.elapsedRealtime() - mPreTime > mTimeInterval) {

                    if (!emitter.isCancelled()) {
                        emitter.onNext(progress);
                    }

                    mPreTime = SystemClock.elapsedRealtime();
                }
            }

            @Override
            public void onError(Exception e) {
                if (!emitter.isCancelled()) {
                    emitter.onError(e);
                }
            }

            @Override
            public void onComplete(long total) {
                if (!emitter.isCancelled()) {
                    progress.total = total;
                    progress.read = total;
                    emitter.onNext(progress);

                    emitter.onComplete();
                }
            }

        }, mLogger, mDataSource);

        mCoreDowner.start();
    }


    public void cancel() {
        mLogger.log("cancel");
    }


    public Flowable<Progress> toFlowable(BackpressureStrategy mode) {
        return Flowable.create(this, mode).doOnCancel(new Action() {
            @Override
            public void run() throws Exception {
                cancel();
            }
        });
    }
}