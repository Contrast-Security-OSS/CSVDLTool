/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
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
 */

package com.contrastsecurity.preference;

import java.lang.reflect.Type;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.model.OrganizationJson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text apiKeyTxt;
    private Text serviceKeyTxt;
    private Text userNameTxt;
    private Text orgNameTxt;
    private Text orgIdTxt;

    public BasePreferencePage() {
        super("基本設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        new Label(composite, SWT.LEFT).setText("Contrast URL：");
        contrastUrlTxt = new Text(composite, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(preferenceStore.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });

        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(preferenceStore.getString(PreferenceConstants.API_KEY));
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });
        new Label(composite, SWT.LEFT).setText("Service Key：");
        serviceKeyTxt = new Text(composite, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));
        serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                serviceKeyTxt.selectAll();
            }
        });

        new Label(composite, SWT.LEFT).setText("Username：");
        userNameTxt = new Text(composite, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });

        Button getOrgBtn = new Button(composite, SWT.NULL);
        GridData getOrgBtnGrDt = new GridData();
        getOrgBtnGrDt.horizontalSpan = 2;
        getOrgBtnGrDt.horizontalAlignment = SWT.RIGHT;
        getOrgBtn.setLayoutData(getOrgBtnGrDt);
        getOrgBtn.setText("組織情報を取得");
        getOrgBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            public void widgetSelected(SelectionEvent event) {
                try {
                    Gson gson = new Gson();
                    HttpGet httpGet = new HttpGet(String.format("%s/api/ng/profile/organizations/default", contrastUrlTxt.getText()));
                    String auth = userNameTxt.getText() + ":" + serviceKeyTxt.getText();
                    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    String authHeader = new String(encodedAuth);
                    List<Header> headers = new ArrayList<Header>();
                    headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
                    headers.add(new BasicHeader("API-Key", apiKeyTxt.getText()));
                    headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
                    RequestConfig config = null;
                    CloseableHttpClient httpClient = null;
                    if (preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO)) {
                        HttpHost proxy = new HttpHost(preferenceStore.getString(PreferenceConstants.PROXY_HOST),
                                Integer.parseInt(preferenceStore.getString(PreferenceConstants.PROXY_PORT)));
                        config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).setProxy(proxy).build();
                        String proxy_user = preferenceStore.getString(PreferenceConstants.PROXY_USER);
                        String proxy_pass = preferenceStore.getString(PreferenceConstants.PROXY_PASS);
                        if (proxy_user.isEmpty() || proxy_pass.isEmpty()) {
                            httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                        } else {
                            CredentialsProvider credsProvider = new BasicCredentialsProvider();
                            credsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxy_user, proxy_pass));
                            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultHeaders(headers).build();
                        }
                    } else {
                        config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
                        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
                    }
                    httpGet.setConfig(config);
                    try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                        System.out.println(httpResponse.getStatusLine().getStatusCode());
                        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String jsonString = EntityUtils.toString(httpResponse.getEntity());
                            System.out.println(jsonString);
                            Type organizationType = new TypeToken<OrganizationJson>() {
                            }.getType();
                            OrganizationJson organizationJson = gson.fromJson(jsonString, organizationType);
                            System.out.println(organizationJson);
                            orgNameTxt.setText(organizationJson.getOrganization().getName());
                            orgIdTxt.setText(organizationJson.getOrganization().getOrganization_uuid());
                        } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                            String jsonString = EntityUtils.toString(httpResponse.getEntity());
                            System.out.println(jsonString);
                            MessageDialog.openError(composite.getShell(), "組織情報の取得", "401: 認証エラーです。");
                        } else {
                            System.out.println("200, 401以外のステータスコードが返却されました。");
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        new Label(composite, SWT.LEFT).setText("組織名：");
        orgNameTxt = new Text(composite, SWT.BORDER);
        orgNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgNameTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_NAME));
        orgNameTxt.setEditable(false);

        new Label(composite, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(composite, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_ID));
        orgIdTxt.setEditable(false);

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtnGrDt.horizontalSpan = 2;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
        applyBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });
        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.contrastUrlTxt != null) {
            ps.setValue(PreferenceConstants.CONTRAST_URL, this.contrastUrlTxt.getText());
        }
        if (this.apiKeyTxt != null) {
            ps.setValue(PreferenceConstants.API_KEY, this.apiKeyTxt.getText());
        }
        if (this.serviceKeyTxt != null) {
            ps.setValue(PreferenceConstants.SERVICE_KEY, this.serviceKeyTxt.getText());
        }
        if (this.userNameTxt != null) {
            ps.setValue(PreferenceConstants.USERNAME, this.userNameTxt.getText());
        }
        if (this.orgNameTxt != null) {
            ps.setValue(PreferenceConstants.ORG_NAME, this.orgNameTxt.getText());
        }
        if (this.orgIdTxt != null) {
            ps.setValue(PreferenceConstants.ORG_ID, this.orgIdTxt.getText());
        }
        return true;
    }
}
