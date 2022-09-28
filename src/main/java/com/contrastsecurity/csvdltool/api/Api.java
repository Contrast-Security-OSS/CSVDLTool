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
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.csvdltool.BasicAuthStatusEnum;
import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.Main.AuthType;
import com.contrastsecurity.csvdltool.PasswordDialog;
import com.contrastsecurity.csvdltool.TsvDialog;
import com.contrastsecurity.csvdltool.TsvStatusEnum;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthCancelException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.BasicAuthFailureException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvCancelException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.exception.TsvFailureException;
import com.contrastsecurity.csvdltool.json.ContrastJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.TsvSettings;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import okhttp3.Authenticator;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public abstract class Api {

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    Logger logger = LogManager.getLogger("csvdltool");

    protected Shell shell;
    protected IPreferenceStore ps;
    protected Organization org;
    protected String contrastUrl;
    protected String userName;
    protected String serviceKey;
    protected boolean success;
    protected int totalCount;
    private List<Integer> ignoreStatusCodes;
    private String pass;
    private String code;
    private boolean jsonResponseFlg;
    private int authRetryMax;

    public Api(Shell shell, IPreferenceStore ps, Organization org) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.contrastUrl = this.ps.getString(PreferenceConstants.CONTRAST_URL);
        this.serviceKey = this.ps.getString(PreferenceConstants.SERVICE_KEY);
        this.userName = this.ps.getString(PreferenceConstants.USERNAME);
        if (((CSVDLToolShell) this.shell).getMain().getCookieJar() == null) {
            ((CSVDLToolShell) this.shell).getMain().setCookieJar(new MyCookieJar(this.contrastUrl));
        }
        this.ignoreStatusCodes = new ArrayList<Integer>();
        this.jsonResponseFlg = true;
        this.authRetryMax = this.ps.getInt(PreferenceConstants.AUTH_RETRY_MAX);
    }

    public Api(Shell shell, IPreferenceStore ps, Organization org, boolean jsonResponseFlg) {
        this(shell, ps, org);
        this.ignoreStatusCodes = new ArrayList<Integer>();
        this.jsonResponseFlg = jsonResponseFlg;
    }

    public void setIgnoreStatusCodes(List<Integer> ignoreStatusCodes) {
        this.ignoreStatusCodes = ignoreStatusCodes;
    }

    private void basicAuth(int retryCnt) throws Exception {
        BasicAuthStatusEnum basicAuthStatusEnum = BasicAuthStatusEnum.NONE;
        String basicAuthStatusEnumStr = this.ps.getString(PreferenceConstants.BASIC_AUTH_STATUS);
        if (basicAuthStatusEnumStr != null && !basicAuthStatusEnumStr.isEmpty()) {
            basicAuthStatusEnum = BasicAuthStatusEnum.valueOf(this.ps.getString(PreferenceConstants.BASIC_AUTH_STATUS));
        }
        if (BasicAuthStatusEnum.AUTH == basicAuthStatusEnum) {
            return;
        }

        boolean isNeedPassInput = false;
        if (this.ps.getString(PreferenceConstants.PASS_TYPE).equals("input")) {
            if (this.ps.getString(PreferenceConstants.PASSWORD).isEmpty()) {
                isNeedPassInput = true;
            }
        } else {
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(Main.MASTER_PASSWORD);
            try {
                this.pass = encryptor.decrypt(this.ps.getString(PreferenceConstants.PASSWORD));
            } catch (Exception e) {
                e.printStackTrace();
                throw new BasicAuthException("パスワードの復号化に失敗しました。\r\nパスワードの設定をやり直してください。");
            }
        }
        if (isNeedPassInput) {
            PasswordDialog passwordDialog = new PasswordDialog(shell, retryCnt);
            shell.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    int result = passwordDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        pass = "";
                    }
                    pass = passwordDialog.getPass();
                    if (pass == null) {
                        pass = "";
                    }
                }
            });
        }
        if (this.pass.isEmpty()) {
            throw new BasicAuthCancelException("認証をキャンセルしました。");
        }

        try {
            Api passwordAuthApi = new PasswordAuthApi(this.shell, this.ps, this.org, this.contrastUrl, this.userName, this.pass);
            String success = (String) passwordAuthApi.postWithoutCheckTsv();
            if (!Boolean.valueOf(success)) {
                throw new BasicAuthFailureException("認証に失敗しました。\r\nUsername, Passwordが正しいか再度ご確認ください。");
            }
            CookieJar cookieJar = ((CSVDLToolShell) this.shell).getMain().getCookieJar();
            List<Cookie> cookies = cookieJar.loadForRequest(HttpUrl.parse(ps.getString(PreferenceConstants.CONTRAST_URL)));
            String xsrf_token = null;
            for (Cookie c : cookies) {
                if (c.name().equals("XSRF-TOKEN")) {
                    xsrf_token = c.value();
                    this.ps.setValue(PreferenceConstants.XSRF_TOKEN, xsrf_token);
                    this.ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.AUTH.name());
                    break;
                }
            }
            cookieJar.saveFromResponse(null, cookies);
            shell.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    ((CSVDLToolShell) shell).getMain().loggedIn();
                }
            });
        } catch (Exception e) {
            if (e instanceof BasicAuthFailureException) {
                throw e;
            }
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
            if (e instanceof UnknownHostException) {
                throw new BasicAuthException(String.format("ホストが見つかりません。\r\n%s", e.getMessage()));
            }
            throw new BasicAuthException(String.format("%s\r\nエラーの詳細はログファイルでご確認ください。", e.getMessage()));
        }
    }

    protected TsvSettings checkTsv() throws Exception {
        Api tsvSettingsApi = null;
        if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
            tsvSettingsApi = new TsvSettingsApi(shell, ps, org, this.contrastUrl, this.userName);
        } else {
            tsvSettingsApi = new TsvSettingsApi(shell, ps, org, this.contrastUrl, this.userName, this.serviceKey);
        }
        try {
            TsvSettings tsvSettings = (TsvSettings) tsvSettingsApi.getWithoutCheckTsv();
            return tsvSettings;
        } catch (TsvException e) {
            Gson gson = new Gson();
            Type contrastType = new TypeToken<ContrastJson>() {
            }.getType();
            ContrastJson contrastJson = gson.fromJson(e.getMessage(), contrastType);
            if (contrastJson.getSuccess().equals("false") && contrastJson.getMessages().contains("TSV code required")) {
                TsvSettings ts = new TsvSettings();
                ts.setTsv_enabled(true);
                ts.setTsv_type("EMAIL");
                return ts;
            }
        } catch (ApiException e) {
            throw new TsvException(String.format("TeamServerからエラーが返されました。\r\n%s", e.getMessage()));
        } catch (NonApiException e) {
            throw new TsvException(String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", e.getMessage()));
        } catch (Exception e) {
            throw new TsvException(String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", e.getMessage()));
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void tsvCheck(int retryCnt) throws Exception {
        TsvStatusEnum tsvStatusEnum = TsvStatusEnum.NONE;
        String tsvStatusEnumStr = this.ps.getString(PreferenceConstants.TSV_STATUS);
        if (tsvStatusEnumStr != null && !tsvStatusEnumStr.isEmpty()) {
            tsvStatusEnum = TsvStatusEnum.valueOf(this.ps.getString(PreferenceConstants.TSV_STATUS));
        }
        if (TsvStatusEnum.NONE == tsvStatusEnum) {
            TsvSettings tsvSettings = checkTsv();
            if (!tsvSettings.isTsv_enabled()) {
                this.ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.SKIP.name());
            } else {
                if (tsvSettings.getTsv_type() == null) {
                    throw new TsvException("二段階認証コードの通知方法の取得に失敗しました。\r\n二段階認証の設定に問題がないかご確認ください。");
                }
                if (tsvSettings.getTsv_type().equals("EMAIL")) {
                    Api tsvInitializeApi = null;
                    if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
                        tsvInitializeApi = new TsvInitializeApi(this.shell, this.ps, this.org, this.contrastUrl, this.userName);
                    } else {
                        tsvInitializeApi = new TsvInitializeApi(this.shell, this.ps, this.org, this.contrastUrl, this.userName, this.serviceKey);
                    }
                    String rtnMsg = (String) tsvInitializeApi.postWithoutCheckTsv();
                    if (!rtnMsg.equals("true")) {
                        throw new TsvException("二段階認証コードのメール送信要求に失敗しました。");
                    }
                }
                TsvDialog tsvDialog = new TsvDialog(shell, retryCnt);
                shell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        int result = tsvDialog.open();
                        if (IDialogConstants.OK_ID != result) {
                            code = "";
                        }
                        code = tsvDialog.getCode();
                        if (code == null) {
                            code = "";
                        }
                    }
                });
                if (!code.isEmpty()) {
                    Api tsvAuthorizeApi = new TsvAuthorizeApi(this.shell, this.ps, this.org, this.contrastUrl, this.userName, this.serviceKey, code);
                    tsvAuthorizeApi.setIgnoreStatusCodes(new ArrayList(Arrays.asList(400)));
                    try {
                        String rtnMsg = (String) tsvAuthorizeApi.postWithoutCheckTsv();
                        if (rtnMsg.equals("true")) {
                            this.ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.AUTH.name());
                        } else {
                            throw new TsvFailureException("二段階認証に失敗しました。");
                        }
                    } catch (NonApiException nae) {
                        if (nae.getMessage().equals("400")) {
                            throw new TsvException("二段階認証に失敗しました。");
                        }
                    }
                } else {
                    throw new TsvCancelException("二段階認証をキャンセルしました。");
                }
            }
        }
    }

    public Object getWithoutCheckTsv() throws Exception {
        String response = this.getResponse(HttpMethod.GET);
        return this.convert(response);
    }

    public Object get() throws Exception {
        if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
            for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                try {
                    basicAuth(cnt);
                    break;
                } catch (BasicAuthFailureException bafe) {
                    continue;
                }
            }
            try {
                for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                    try {
                        tsvCheck(cnt);
                        break;
                    } catch (TsvFailureException tfe) {
                        continue;
                    }
                }
            } catch (TsvException tsve) {
                shell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        ((CSVDLToolShell) shell).getMain().logOut();
                    }
                });
                throw tsve;
            }
        } else {
            for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                try {
                    tsvCheck(cnt);
                    break;
                } catch (TsvFailureException tfe) {
                    continue;
                }
            }
        }
        String response = this.getResponse(HttpMethod.GET);
        return this.convert(response);
    }

    public Object postWithoutCheckTsv() throws Exception {
        String response = this.getResponse(HttpMethod.POST);
        return this.convert(response);
    }

    public Object post() throws Exception {
        if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
            for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                try {
                    basicAuth(cnt);
                    break;
                } catch (BasicAuthFailureException bafe) {
                    continue;
                }
            }
            try {
                for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                    try {
                        tsvCheck(cnt);
                        break;
                    } catch (TsvFailureException tfe) {
                        continue;
                    }
                }
            } catch (TsvException tsve) {
                shell.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        ((CSVDLToolShell) shell).getMain().logOut();
                    }
                });
                throw tsve;
            }
        } else {
            for (int cnt = 0; cnt < this.authRetryMax; cnt++) {
                try {
                    tsvCheck(cnt);
                    break;
                } catch (TsvFailureException tfe) {
                    continue;
                }
            }
        }
        String response = this.getResponse(HttpMethod.POST);
        return this.convert(response);
    }

    public Object put() throws Exception {
        String response = this.getResponse(HttpMethod.PUT);
        return this.convert(response);
    }

    public Object delete() throws Exception {
        String response = this.getResponse(HttpMethod.DELETE);
        return this.convert(response);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getTotalCount() {
        return totalCount;
    }

    protected abstract String getUrl();

    protected abstract Object convert(String response);

    protected List<Header> getHeaders() {
        List<Header> headers = new ArrayList<Header>();
        if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
            headers.add(new BasicHeader("X-XSRF-TOKEN", ps.getString(PreferenceConstants.XSRF_TOKEN)));
        } else {
            String apiKey = this.org.getApikey();
            String auth = String.format("%s:%s", this.userName, this.serviceKey);
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = new String(encodedAuth);
            headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
            headers.add(new BasicHeader("API-Key", apiKey));
            headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
        }
        return headers;
    }

    protected RequestBody getBody() throws Exception {
        return null;
    }

    protected String getResponse(HttpMethod httpMethod) throws Exception {
        String url = this.getUrl();
        logger.trace(url);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
            clientBuilder.cookieJar(((CSVDLToolShell) this.shell).getMain().getCookieJar());
        }
        Request.Builder requestBuilder = null;
        switch (httpMethod) {
            case POST:
                requestBuilder = new Request.Builder().url(url).post(getBody());
                break;
            case PUT:
                requestBuilder = new Request.Builder().url(url).put(getBody());
                break;
            case DELETE:
                requestBuilder = new Request.Builder().url(url).delete(getBody());
                break;
            default:
                requestBuilder = new Request.Builder().url(url).get();
        }
        List<Header> headers = this.getHeaders();
        for (Header header : headers) {
            requestBuilder.addHeader(header.getName(), header.getValue());
        }
        OkHttpClient httpClient = null;
        Request request = requestBuilder.build();
        Response response = null;
        try {
            int connectTimeout = Integer.parseInt(this.ps.getString(PreferenceConstants.CONNECTION_TIMEOUT));
            int sockettTimeout = Integer.parseInt(this.ps.getString(PreferenceConstants.SOCKET_TIMEOUT));
            clientBuilder.readTimeout(sockettTimeout, TimeUnit.MILLISECONDS).connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

            if (this.ps.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
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

            if (this.ps.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                clientBuilder.proxy(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(this.ps.getString(PreferenceConstants.PROXY_HOST), Integer.parseInt(this.ps.getString(PreferenceConstants.PROXY_PORT)))));
                if (!this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("none")) {
                    Authenticator proxyAuthenticator = null;
                    // プロキシ認証あり
                    if (this.ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                        proxyAuthenticator = new Authenticator() {

                            @Override
                            public Request authenticate(Route route, Response response) throws IOException {
                                String credential = Credentials.basic(ps.getString(PreferenceConstants.PROXY_TMP_USER), ps.getString(PreferenceConstants.PROXY_TMP_PASS));
                                return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                            }
                        };
                    } else {

                        BasicTextEncryptor encryptor = new BasicTextEncryptor();
                        encryptor.setPassword(Main.MASTER_PASSWORD);
                        try {
                            String proxy_pass = encryptor.decrypt(this.ps.getString(PreferenceConstants.PROXY_PASS));
                            proxyAuthenticator = new Authenticator() {
                                @Override
                                public Request authenticate(Route route, Response response) throws IOException {
                                    String credential = Credentials.basic(ps.getString(PreferenceConstants.PROXY_USER), proxy_pass);
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
                response = httpClient.newCall(request).execute();
                if (response.code() == 200 || this.ignoreStatusCodes.contains(response.code())) {
                    String res = response.body().string();
                    if (jsonResponseFlg) {
                        try {
                            Gson gson = new Gson();
                            Type contrastType = new TypeToken<ContrastJson>() {
                            }.getType();
                            gson.fromJson(res, contrastType);
                        } catch (JsonSyntaxException jse) {
                            shell.getDisplay().syncExec(new Runnable() {
                                @Override
                                public void run() {
                                    ((CSVDLToolShell) shell).getMain().loggedOut();
                                }
                            });
                            // ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
                            // ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.NONE.name());
                            // ps.setValue(PreferenceConstants.XSRF_TOKEN, "");
                            throw new ApiException("認証が必要です。もう一度実行してください。");
                        }
                    }
                    return res;
                } else if (response.code() == 303) {
                    throw new TsvException(response.body().string());
                } else if (response.code() == 400) {
                    throw new ApiException(response.body().string());
                } else if (response.code() == 401 || response.code() == 403) {
                    if (((CSVDLToolShell) this.shell).getMain().getAuthType() == AuthType.PASSWORD) {
                        Gson gson = new Gson();
                        Type contrastType = new TypeToken<ContrastJson>() {
                        }.getType();
                        ContrastJson contrastJson = gson.fromJson(response.body().string(), contrastType);
                        if (contrastJson.getSuccess().equals("false")) {
                            if (contrastJson.getMessages().contains("Invalid CSRF token") || contrastJson.getMessages().contains("Authorization failure")) {
                                shell.getDisplay().syncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((CSVDLToolShell) shell).getMain().loggedOut();
                                    }
                                });
                                // ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
                                // ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.NONE.name());
                                // ps.setValue(PreferenceConstants.XSRF_TOKEN, "");
                                throw new ApiException("認証が必要です。もう一度実行してください。");
                            } else {
                                throw new BasicAuthFailureException("認証に失敗しました。\r\nUsername, Passwordが正しいか再度ご確認ください。");
                            }
                        }
                    }
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
            if (e instanceof TsvException || e instanceof BasicAuthException) {
                throw e;
            }
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
