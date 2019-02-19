package com.caij.down.core;

import android.util.Log;

public interface Logger {

    String TAG = "RxDownload";

    public void log(String msg);

    public static Logger DEFAULT = new Logger() {
        @Override
        public void log(String msg) {
            Log.d(TAG, msg);
        }
    };
}
