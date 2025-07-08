package com.contrastsecurity.csvdltool.api;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

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

        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                lastException = e;
                logger.warn(request.url());
                logger.warn("Request failed, retrying by interceptor... (" + (retryCount + 1) + "/" + maxRetries + ")");
                // System.err.println("Request failed, retrying... (" + (i + 1) + "/" + maxRetries + ")");
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
