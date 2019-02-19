package com.caij.down.rx;

import com.caij.down.core.Callback;
import com.caij.down.core.Progress;
import com.caij.down.core.Result;

import java.util.concurrent.Executor;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class RxResult extends Result {

    private Flowable<Progress> mObservable;
    private Disposable mDisposable;


    public RxResult(Flowable<Progress> observable) {
        mObservable = observable;
    }

    @Override
    public void cancel() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    public Result execute(final Callback callback) {
        if (mExeExecutor != null) {
            mObservable = mObservable.subscribeOn(Schedulers.from(mExeExecutor));
        }

        if (mCallExecutor != null) {
            mObservable = mObservable.observeOn(Schedulers.from(mCallExecutor));
        }

        mDisposable = mObservable
                .subscribeWith(new DisposableSubscriber<Progress>() {
            @Override
            public void onNext(Progress progress) {
                callback.onProgress(progress);
            }

            @Override
            public void onError(Throwable e) {
                callback.onError(e);
            }

            @Override
            public void onComplete() {
                callback.onComplete();
            }
        });
        return this;
    }
}
