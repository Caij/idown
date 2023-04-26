package com.caij.down.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Connection {

    void connect() throws IOException;

    void connect(Map<String, String> headers) throws IOException;

    void disconnect();

    int getResponseCode() throws IOException;

    InputStream getInputStream() throws IOException;

    long getContentLength();

}
