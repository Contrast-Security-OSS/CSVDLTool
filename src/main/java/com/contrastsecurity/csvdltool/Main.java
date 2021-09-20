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

package com.contrastsecurity.csvdltool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
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

import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.AboutPage;
import com.contrastsecurity.csvdltool.preference.BasePreferencePage;
import com.contrastsecurity.csvdltool.preference.CSVPreferencePage;
import com.contrastsecurity.csvdltool.preference.ConnectionPreferencePage;
import com.contrastsecurity.csvdltool.preference.LibCSVColumnPreferencePage;
import com.contrastsecurity.csvdltool.preference.MyPreferenceDialog;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.contrastsecurity.csvdltool.preference.VulCSVColumnPreferencePage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "CSVDLTool - %s";
    // 以下のMASTER_PASSWORDはプロキシパスワードを保存する際に暗号化で使用するパスワードです。
    // 本ツールをリリース用にコンパイルする際はchangemeを別の文字列に置き換えてください。
    public static final String MASTER_PASSWORD = "changeme!";

    private CSVDLToolShell shell;

    private Button appLoadBtn;
    private Text srcListFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private CTabFolder tabFolder;

    private Button vulExecuteBtn;
    private Button vulOnlyParentAppChk;
    private Button includeDescChk;
    private Button includeStackTraceChk;

    private Button libExecuteBtn;
    private Button onlyHasCVEChk;
    private Button includeCVEDetailChk;

    private Button settingBtn;

    private Text vulSeverityFilterTxt;
    private Text vulVulnTypeFilterTxt;
    private Text vulLastDetectedFilterTxt;

    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> filterMap;
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();

    private PreferenceStore preferenceStore;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = Logger.getLogger("csvdltool");

    String currentTitle;

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
            this.preferenceStore.setDefault(PreferenceConstants.PROXY_AUTH, "none");
            this.preferenceStore.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.preferenceStore.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);

            this.preferenceStore.setDefault(PreferenceConstants.CSV_COLUMN_VUL, VulCSVColmunEnum.defaultValuesStr());
            this.preferenceStore.setDefault(PreferenceConstants.SLEEP_VUL, 300);
            this.preferenceStore.setDefault(PreferenceConstants.CSV_OUT_HEADER_VUL, true);
            this.preferenceStore.setDefault(PreferenceConstants.CSV_FILE_FORMAT_VUL, "'vul'_yyyy-MM-dd_HHmmss");

            this.preferenceStore.setDefault(PreferenceConstants.CSV_COLUMN_LIB, LibCSVColmunEnum.defaultValuesStr());
            this.preferenceStore.setDefault(PreferenceConstants.SLEEP_LIB, 300);
            this.preferenceStore.setDefault(PreferenceConstants.CSV_OUT_HEADER_LIB, true);
            this.preferenceStore.setDefault(PreferenceConstants.CSV_FILE_FORMAT_LIB, "'lib'_yyyy-MM-dd_HHmmss");
            this.preferenceStore.setDefault(PreferenceConstants.OPENED_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.preferenceStore.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.preferenceStore.setDefault(PreferenceConstants.API_KEY, contrastSecurityYaml.getApiKey());
            this.preferenceStore.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.preferenceStore.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new CSVDLToolShell(display, this);
        shell.setMinimumSize(640, 600);
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png"));
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png"));
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png"));
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png"));
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png"));
        shell.setImages(imageArray);
        Window.setDefaultImages(imageArray);
        setWindowTitle();
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
                int idx = tabFolder.getSelectionIndex();
                preferenceStore.setValue(PreferenceConstants.OPENED_TAB_IDX, idx);
                preferenceStore.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                preferenceStore.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                preferenceStore.setValue(PreferenceConstants.VUL_ONLY_PARENT_APP, vulOnlyParentAppChk.getSelection());
                preferenceStore.setValue(PreferenceConstants.INCLUDE_DESCRIPTION, includeDescChk.getSelection());
                preferenceStore.setValue(PreferenceConstants.INCLUDE_STACKTRACE, includeStackTraceChk.getSelection());
                preferenceStore.setValue(PreferenceConstants.ONLY_HAS_CVE, onlyHasCVEChk.getSelection());
                preferenceStore.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
                preferenceStore.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                preferenceStore.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
                try {
                    preferenceStore.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                List<Organization> orgs = getValidOrganizations();
                if (orgs.isEmpty()) {
                    appLoadBtn.setEnabled(false);
                    vulExecuteBtn.setEnabled(false);
                    settingBtn.setText("このボタンから基本設定を行ってください。");
                    currentTitle = "";
                    uiReset();
                } else {
                    appLoadBtn.setEnabled(true);
                    vulExecuteBtn.setEnabled(true);
                    settingBtn.setText("設定");
                    List<String> orgNameList = new ArrayList<String>();
                    String title = String.join(", ", orgNameList);
                    if (currentTitle != null && !currentTitle.equals(title)) {
                        uiReset();
                        currentTitle = title;
                    }
                }
                setWindowTitle();
                if (preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO) && preferenceStore.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String usr = preferenceStore.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = preferenceStore.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            preferenceStore.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            preferenceStore.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            preferenceStore.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
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
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 8;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        Group appListGrp = new Group(shell, SWT.NONE);
        appListGrp.setLayout(new GridLayout(3, false));
        GridData appListGrpGrDt = new GridData(GridData.FILL_BOTH);
        appListGrpGrDt.minimumHeight = 200;
        appListGrp.setLayoutData(appListGrpGrDt);
        // appListGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        appLoadBtn = new Button(appListGrp, SWT.PUSH);
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("アプリケーション一覧の読み込み");
        appLoadBtn.setToolTipText("TeamServerにオンボードされているアプリケーションを読み込みます。");
        appLoadBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                uiReset();

                AppsGetWithProgress progress = new AppsGetWithProgress(preferenceStore, getValidOrganizations());
                ProgressMonitorDialog progDialog = new AppGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    logger.error(trace);
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "アプリケーション一覧の取得", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "アプリケーション一覧の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else {
                        MessageDialog.openError(shell, "アプリケーション一覧の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullAppMap = progress.getFullAppMap();
                if (fullAppMap.isEmpty()) {
                    String userName = preferenceStore.getString(PreferenceConstants.USERNAME);
                    StringJoiner sj = new StringJoiner("\r\n");
                    sj.add("アプリケーションの取得件数が０件です。考えられる原因としては以下となります。");
                    sj.add("・下記ユーザーのアプリケーションアクセスグループにView権限が設定されていない。");
                    sj.add(String.format("　%s", userName));
                    sj.add("・Assessライセンスが付与されているアプリケーションがない。");
                    sj.add("・接続設定が正しくない。プロキシの設定がされていない。など");
                    MessageDialog.openInformation(shell, "アプリケーション一覧の取得", sj.toString());
                }
                for (String appLabel : fullAppMap.keySet()) {
                    srcList.add(appLabel); // UI list
                    srcApps.add(appLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                filterMap = progress.getFilterMap();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        Composite srcGrp = new Composite(appListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(1, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage("Filter...");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcList.removeAll(); // UI List src
                srcApps.clear(); // memory src
                if (fullAppMap == null) {
                    srcCount.setText(String.valueOf(srcList.getItemCount()));
                    return;
                }
                String keyword = srcListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appLabel : fullAppMap.keySet()) {
                        if (dstApps.contains(appLabel)) {
                            continue; // 既に選択済みのアプリはスキップ
                        }
                        srcList.add(appLabel); // UI List src
                        srcApps.add(appLabel); // memory src
                    }
                } else {
                    for (String appLabel : fullAppMap.keySet()) {
                        if (appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                            if (dstApps.contains(appLabel)) {
                                continue; // 既に選択済みのアプリはスキップ
                            }
                            srcList.add(appLabel);
                            srcApps.add(appLabel);
                        }
                    }
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
            }
        });
        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.srcList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.srcList.setToolTipText("選択可能なアプリケーション一覧");
        this.srcList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = srcList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
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
                    String appName = srcApps.get(idx);
                    String keyword = dstListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        dstList.add(appName);
                        dstApps.add(appName);
                    }
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
                    String appName = dstApps.get(idx);
                    String keyword = srcListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        srcList.add(appName);
                        srcApps.add(appName);
                    }
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

        dstListFilter = new Text(dstGrp, SWT.BORDER);
        dstListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dstListFilter.setMessage("Filter...");
        dstListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                dstList.removeAll(); // UI List dst
                dstApps.clear(); // memory dst
                if (fullAppMap == null) {
                    dstCount.setText(String.valueOf(dstList.getItemCount()));
                    return;
                }
                String keyword = dstListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appName : fullAppMap.keySet()) {
                        if (srcApps.contains(appName)) {
                            continue; // 選択可能にあるアプリはスキップ
                        }
                        dstList.add(appName); // UI List dst
                        dstApps.add(appName); // memory dst
                    }
                } else {
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcApps.contains(appName)) {
                                continue; // 選択可能にあるアプリはスキップ
                            }
                            dstList.add(appName); // UI List dst
                            dstApps.add(appName); // memory dst
                        }
                    }
                }
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.dstList = new org.eclipse.swt.widgets.List(dstGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.dstList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.dstList.setToolTipText("選択済みのアプリケーション一覧");
        this.dstList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = dstList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
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

        tabFolder = new CTabFolder(shell, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_HORIZONTAL);
        tabFolder.setLayoutData(tabFolderGrDt);
        tabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        // #################### 脆弱性 #################### //
        CTabItem vulTabItem = new CTabItem(tabFolder, SWT.NONE);
        vulTabItem.setText("脆弱性");

        // ========== グループ ==========
        Composite vulButtonGrp = new Composite(tabFolder, SWT.NULL);
        GridLayout buttonGrpLt = new GridLayout(1, false);
        buttonGrpLt.marginWidth = 10;
        buttonGrpLt.marginHeight = 10;
        vulButtonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // buttonGrpGrDt.horizontalSpan = 3;
        // buttonGrpGrDt.widthHint = 100;
        vulButtonGrp.setLayoutData(buttonGrpGrDt);

        Group vulFilterGrp = new Group(vulButtonGrp, SWT.NONE);
        GridLayout vulFilterGrpLt = new GridLayout(2, false);
        vulFilterGrpLt.marginWidth = 10;
        vulFilterGrpLt.marginHeight = 10;
        // vulFilterGrpLt.horizontalSpacing = 10;
        // vulFilterGrpLt.verticalSpacing = 10;
        vulFilterGrp.setLayout(vulFilterGrpLt);
        GridData vulFilterGrpGrDt = new GridData(GridData.FILL_BOTH);
        vulFilterGrp.setLayoutData(vulFilterGrpGrDt);
        vulFilterGrp.setText("フィルタ条件");

        new Label(vulFilterGrp, SWT.LEFT).setText("重大度：");
        vulSeverityFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulSeverityFilterTxt.setText("すべて");
        vulSeverityFilterTxt.setEditable(false);
        vulSeverityFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSeverityFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (filterMap != null && filterMap.containsKey(FilterEnum.SEVERITY)) {
                    FilterSeverityDialog filterDialog = new FilterSeverityDialog(shell, filterMap.get(FilterEnum.SEVERITY));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : filterMap.get(FilterEnum.SEVERITY)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulSeverityFilterTxt.setText("すべて");
                    } else {
                        vulSeverityFilterTxt.setText(String.join(", ", labels));
                    }
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText("脆弱性タイプ：");
        vulVulnTypeFilterTxt = new Text(vulFilterGrp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        vulVulnTypeFilterTxt.setText("すべて");
        vulVulnTypeFilterTxt.setEditable(false);
        GridData vulVulnTypeFilterTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        vulVulnTypeFilterTxtGrDt.heightHint = 2 * vulVulnTypeFilterTxt.getLineHeight();
        vulVulnTypeFilterTxt.setLayoutData(vulVulnTypeFilterTxtGrDt);
        vulVulnTypeFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (filterMap != null && filterMap.containsKey(FilterEnum.VULNTYPE)) {
                    FilterVulnTypeDialog filterDialog = new FilterVulnTypeDialog(shell, filterMap.get(FilterEnum.VULNTYPE));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : filterMap.get(FilterEnum.VULNTYPE)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulVulnTypeFilterTxt.setText("すべて");
                    } else {
                        vulVulnTypeFilterTxt.setText(String.join(", ", labels));
                    }
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText("最終検出日：");
        vulLastDetectedFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulLastDetectedFilterTxt.setText("すべて");
        vulLastDetectedFilterTxt.setEditable(false);
        vulLastDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulLastDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(shell);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                vulLastDetectedFilterTxt.setText(String.format("%s ～ %s", sdf.format(filterDialog.getFrDate()), sdf.format(filterDialog.getToDate())));
            }
        });

        // ========== 取得ボタン ==========
        vulExecuteBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.heightHint = 50;
        vulExecuteBtn.setLayoutData(executeBtnGrDt);
        vulExecuteBtn.setText("取得");
        vulExecuteBtn.setToolTipText("脆弱性情報を取得し、CSV形式で出力します。");
        vulExecuteBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        vulExecuteBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, "脆弱性情報取得", "取得対象のアプリケーションを選択してください。");
                    return;
                }
                VulGetWithProgress progress = new VulGetWithProgress(shell, preferenceStore, dstApps, fullAppMap, filterMap, vulOnlyParentAppChk.getSelection(),
                        includeDescChk.getSelection(), includeStackTraceChk.getSelection());
                ProgressMonitorDialog progDialog = new VulGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    logger.error(trace);
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "脆弱性情報の取得", String.format("TeamServerからエラーが返されました。\r\n%s", exceptionMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "脆弱性情報の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", exceptionMsg));
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, "脆弱性情報の取得", exceptionMsg);
                    } else {
                        MessageDialog.openError(shell, "脆弱性情報の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", exceptionMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        vulOnlyParentAppChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyParentAppChk.setText("マージされたアプリの場合、親アプリの脆弱性だけを出力する。");
        if (preferenceStore.getBoolean(PreferenceConstants.VUL_ONLY_PARENT_APP)) {
            vulOnlyParentAppChk.setSelection(true);
        }

        includeDescChk = new Button(vulButtonGrp, SWT.CHECK);
        includeDescChk.setText("改行を含む長文の項目（ルート、HTTP情報、修正方法、コメントなど）も添付ファイルで出力する。（フォルダ出力）");
        includeDescChk.setToolTipText("ルート、HTTP情報、コメント、何が起こったか？、どんなリスクであるか？、修正方法の５つの項目が添付ファイルで出力されます。");
        if (preferenceStore.getBoolean(PreferenceConstants.INCLUDE_DESCRIPTION)) {
            includeDescChk.setSelection(true);
        }
        includeDescChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button chk = (Button) e.getSource();
                if (!chk.getSelection()) {
                    includeStackTraceChk.setSelection(false);
                }
            }
        });

        includeStackTraceChk = new Button(vulButtonGrp, SWT.CHECK);
        includeStackTraceChk.setText("脆弱性の詳細（スタックトレース）も添付ファイルで出力する。（フォルダ出力）");
        if (preferenceStore.getBoolean(PreferenceConstants.INCLUDE_STACKTRACE)) {
            includeStackTraceChk.setSelection(true);
        }
        includeStackTraceChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button chk = (Button) e.getSource();
                if (chk.getSelection()) {
                    includeDescChk.setSelection(true);
                }
            }
        });
        vulTabItem.setControl(vulButtonGrp);

        // #################### ライブラリ #################### //
        CTabItem libTabItem = new CTabItem(tabFolder, SWT.NONE);
        libTabItem.setText("ライブラリ");

        // ========== グループ ==========
        Composite libButtonGrp = new Composite(tabFolder, SWT.NULL);
        GridLayout libButtonGrpLt = new GridLayout(1, false);
        libButtonGrpLt.marginWidth = 10;
        libButtonGrpLt.marginHeight = 10;
        libButtonGrp.setLayout(libButtonGrpLt);
        GridData libButtonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // libButtonGrpGrDt.horizontalSpan = 3;
        // libButtonGrpGrDt.widthHint = 100;
        libButtonGrp.setLayoutData(libButtonGrpGrDt);

        // ========== 取得ボタン ==========
        libExecuteBtn = new Button(libButtonGrp, SWT.PUSH);
        GridData libExecuteBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        libExecuteBtnGrDt.heightHint = 50;
        libExecuteBtn.setLayoutData(libExecuteBtnGrDt);
        libExecuteBtn.setText("取得");
        libExecuteBtn.setToolTipText("ライブラリ情報を取得し、CSV形式で出力します。");
        libExecuteBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        libExecuteBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, "ライブラリ情報取得", "取得対象のアプリケーションを選択してください。");
                    return;
                }
                LibGetWithProgress progress = new LibGetWithProgress(shell, preferenceStore, dstApps, fullAppMap, onlyHasCVEChk.getSelection(), includeCVEDetailChk.getSelection());
                ProgressMonitorDialog progDialog = new LibGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    logger.error(trace);
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "ライブラリ情報の取得", String.format("TeamServerからエラーが返されました。\r\n%s", exceptionMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "ライブラリ情報の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", exceptionMsg));
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, "ライブラリ情報の取得", exceptionMsg);
                    } else {
                        MessageDialog.openError(shell, "ライブラリ情報の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", exceptionMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        onlyHasCVEChk = new Button(libButtonGrp, SWT.CHECK);
        onlyHasCVEChk.setText("CVE（脆弱性）を含むライブラリのみ出力する。");
        if (preferenceStore.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            onlyHasCVEChk.setSelection(true);
        }
        includeCVEDetailChk = new Button(libButtonGrp, SWT.CHECK);
        includeCVEDetailChk.setText("CVEの詳細情報も出力する。（フォルダ出力）");
        includeCVEDetailChk.setToolTipText("CVEの詳細情報が添付ファイルで出力されます。");
        if (preferenceStore.getBoolean(PreferenceConstants.INCLUDE_CVE_DETAIL)) {
            includeCVEDetailChk.setSelection(true);
        }
        libTabItem.setControl(libButtonGrp);

        int idx = this.preferenceStore.getInt(PreferenceConstants.OPENED_TAB_IDX);
        tabFolder.setSelection(idx);

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
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage());
                PreferenceNode csvNode = new PreferenceNode("csv", new CSVPreferencePage());
                PreferenceNode vulCsvColumnNode = new PreferenceNode("vulcsvcolumn", new VulCSVColumnPreferencePage());
                PreferenceNode libCsvColumnNode = new PreferenceNode("libcsvcolumn", new LibCSVColumnPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(csvNode);
                mgr.addTo(csvNode.getId(), vulCsvColumnNode);
                mgr.addTo(csvNode.getId(), libCsvColumnNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new MyPreferenceDialog(shell, mgr);
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
        int width = this.preferenceStore.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.preferenceStore.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.pack();
        }
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

    private void uiReset() {
        // src
        srcListFilter.setText("");
        srcList.removeAll();
        srcApps.clear();
        // dst
        dstListFilter.setText("");
        dstList.removeAll();
        dstApps.clear();
        // full
        if (fullAppMap != null) {
            fullAppMap.clear();
        }
    }

    private void uiUpdate() {
    }

    public PreferenceStore getPreferenceStore() {
        return preferenceStore;
    }

    public Organization getValidOrganization() {
        String orgJsonStr = preferenceStore.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        return org;
                    }
                }
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
        return null;
    }

    public List<Organization> getValidOrganizations() {
        List<Organization> orgs = new ArrayList<Organization>();
        String orgJsonStr = preferenceStore.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        orgs.add(org);
                    }
                }
            } catch (JsonSyntaxException e) {
                return orgs;
            }
        }
        return orgs;
    }

    public void setWindowTitle() {
        String text = null;
        List<Organization> validOrgs = getValidOrganizations();
        if (!validOrgs.isEmpty()) {
            List<String> orgNameList = new ArrayList<String>();
            for (Organization validOrg : validOrgs) {
                orgNameList.add(validOrg.getName());
            }
            text = String.join(", ", orgNameList);
        }
        if (text == null || text.isEmpty()) {
            this.shell.setText(String.format(WINDOW_TITLE, "組織未設定"));
        } else {
            this.shell.setText(String.format(WINDOW_TITLE, text));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // if ("authInput".equals(event.getPropertyName())) {
        // Boolean enableFlg = (Boolean) event.getNewValue();
        // this.executeBtn.setEnabled(enableFlg.booleanValue());
        // } else if ("optionInputs".equals(event.getPropertyName())) {
        // String oldValue = (String) event.getOldValue();
        // System.out.println(oldValue);
        // }
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
