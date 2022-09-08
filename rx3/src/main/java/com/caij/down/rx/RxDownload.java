package com.caij.down.rx;

import com.caij.down.core.DataSource;
import com.caij.down.core.Download;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;


public class RxDownload extends Download {

    public RxDownload(Engine engine, Logger logger) {
        super(engine, logger);
    }

    public RxDownload() {
    }

    public RxDownload(Engine engine) {
        super(engine);
    }

    public CallFlowable down(String url, DataSource dataSource, long timeInterval) {
        return new CallFlowable(mEngine, mLogger, url, dataSource, timeInterval);
    }

}
