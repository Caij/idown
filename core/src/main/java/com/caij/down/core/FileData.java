package com.caij.down.core;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileData implements DataSource {

    private final File mTempFile;
    private final File mFile;

    public FileData(File file) {
        mFile = file;
        mTempFile = new File(mFile.getAbsolutePath() + ".temp");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!mTempFile.getParentFile().exists()) {
            if (!mTempFile.getParentFile().mkdirs()) {
                throw new IOException();
            }
        }
        return new FileOutputStream(mTempFile);
    }

    @Override
    public void onSuccess() {
        if (mTempFile != null && mTempFile.exists()) {
            boolean isRename = mTempFile.renameTo(mFile);
            if (!isRename) {
                throw new RuntimeException("file rename error");
            }
        }
    }

    @Override
    public void onError(Exception e) {
        if (mTempFile != null && mTempFile.exists()) {
            mTempFile.deleteOnExit();
            Log.d("FileData", "on error delete file " + mTempFile.getAbsolutePath());
        }
    }
}
