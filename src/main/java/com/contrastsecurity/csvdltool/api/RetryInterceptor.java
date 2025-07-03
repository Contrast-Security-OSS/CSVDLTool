package com.contrastsecurity.csvdltool.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {

    private final int maxRetries;
    private final long retryDelayMillis;

    public RetryInterceptor(int maxRetries, long retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                lastException = e;
                System.err.println("Request failed, retrying... (" + (i + 1) + "/" + maxRetries + ")");
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
            }
        }

        if (response != null) {
            return response;
        } else if (lastException != null) {
            throw lastException;
        } else {
            throw new IOException("Failed to execute request after " + maxRetries + " retries without a clear exception.");
        }
    }

}
