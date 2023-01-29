package com.caij.down.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class URLHttpEngine implements Engine {

    @Override
    public Connection createConnection(String url) {
        return new HttpConnection(url);
    }

    private static class HttpConnection implements Connection {

        private String mUrl;
        private HttpURLConnection mConnection;

        HttpConnection(String url) {
            mUrl = url;
        }

        @Override
        public void connect() throws IOException {
            connect(null);
        }

        @Override
        public void connect(Map<String, String> headers) throws IOException {
            URL connectURL = new URL(mUrl);
            mConnection = (HttpURLConnection) connectURL.openConnection();
            mConnection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
            mConnection.setRequestMethod("GET");
            mConnection.setConnectTimeout(5 * 1000);
            mConnection.setReadTimeout(5 * 1000);

            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    mConnection.setRequestProperty(entry.getKey(), entry.getKey());
                }
            }

            mConnection.connect();
        }

        @Override
        public void disconnect() {
            if (mConnection != null) mConnection.disconnect();
        }

        @Override
        public int getResponseCode() throws IOException {
            checkState();
            return mConnection.getResponseCode();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            checkState();
            return mConnection.getInputStream();
        }

        @Override
        public long getContentLength() {
            checkState();
            return Long.parseLong(mConnection.getHeaderField("Content-Length"));
        }

        @Override
        public void cancel() {
            try {
                if (mConnection != null) mConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void checkState() {
            if (mConnection == null) throw new IllegalStateException("not connect");
        }
    }
}
