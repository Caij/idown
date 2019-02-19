package com.caij.down.rx;

import com.caij.down.core.DataSource;
import com.caij.down.core.Download;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Progress;
import com.caij.down.core.Result;
import com.caij.down.core.URLHttpEngine;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class RxDownload extends Download {

    @Override
    public Result down(String url, DataSource dataSource) {
        Flowable<Progress> progressFlowable = new CallExecuteObservable(mEngine, mLogger, url, dataSource)
                .toFlowable(BackpressureStrategy.BUFFER);
        return new RxResult(progressFlowable);
    }
}
