package com.caij.down.core;

public interface Callback {

    void onProgress(Progress progress);

    void onComplete();

    void onError(Throwable e);
}
