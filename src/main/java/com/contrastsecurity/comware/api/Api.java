package com.contrastsecurity.comware.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.comware.exception.ResponseException;
import com.contrastsecurity.preference.PreferenceConstants;

public abstract class Api {

    protected IPreferenceStore preferenceStore;

    public Api(IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
    }

    public Object get() throws Exception {
        String response = this.getResponse();
        return this.convert(response);
    }

    protected abstract String getUrl();

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

    protected abstract Object convert(String response);

    private String getResponse() throws Exception {
        HttpGet httpGet = new HttpGet(this.getUrl());
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
                    httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                } else {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxy_user, proxy_pass));
                    httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultHeaders(headers).build();
                }
            } else {
                httpGet.setConfig(RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build());
                httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
            }
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                System.out.println(httpResponse.getStatusLine().getStatusCode());
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(httpResponse.getEntity());
                } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new ResponseException(EntityUtils.toString(httpResponse.getEntity()));
                } else {
                    throw new ResponseException("200, 401以外のステータスコードが返却されました。");
                }
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
