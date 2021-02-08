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

import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public abstract class Api {

    Logger logger = Logger.getLogger("csvdltool");

    protected IPreferenceStore preferenceStore;

    public Api(IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
    }

    public Object get() throws Exception {
        String response = this.getResponse();
        return this.convert(response);
    }

    protected abstract String getUrl();

    protected abstract Object convert(String response);

    protected List<Header> getHeaders() {
        String apiKey = preferenceStore.getString(PreferenceConstants.API_KEY);
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

    private String getResponse() throws Exception {
        String url = this.getUrl();
        logger.info(url);
        HttpGet httpGet = new HttpGet(url);
        List<Header> headers = this.getHeaders();
        CloseableHttpClient httpClient = null;
        try {
            if (this.preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                HttpHost proxy = new HttpHost(this.preferenceStore.getString(PreferenceConstants.PROXY_HOST),
                        Integer.parseInt(this.preferenceStore.getString(PreferenceConstants.PROXY_PORT)));
                httpGet.setConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).setProxy(proxy).build());
                String proxy_user = this.preferenceStore.getString(PreferenceConstants.PROXY_USER);
                String proxy_pass = this.preferenceStore.getString(PreferenceConstants.PROXY_PASS);
                if (proxy_user.isEmpty() || proxy_pass.isEmpty()) {
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
                        httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultHeaders(headers)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        return true;
                                    }
                                }).build()).build();
                    }
                }
            } else {
                httpGet.setConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build());
                if (preferenceStore.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
                    httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                } else {
                    httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                }
            }
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(httpResponse.getEntity());
                } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new ApiException(EntityUtils.toString(httpResponse.getEntity()));
                } else {
                    logger.warn(httpResponse.getStatusLine().getStatusCode());
                    logger.warn(EntityUtils.toString(httpResponse.getEntity()));
                    throw new ApiException("200, 401以外のステータスコードが返却されました。");
                }
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
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
