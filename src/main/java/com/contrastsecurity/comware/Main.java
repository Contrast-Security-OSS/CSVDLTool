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

package com.contrastsecurity.comware;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.model.Application;
import com.contrastsecurity.model.ApplicationJson;
import com.contrastsecurity.model.ContrastSecurityYaml;
import com.contrastsecurity.model.HowToFixJson;
import com.contrastsecurity.model.NotesJson;
import com.contrastsecurity.model.StoryJson;
import com.contrastsecurity.model.TraceJson;
import com.contrastsecurity.model.TracesJson;
import com.contrastsecurity.preference.AboutPage;
import com.contrastsecurity.preference.BasePreferencePage;
import com.contrastsecurity.preference.PreferenceConstants;
import com.contrastsecurity.preference.ProxyPreferencePage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String ROOT_DIR = "sample";
    public static final String WINDOW_TITLE = "Comware";

    private ComwareShell shell;

    // TTL生成のみチェックボックス
    private Button onlyTtlGenChkBox;
    // 一括起動ボタン
    private Button bulkExecuteBtn;

    private PreferenceStore preferenceStore;

    // Diff取得識別子に指定できる文字定義
    public static String ACCEPTABLE_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

    private Map<String, Map<String, String>> optionInputsCache;

    private String loadDirErrorMsg;
    private String openingMsg;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();
        main.createPart();
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home");
            this.preferenceStore = new PreferenceStore(homeDir + "\\comware.properties");
            try {
                this.preferenceStore.load();
            } catch (FileNotFoundException fnfe) {
                this.preferenceStore = new PreferenceStore("comware.properties");
                this.preferenceStore.load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            System.out.println(contrastSecurityYaml);
            this.preferenceStore.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.preferenceStore.setDefault(PreferenceConstants.API_KEY, contrastSecurityYaml.getApiKey());
            this.preferenceStore.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.preferenceStore.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.optionInputsCache = new HashMap<String, Map<String, String>>();
    }

    private void createPart() {
        Display display = new Display();
        shell = new ComwareShell(display, this);
        shell.setText(String.format(WINDOW_TITLE));
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
            }

            @Override
            public void shellActivated(ShellEvent event) {
            }
        });

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.stateMask == SWT.CTRL) {
                    int num = Character.getNumericValue(event.character);
                    if (num > -1) {
                        support.firePropertyChange("userswitch", 0, num);
                    }
                }
            }
        };
        display.addFilter(SWT.KeyUp, listener);

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 10;
        shell.setLayout(baseLayout);

        if (loadDirErrorMsg != null && !loadDirErrorMsg.isEmpty()) {
            MessageDialog.openError(shell, "サーバ定義ロード", loadDirErrorMsg);
        }
        if (openingMsg != null && !openingMsg.isEmpty()) {
            MessageDialog.openInformation(shell, "ご利用ありがとうございます。", openingMsg);
        }

        // ========== 一括グループ ==========
        Composite bulkGrp = new Composite(shell, SWT.NULL);
        bulkGrp.setLayout(new GridLayout(1, false));
        GridData bulkGrpGrDt = new GridData(GridData.FILL_BOTH);
        bulkGrpGrDt.horizontalSpan = 3;
        // bulkGrpGrDt.widthHint = 100;
        bulkGrp.setLayoutData(bulkGrpGrDt);
        // bulkGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        // ========== 一括起動ボタン ==========
        bulkExecuteBtn = new Button(bulkGrp, SWT.PUSH);
        bulkExecuteBtn.setLayoutData(new GridData(GridData.FILL_BOTH));
        bulkExecuteBtn.setText("取得");
        bulkExecuteBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        bulkExecuteBtn.setToolTipText("対象サーバすべてに一括接続をします。");
        bulkExecuteBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
                    String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
                    String apiKey = preferenceStore.getString(PreferenceConstants.API_KEY);
                    String serviceKey = preferenceStore.getString(PreferenceConstants.SERVICE_KEY);
                    String userName = preferenceStore.getString(PreferenceConstants.USERNAME);
                    String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);

                    RequestConfig config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
                    HttpGet httpGet = new HttpGet(String.format("%s/api/ng/%s/applications?expand=modules,skip_links", contrastUrl, orgId));
                    String auth = userName + ":" + serviceKey;
                    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                    String authHeader = new String(encodedAuth);
                    httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                    httpGet.addHeader("API-Key", apiKey);
                    httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                    httpGet.setConfig(config);
                    try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String jsonString = EntityUtils.toString(httpResponse.getEntity());
                            // System.out.println(jsonString);
                            Gson gson = new Gson();
                            Type contType = new TypeToken<ApplicationJson>() {
                            }.getType();
                            ApplicationJson applicationJson = gson.fromJson(jsonString, contType);
                            // System.out.println(applicationJson);
                            for (Application app : applicationJson.getApplications()) {
                                if (!app.getApp_id().equals("dcaeafe3-cee2-421d-a6de-8215abf7672d")) {
                                    continue;
                                }
                                String url = String.format("%s/api/ng/%s/traces/%s/ids", contrastUrl, orgId, app.getApp_id());
                                httpGet = new HttpGet(url);
                                httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                                httpGet.addHeader("API-Key", apiKey);
                                httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                                httpGet.setConfig(config);
                                try (CloseableHttpResponse httpResponse2 = httpClient.execute(httpGet);) {
                                    if (httpResponse2.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                        String jsonString2 = EntityUtils.toString(httpResponse2.getEntity());
                                        Type tracesType = new TypeToken<TracesJson>() {
                                        }.getType();
                                        TracesJson tracesJson = gson.fromJson(jsonString2, tracesType);
                                        // System.out.println(tracesJson);
                                        for (String trace_id : tracesJson.getTraces()) {
                                            url = String.format("%s/api/ng/%s/traces/%s/trace/%s?expand=skip_links", contrastUrl, orgId, app.getApp_id(), trace_id);
                                            httpGet = new HttpGet(url);
                                            httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                                            httpGet.addHeader("API-Key", apiKey);
                                            httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                                            httpGet.setConfig(config);
                                            try (CloseableHttpResponse httpResponse3 = httpClient.execute(httpGet);) {
                                                if (httpResponse3.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                                    String jsonString3 = EntityUtils.toString(httpResponse3.getEntity());
                                                    // System.out.println(jsonString3);
                                                    Type traceType = new TypeToken<TraceJson>() {
                                                    }.getType();
                                                    TraceJson traceJson = gson.fromJson(jsonString3, traceType);
                                                    System.out.println(traceJson.getTrace());
                                                    // Story
                                                    url = String.format("%s/api/ng/%s/traces/%s/story", contrastUrl, orgId, trace_id);
                                                    httpGet = new HttpGet(url);
                                                    httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                                                    httpGet.addHeader("API-Key", apiKey);
                                                    httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                                                    httpGet.setConfig(config);
                                                    try (CloseableHttpResponse httpResponse4 = httpClient.execute(httpGet);) {
                                                        if (httpResponse4.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                                            String jsonString4 = EntityUtils.toString(httpResponse4.getEntity());
                                                            System.out.println(jsonString4);
                                                            Type storyType = new TypeToken<StoryJson>() {
                                                            }.getType();
                                                            StoryJson storyJson = gson.fromJson(jsonString4, storyType);
                                                            System.out.println(storyJson);
                                                        } else {
                                                            System.out.println("200以外のステータスコードが返却されました。");
                                                        }
                                                    } catch (Exception e) {
                                                        throw e;
                                                    }
                                                    // How to Fix
                                                    url = String.format("%s/api/ng/%s/traces/%s/recommendation", contrastUrl, orgId, trace_id);
                                                    httpGet = new HttpGet(url);
                                                    httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                                                    httpGet.addHeader("API-Key", apiKey);
                                                    httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                                                    httpGet.setConfig(config);
                                                    try (CloseableHttpResponse httpResponse5 = httpClient.execute(httpGet);) {
                                                        if (httpResponse5.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                                            String jsonString5 = EntityUtils.toString(httpResponse5.getEntity());
                                                            // System.out.println(jsonString5);
                                                            Type howToFixType = new TypeToken<HowToFixJson>() {
                                                            }.getType();
                                                            HowToFixJson howToFixJson = gson.fromJson(jsonString5, howToFixType);
                                                            // System.out.println(howToFixJson);
                                                        } else {
                                                            System.out.println("200以外のステータスコードが返却されました。");
                                                        }
                                                    } catch (Exception e) {
                                                        throw e;
                                                    }
                                                    // Comment
                                                    url = String.format("%s/api/ng/%s/applications/%s/traces/%s/notes", contrastUrl, orgId, app.getApp_id(), trace_id);
                                                    httpGet = new HttpGet(url);
                                                    httpGet.addHeader(HttpHeaders.ACCEPT, "application/json");
                                                    httpGet.addHeader("API-Key", apiKey);
                                                    httpGet.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
                                                    httpGet.setConfig(config);
                                                    try (CloseableHttpResponse httpResponse6 = httpClient.execute(httpGet);) {
                                                        if (httpResponse6.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                                            String jsonString6 = EntityUtils.toString(httpResponse6.getEntity());
                                                            // System.out.println(jsonString6);
                                                            Type notesType = new TypeToken<NotesJson>() {
                                                            }.getType();
                                                            NotesJson notesJson = gson.fromJson(jsonString6, notesType);
                                                            System.out.println(notesJson);
                                                        } else {
                                                            System.out.println("200以外のステータスコードが返却されました。");
                                                        }
                                                    } catch (Exception e) {
                                                        throw e;
                                                    }
                                                } else {
                                                    System.out.println("200以外のステータスコードが返却されました。");
                                                }
                                            } catch (Exception e) {
                                                throw e;
                                            }

                                        }
                                    } else {
                                        System.out.println("200以外のステータスコードが返却されました。");
                                    }
                                } catch (Exception e) {
                                    throw e;
                                }
                            }
                        } else {
                            System.out.println("200以外のステータスコードが返却されました。");
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        // ========== 設定ボタン ==========
        Button settingsBtn = new Button(bulkGrp, SWT.PUSH);
        settingsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingsBtn.setText("設定");
        settingsBtn.setToolTipText("動作に必要な設定を行います。");
        settingsBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage());
                PreferenceNode pathNode = new PreferenceNode("path", new ProxyPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addTo(baseNode.getId(), pathNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new PreferenceDialog(shell, mgr);
                dialog.setPreferenceStore(preferenceStore);
                dialog.open();
                try {
                    preferenceStore.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Logger logger = Logger.getLogger("comware");

        uiUpdate();
        shell.setSize(450, 300);
        shell.open();
        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
        }
        display.dispose();
    }

    private void uiUpdate() {
    }

    public PreferenceStore getPreferenceStore() {
        return preferenceStore;
    }

    public boolean isTtlOnly() {
        return this.onlyTtlGenChkBox.getSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("authInput".equals(event.getPropertyName())) {
            Boolean enableFlg = (Boolean) event.getNewValue();
            this.bulkExecuteBtn.setEnabled(enableFlg.booleanValue());
        } else if ("optionInputs".equals(event.getPropertyName())) {
            String oldValue = (String) event.getOldValue();
            String value = (String) event.getNewValue();
            String group = oldValue.split("/")[0];
            String name = oldValue.split("/")[1];
            if (this.optionInputsCache.containsKey(group)) {
                Map<String, String> inputs = this.optionInputsCache.get(group);
                inputs.put(name, value);
            } else {
                Map<String, String> inputs = new HashMap<String, String>();
                inputs.put(name, value);
                this.optionInputsCache.put(group, inputs);
            }
        }
    }

    /**
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
