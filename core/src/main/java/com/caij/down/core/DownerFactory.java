package com.caij.down.core;

import java.util.HashMap;
import java.util.Map;

public class DownerFactory {

    private Map<Class, Download> downloadMap;

    public DownerFactory() {
        downloadMap = new HashMap<>();
    }

    public <R> Download<R> getDownload(Class<R> rClass) {
        return downloadMap.get(rClass);
    }

    public void register(Class c, Download download) {
        downloadMap.put(c, download);
    }
}
