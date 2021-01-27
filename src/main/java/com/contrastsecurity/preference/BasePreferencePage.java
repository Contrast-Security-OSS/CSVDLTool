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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import org.eclipse.swt.widgets.Label;
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

        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(preferenceStore.getString(PreferenceConstants.API_KEY));

        new Label(composite, SWT.LEFT).setText("Service Key：");
        serviceKeyTxt = new Text(composite, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));

        new Label(composite, SWT.LEFT).setText("Username：");
        userNameTxt = new Text(composite, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));

        Button mkDirBtn = new Button(composite, SWT.NULL);
        GridData mkDirBtnGrDt = new GridData();
        mkDirBtnGrDt.horizontalSpan = 2;
        mkDirBtnGrDt.horizontalAlignment = SWT.RIGHT;
        mkDirBtn.setLayoutData(mkDirBtnGrDt);
        mkDirBtn.setText("組織IDを取得");
        mkDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            public void widgetSelected(SelectionEvent event) {
                try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
                    Gson gson = new Gson();
                    RequestConfig config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
                    HttpGet httpGet = new HttpGet(String.format("%s/api/ng/profile/organizations/default", contrastUrlTxt.getText()));
                    String auth = userNameTxt.getText() + ":" + serviceKeyTxt.getText();
                    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    String authHeader = new String(encodedAuth);
                    httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                    httpGet.addHeader("API-Key", apiKeyTxt.getText());
                    httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
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
