package com.caij.down.asynctask;

import com.caij.down.core.DataSource;
import com.caij.down.core.Download;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;

import java.util.Map;

public class ATDownLoad extends Download {

    public ATDownLoad(Engine engine, Logger logger) {
        super(engine, logger);
    }

    public ATDownLoad() {
    }

    public ATDownLoad(Engine engine) {
        super(engine);
    }

    public Result down(String url, Map<String, String> headers, DataSource dataSource, long timeInterval) {
        return new ATResult(mEngine, mLogger, url, headers,  dataSource, timeInterval);
    }

    public Result down(String url, DataSource dataSource, long timeInterval) {
        return new ATResult(mEngine, mLogger, url, null,  dataSource, timeInterval);
    }
}
