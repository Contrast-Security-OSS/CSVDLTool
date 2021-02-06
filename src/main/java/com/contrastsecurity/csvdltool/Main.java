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

package com.contrastsecurity.csvdltool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ApplicationsApi;
import com.contrastsecurity.csvdltool.api.HowToFixApi;
import com.contrastsecurity.csvdltool.api.HttpRequestApi;
import com.contrastsecurity.csvdltool.api.RoutesApi;
import com.contrastsecurity.csvdltool.api.StoryApi;
import com.contrastsecurity.csvdltool.api.TraceApi;
import com.contrastsecurity.csvdltool.api.TracesApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.json.HowToFixJson;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
import com.contrastsecurity.csvdltool.model.HttpRequest;
import com.contrastsecurity.csvdltool.model.Note;
import com.contrastsecurity.csvdltool.model.Route;
import com.contrastsecurity.csvdltool.model.Story;
import com.contrastsecurity.csvdltool.model.Trace;
import com.contrastsecurity.csvdltool.preference.AboutPage;
import com.contrastsecurity.csvdltool.preference.BasePreferencePage;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.contrastsecurity.csvdltool.preference.ProxyPreferencePage;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "CSVDLTool";

    private CSVDLToolShell shell;

    private Button appLoadBtn;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private Button executeBtn;
    private Button includeDescChk;
    private Button settingBtn;

    private Map<String, String> fullAppMap = new TreeMap<String, String>();
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();

    private PreferenceStore preferenceStore;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = Logger.getLogger("csvdltool");

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
            this.preferenceStore = new PreferenceStore(homeDir + "\\csvdltool.properties");
            try {
                this.preferenceStore.load();
            } catch (FileNotFoundException fnfe) {
                this.preferenceStore = new PreferenceStore("csvdltool.properties");
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
            // System.out.println(contrastSecurityYaml);
            this.preferenceStore.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.preferenceStore.setDefault(PreferenceConstants.API_KEY, contrastSecurityYaml.getApiKey());
            this.preferenceStore.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.preferenceStore.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new CSVDLToolShell(display, this);
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png"));
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png"));
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png"));
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png"));
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png"));
        shell.setImages(imageArray);
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
                preferenceStore.setValue(PreferenceConstants.INCLUDE_DESCRIPTION, includeDescChk.getSelection());
                try {
                    preferenceStore.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
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
        baseLayout.marginWidth = 5;
        shell.setLayout(baseLayout);

        Group appListGrp = new Group(shell, SWT.NONE);
        appListGrp.setLayout(new GridLayout(3, false));
        appListGrp.setLayoutData(new GridData(GridData.FILL_BOTH));
        // appListGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        appLoadBtn = new Button(appListGrp, SWT.PUSH);
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("アプリ読み込み");
        appLoadBtn.addSelectionListener(new SelectionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent event) {
                Api applicationsApi = new ApplicationsApi(preferenceStore);
                try {
                    List<Application> applications = (List<Application>) applicationsApi.get();
                    srcList.removeAll();
                    srcApps.clear();
                    dstList.removeAll();
                    dstApps.clear();
                    fullAppMap.clear();
                    for (Application app : applications) {
                        srcList.add(app.getName());
                        srcApps.add(app.getName());
                        fullAppMap.put(app.getName(), app.getApp_id());
                    }
                    srcCount.setText(String.valueOf(srcList.getItemCount()));
                } catch (ApiException re) {
                    MessageDialog.openError(shell, "組織情報の取得", re.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageDialog.openError(shell, "組織情報の取得", e.getMessage());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Composite srcGrp = new Composite(appListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(1, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        Text srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage("Filter");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                String keyword = srcListFilter.getText();
                if (keyword.isEmpty()) {
                    srcList.removeAll();
                    srcApps.clear();
                    for (String appName : fullAppMap.keySet()) {
                        if (dstApps.contains(appName)) {
                            continue;
                        }
                        srcList.add(appName);
                        srcApps.add(appName);
                    }
                } else {
                    srcList.removeAll();
                    srcApps.clear();
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (dstApps.contains(appName)) {
                                continue;
                            }
                            srcList.add(appName);
                            srcApps.add(appName);
                        }
                    }
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
            }
        });
        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        this.srcList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.srcList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = srcList.getSelectionIndex();
                dstList.add(srcApps.get(idx));
                dstApps.add(srcApps.get(idx));
                srcList.remove(idx);
                srcApps.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.srcCount = new Label(srcGrp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.heightHint = 8;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(display, "ＭＳ ゴシック", 8, SWT.NORMAL));
        this.srcCount.setText("0");

        Composite btnGrp = new Composite(appListGrp, SWT.NONE);
        btnGrp.setLayout(new GridLayout(1, false));
        GridData btnGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        btnGrpGrDt.verticalAlignment = SWT.CENTER;
        btnGrp.setLayoutData(btnGrpGrDt);

        Button allRightBtn = new Button(btnGrp, SWT.PUSH);
        allRightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allRightBtn.setText(">>");
        allRightBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : srcApps) {
                    dstList.add(appName);
                    dstApps.add(appName);
                }
                srcList.removeAll();
                srcApps.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Button rightBtn = new Button(btnGrp, SWT.PUSH);
        rightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightBtn.setText(">");
        rightBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : srcList.getSelectionIndices()) {
                    dstList.add(srcApps.get(idx));
                    dstApps.add(srcApps.get(idx));
                }
                List<Integer> sortedList = Arrays.stream(srcList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    srcList.remove(idx.intValue());
                    srcApps.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Button leftBtn = new Button(btnGrp, SWT.PUSH);
        leftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftBtn.setText("<");
        leftBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : dstList.getSelectionIndices()) {
                    srcList.add(dstApps.get(idx));
                    srcApps.add(dstApps.get(idx));
                }
                List<Integer> sortedList = Arrays.stream(dstList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    dstList.remove(idx.intValue());
                    dstApps.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Button allLeftBtn = new Button(btnGrp, SWT.PUSH);
        allLeftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allLeftBtn.setText("<<");
        allLeftBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : dstApps) {
                    srcList.add(appName);
                    srcApps.add(appName);
                }
                dstList.removeAll();
                dstApps.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Composite dstGrp = new Composite(appListGrp, SWT.NONE);
        dstGrp.setLayout(new GridLayout(1, false));
        GridData dstGrpGrDt = new GridData(GridData.FILL_BOTH);
        dstGrp.setLayoutData(dstGrpGrDt);

        Text dstListFilter = new Text(dstGrp, SWT.BORDER);
        dstListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dstListFilter.setMessage("Filter");
        dstListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                String keyword = dstListFilter.getText();
                if (keyword.isEmpty()) {
                    dstList.removeAll();
                    dstApps.clear();
                    for (String appName : fullAppMap.keySet()) {
                        if (srcApps.contains(appName)) {
                            continue;
                        }
                        dstList.add(appName);
                        dstApps.add(appName);
                    }
                } else {
                    dstList.removeAll();
                    dstApps.clear();
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcApps.contains(appName)) {
                                continue;
                            }
                            dstList.add(appName);
                            dstApps.add(appName);
                        }
                    }
                }
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.dstList = new org.eclipse.swt.widgets.List(dstGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        this.dstList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.dstList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = dstList.getSelectionIndex();
                srcList.add(dstApps.get(idx));
                srcApps.add(dstApps.get(idx));
                dstList.remove(idx);
                dstApps.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.dstCount = new Label(dstGrp, SWT.RIGHT);
        this.dstCount.setFont(new Font(display, "ＭＳ ゴシック", 8, SWT.NORMAL));
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.heightHint = 8;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setText("0");

        // ========== 一括グループ ==========
        Group bulkGrp = new Group(shell, SWT.NULL);
        bulkGrp.setLayout(new GridLayout(1, false));
        GridData bulkGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // bulkGrpGrDt.horizontalSpan = 3;
        // bulkGrpGrDt.widthHint = 100;
        bulkGrp.setLayoutData(bulkGrpGrDt);

        // ========== 一括起動ボタン ==========
        executeBtn = new Button(bulkGrp, SWT.PUSH);
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.heightHint = 50;
        executeBtn.setLayoutData(executeBtnGrDt);
        executeBtn.setText("取得");
        executeBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        executeBtn.addSelectionListener(new SelectionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, "取得", "取得対象のアプリケーションを選択してください。");
                    return;
                }
                executeBtn.setEnabled(false);
                settingBtn.setEnabled(false);
                List<List<String>> csvList = new ArrayList<List<String>>();
                for (String appName : dstApps) {
                    String appId = fullAppMap.get(appName);
                    Api tracesApi = new TracesApi(preferenceStore, appId);
                    try {
                        List<String> traces = (List<String>) tracesApi.get();
                        for (String trace_id : traces) {
                            List<String> csvLineList = new ArrayList<String>();
                            Api traceApi = new TraceApi(preferenceStore, appId, trace_id);
                            Trace trace = (Trace) traceApi.get();
                            // ==================== 01. アプリケーション名 ====================
                            csvLineList.add(appName);
                            // ==================== 02. マージしたときの、各アプリ名称（可能であれば） ====================
                            csvLineList.add("");
                            // ==================== 03. （脆弱性の）カテゴリ ====================
                            csvLineList.add(trace.getCategory_label());
                            // ==================== 04. （脆弱性の）ルール ====================
                            csvLineList.add(trace.getRule_title());
                            // ==================== 05. 深刻度 ====================
                            csvLineList.add(trace.getSeverity_label());
                            // ==================== 06. ステータス ====================
                            csvLineList.add(trace.getStatus());
                            // ==================== 07. 言語（Javaなど） ====================
                            csvLineList.add(trace.getLanguage());
                            // ==================== 08. グループ（アプリケーションのグループ） ====================
                            csvLineList.add("");
                            // ==================== 09. 脆弱性のタイトル（例：SQLインジェクション：「/api/v1/approvers/」ページのリクエストボディ ） ====================
                            csvLineList.add("");
                            // ==================== 10. 最初の検出 ====================
                            csvLineList.add(trace.getFirst_time_seen());
                            // ==================== 11. 最後の検出 ====================
                            csvLineList.add(trace.getLast_time_seen());
                            // ==================== 12. ビルド番号 ====================
                            csvLineList.add(trace.getApp_version_tags());
                            // ==================== 13. 次のサーバにより報告 ====================
                            csvLineList.add("");
                            // ==================== 14. ルート ====================
                            Api routesApi = new RoutesApi(preferenceStore, appId, trace_id);
                            List<Route> routes = (List<Route>) routesApi.get();
                            List<String> signatureList = routes.stream().map(Route::getSignature).collect(Collectors.toList());
                            csvLineList.add(String.join(",", signatureList));
                            // ==================== 15. モジュール ====================
                            Application app = trace.getApplication();
                            String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                            csvLineList.add(module);
                            // ==================== 16. HTTP情報 ====================
                            Api httpRequestApi = new HttpRequestApi(preferenceStore, trace_id);
                            HttpRequest httpRequest = (HttpRequest) httpRequestApi.get();
                            if (httpRequest != null) {
                                csvLineList.add(httpRequest.getText());
                            } else {
                                csvLineList.add("");
                            }
                            if (includeDescChk.getSelection()) {
                                // ==================== 17. 何が起こったか？ ====================
                                csvLineList.add("");
                                // ==================== 18. どんなリスクであるか？ ====================
                                Api storyApi = new StoryApi(preferenceStore, trace_id);
                                Story story = (Story) storyApi.get();
                                csvLineList.add(story.getRisk().getText());
                                // ==================== 19. 修正方法 ====================
                                Api howToFixApi = new HowToFixApi(preferenceStore, trace_id);
                                HowToFixJson howToFixJson = (HowToFixJson) howToFixApi.get();
                                csvLineList.add(howToFixJson.getRecommendation().getText());
                            }
                            // ==================== 20(17). コメント(最後尾) ====================
                            for (Note note : trace.getNotes()) {
                                csvLineList.add(note.getNote());
                            }

                            csvList.add(csvLineList);
                        }
                    } catch (ApiException re) {
                        MessageDialog.openError(shell, "組織情報の取得", re.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageDialog.openError(shell, "組織情報の取得", e.getMessage());
                    }
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("out.csv")), "shift-jis"))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executeBtn.setEnabled(true);
                settingBtn.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        includeDescChk = new Button(bulkGrp, SWT.CHECK);
        includeDescChk.setText("何が起こったか？どんなリスクであるか？修正方法を含める");
        if (preferenceStore.getBoolean(PreferenceConstants.INCLUDE_DESCRIPTION)) {
            includeDescChk.setSelection(true);
        }

        // ========== 設定ボタン ==========
        settingBtn = new Button(shell, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText("設定");
        settingBtn.setToolTipText("動作に必要な設定を行います。");
        settingBtn.addSelectionListener(new SelectionListener() {
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

        Logger logger = Logger.getLogger("csvdltool");

        uiUpdate();
        shell.setSize(480, 360);
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

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("authInput".equals(event.getPropertyName())) {
            Boolean enableFlg = (Boolean) event.getNewValue();
            this.executeBtn.setEnabled(enableFlg.booleanValue());
        } else if ("optionInputs".equals(event.getPropertyName())) {
            String oldValue = (String) event.getOldValue();
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
