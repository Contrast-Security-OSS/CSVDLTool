/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.csvdltool.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public abstract class Api {

    Logger logger = Logger.getLogger("csvdltool");

    protected IPreferenceStore preferenceStore;
    protected Organization organization;
    protected int totalCount;

    public Api(IPreferenceStore preferenceStore, Organization organization) {
        this.preferenceStore = preferenceStore;
        this.organization = organization;
    }

    public Object get() throws Exception {
        String response = this.getResponse();
        return this.convert(response);
    }

    public int getTotalCount() {
        return totalCount;
    }

    protected abstract String getUrl();

    protected abstract Object convert(String response);

    protected List<Header> getHeaders() {
        String apiKey = this.organization.getApikey();
        String serviceKey = preferenceStore.getString(PreferenceConstants.SERVICE_KEY);
        String userName = preferenceStore.getString(PreferenceConstants.USERNAME);
        String auth = String.format("%s:%s", userName, serviceKey);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = new String(encodedAuth);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        headers.add(new BasicHeader("API-Key", apiKey));
        headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
        return headers;
    }

    protected String getResponse() throws Exception {
        String url = this.getUrl();
        logger.trace(url);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        Request.Builder requestBuilder = new Request.Builder().url(url).get();
        List<Header> headers = this.getHeaders();
        for (Header header : headers) {
            requestBuilder.addHeader(header.getName(), header.getValue());
        }
        OkHttpClient httpClient = null;
        Request httpGet = requestBuilder.build();
        Response response = null;
        try {
            int connectTimeout = Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.CONNECTION_TIMEOUT));
            int sockettTimeout = Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.SOCKET_TIMEOUT));
            clientBuilder.readTimeout(sockettTimeout, TimeUnit.MILLISECONDS).connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

            if (preferenceStore.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] trustAllCerts = getTrustManager();
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                clientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                clientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            if (this.preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.preferenceStore.getString(PreferenceConstants.PROXY_HOST),
                        Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.PROXY_PORT)))));
                if (!this.preferenceStore.getString(PreferenceConstants.PROXY_AUTH).equals("none")) {
                    Authenticator proxyAuthenticator = null;
                    // プロキシ認証あり
                    if (this.preferenceStore.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                        proxyAuthenticator = new Authenticator() {
                            @Override
                            public Request authenticate(Route route, Response response) throws IOException {
                                String credential = Credentials.basic(preferenceStore.getString(PreferenceConstants.PROXY_TMP_USER),
                                        preferenceStore.getString(PreferenceConstants.PROXY_TMP_PASS));
                                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                            }
                        };
                    } else {
                        BasicTextEncryptor encryptor = new BasicTextEncryptor();
                        encryptor.setPassword(Main.MASTER_PASSWORD);
                        try {
                            String proxy_pass = encryptor.decrypt(preferenceStore.getString(PreferenceConstants.PROXY_PASS));
                            proxyAuthenticator = new Authenticator() {
                                @Override
                                public Request authenticate(Route route, Response response) throws IOException {
                                    String credential = Credentials.basic(preferenceStore.getString(PreferenceConstants.PROXY_USER), proxy_pass);
                                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                                }
                            };
                        } catch (Exception e) {
                            throw new ApiException("プロキシパスワードの復号化に失敗しました。\\r\\nパスワードの設定をやり直してください。");
                        }
                    }
                    clientBuilder.proxyAuthenticator(proxyAuthenticator);
                }
            }
            httpClient = clientBuilder.build();
            try {
                response = httpClient.newCall(httpGet).execute();
                if (response.code() == 200) {
                    return response.body().string();
                } else if (response.code() == 401) {
                    throw new ApiException(response.body().string());
                } else if (response.code() == 403) {
                    throw new ApiException(response.body().string());
                } else {
                    logger.warn(response.code());
                    logger.warn(response.body().string());
                    throw new NonApiException(String.valueOf(response.code()));
                }
            } catch (IOException ioe) {
                throw ioe;
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(url);
            logger.error(trace);
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.body().close();
                }
            } catch (Exception e) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                String trace = stringWriter.toString();
                logger.error(trace);
                throw e;
            }
        }
    }

    private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        } };
        return trustAllCerts;
    }

}
