package com.caij.down.asynctask;

import com.caij.down.core.Callback;

import java.util.concurrent.Executor;

public abstract class Result {

    protected Executor mExeExecutor;

    public abstract void cancel();

    public abstract Result execute(Callback callback);

    public Result executeOn(Executor executor) {
        mExeExecutor = executor;
        return this;
    }
}
