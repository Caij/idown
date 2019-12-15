package com.caij.down.asynctask;

import com.caij.down.core.Callback;

import java.util.concurrent.Executor;

public abstract class Result {

    protected Executor mExeExecutor;
    protected Executor mCallExecutor;

    public abstract void cancel();

    public abstract Result execute(Callback callback);

    public Result executeOn(Executor executor) {
        mExeExecutor = executor;
        return this;
    }

    public Result callbackOn(Executor executor) {
        mCallExecutor = executor;
        return this;
    }
}
