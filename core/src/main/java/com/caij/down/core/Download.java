package com.caij.down.core;


public abstract class Download<R> {

    protected Logger mLogger;
    protected Engine mEngine;

    public Download(Engine engine, Logger logger) {
        mLogger = logger;
        mEngine = engine;
    }

    public Download() {
        this(new URLHttpEngine(), Logger.DEFAULT);
    }

    public Download(Engine engine) {
        this(engine, Logger.DEFAULT);
    }

    public abstract R down(String url, DataSource dataSource);


}
