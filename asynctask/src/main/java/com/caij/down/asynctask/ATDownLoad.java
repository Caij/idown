package com.caij.down.asynctask;

import com.caij.down.core.DataSource;
import com.caij.down.core.Download;
import com.caij.down.core.Result;

public class ATDownLoad extends Download {

    @Override
    public Result down(String url, DataSource dataSource) {
        return new ATResult(mEngine, mLogger, url, dataSource);
    }
}
