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
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public abstract class Api {

    Logger logger = Logger.getLogger("csvdltool");

    protected IPreferenceStore preferenceStore;
    protected Organization organization;

    public Api(IPreferenceStore preferenceStore, Organization organization) {
        this.preferenceStore = preferenceStore;
        this.organization = organization;
    }

    public Object get() throws Exception {
        String response = this.getResponse();
        return this.convert(response);
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
        HttpGet httpGet = new HttpGet(url);
        List<Header> headers = this.getHeaders();
        for (Header header : headers) {
            httpGet.addHeader(header.getName(), header.getValue());
        }
        CloseableHttpClient httpClient = null;
        try {
            int connectTimeout = Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.CONNECTION_TIMEOUT));
            int sockettTimeout = Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.SOCKET_TIMEOUT));
            if (this.preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                HttpHost proxy = new HttpHost(this.preferenceStore.getString(PreferenceConstants.PROXY_HOST),
                        Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.PROXY_PORT)));
                httpGet.setConfig(RequestConfig.custom().setSocketTimeout(sockettTimeout).setConnectTimeout(connectTimeout).setProxy(proxy).build());
                if (this.preferenceStore.getString(PreferenceConstants.PROXY_AUTH).equals("none")) {
                    // プロキシ認証なし
                    if (preferenceStore.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                        httpClient = HttpClients.custom().setDefaultHeaders(headers).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        return true;
                                    }
                                }).build()).build();
                    } else {
                        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                    }
                } else {
                    // プロキシ認証あり
                    String proxy_user = null;
                    String proxy_pass = null;
                    if (this.preferenceStore.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                        proxy_user = this.preferenceStore.getString(PreferenceConstants.PROXY_TMP_USER);
                        proxy_pass = this.preferenceStore.getString(PreferenceConstants.PROXY_TMP_PASS);
                    } else {
                        proxy_user = this.preferenceStore.getString(PreferenceConstants.PROXY_USER);
                        BasicTextEncryptor encryptor = new BasicTextEncryptor();
                        encryptor.setPassword(Main.MASTER_PASSWORD);
                        try {
                            proxy_pass = encryptor.decrypt(preferenceStore.getString(PreferenceConstants.PROXY_PASS));
                        } catch (Exception e) {
                            throw new ApiException("プロキシパスワードの復号化に失敗しました。\\r\\nパスワードの設定をやり直してください。");
                        }
                    }
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxy_user, proxy_pass));
                    if (preferenceStore.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                        httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultHeaders(headers)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        return true;
                                    }
                                }).build()).build();
                    } else {
                        httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultHeaders(headers).build();
                    }
                }
            } else {
                httpGet.setConfig(RequestConfig.custom().setSocketTimeout(sockettTimeout).setConnectTimeout(connectTimeout).build());
                if (preferenceStore.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                    httpClient = HttpClients.custom().setDefaultHeaders(headers).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                    return true;
                                }
                            }).build()).build();
                } else {
                    httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                }
            }
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(httpResponse.getEntity());
                } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new ApiException(EntityUtils.toString(httpResponse.getEntity()));
                } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                    throw new ApiException(EntityUtils.toString(httpResponse.getEntity()));
                } else {
                    logger.warn(httpResponse.getStatusLine().getStatusCode());
                    logger.warn(EntityUtils.toString(httpResponse.getEntity()));
                    throw new NonApiException(String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                }
            } catch (Exception e) {
                throw e;
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
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException ioe) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                ioe.printStackTrace(printWriter);
                String trace = stringWriter.toString();
                logger.error(trace);
                throw ioe;
            }
        }
    }

}
