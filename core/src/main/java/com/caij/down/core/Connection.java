package com.caij.down.core;

import java.io.IOException;
import java.io.InputStream;

public interface Connection {

    void connect() throws IOException;

    void disconnect();

    int getResponseCode() throws IOException;

    InputStream getInputStream() throws IOException;

    long getContentLength();

    void cancel();
}
