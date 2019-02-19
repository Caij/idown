package com.caij.down.core;

import java.io.IOException;
import java.io.OutputStream;

public interface DataSource {

    OutputStream getOutputStream() throws IOException;

    void onSuccess() throws IOException;

    void onError(Exception e);
}
