package com.caij.down.core;

import java.io.Closeable;

public class Util {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {

            }
        }
    }
}
