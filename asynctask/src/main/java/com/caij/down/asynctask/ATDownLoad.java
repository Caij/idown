package com.caij.down.asynctask;

import com.caij.down.core.DataSource;
import com.caij.down.core.Download;
import com.caij.down.core.Engine;
import com.caij.down.core.Logger;
import com.caij.down.core.Result;

public class ATDownLoad extends Download<Result> {

    public ATDownLoad(Engine engine, Logger logger) {
        super(engine, logger);
    }

    public ATDownLoad() {
    }

    public ATDownLoad(Engine engine) {
        super(engine);
    }

    @Override
    public Result down(String url, DataSource dataSource) {
        return new ATResult(mEngine, mLogger, url, dataSource);
    }
}
