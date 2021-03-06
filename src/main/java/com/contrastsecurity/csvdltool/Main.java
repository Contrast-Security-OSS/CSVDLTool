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

import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.lang.reflect.InvocationTargetException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackEventTagsApi;
import com.contrastsecurity.csvdltool.api.PutTagsToAttackEventsApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.AttackEventCSVColumn;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.ServerCSVColumn;
import com.contrastsecurity.csvdltool.preference.AboutPage;
import com.contrastsecurity.csvdltool.preference.AttackEventCSVColumnPreferencePage;
import com.contrastsecurity.csvdltool.preference.BasePreferencePage;
import com.contrastsecurity.csvdltool.preference.CSVPreferencePage;
import com.contrastsecurity.csvdltool.preference.ConnectionPreferencePage;
import com.contrastsecurity.csvdltool.preference.LibCSVColumnPreferencePage;
import com.contrastsecurity.csvdltool.preference.MyPreferenceDialog;
import com.contrastsecurity.csvdltool.preference.OtherPreferencePage;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.contrastsecurity.csvdltool.preference.ServerCSVColumnPreferencePage;
import com.contrastsecurity.csvdltool.preference.VulCSVColumnPreferencePage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "CSVDLTool - %s";
    // ?????????MASTER_PASSWORD???????????????????????????????????????????????????????????????????????????????????????????????????
    // ????????????????????????????????????????????????????????????changeme???????????????????????????????????????????????????
    public static final String MASTER_PASSWORD = "changeme!";

    // ???????????????????????????????????????
    public static final String CSV_WIN_ENCODING = "Shift_JIS";
    public static final String CSV_MAC_ENCODING = "UTF-8";
    public static final String FILE_ENCODING = "UTF-8";

    public static final int MINIMUM_SIZE_WIDTH = 640;
    public static final int MINIMUM_SIZE_HEIGHT = 620;

    private CSVDLToolShell shell;

    // ASSESS
    private Button appLoadBtn;
    private Text srcListFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private CTabFolder mainTabFolder;
    private CTabFolder subTabFolder;

    private Button vulExecuteBtn;
    private Button vulOnlyParentAppChk;
    private Button includeDescChk;
    private Button includeStackTraceChk;

    private Button libExecuteBtn;
    private Button onlyHasCVEChk;
    private Button includeCVEDetailChk;

    private Button attackLoadBtn;

    private Button serverLoadBtn;

    private Button settingBtn;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)");
    private Text vulSeverityFilterTxt;
    private Text vulVulnTypeFilterTxt;
    private Text vulLastDetectedFilterTxt;

    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> assessFilterMap;
    private Map<FilterEnum, Set<Filter>> protectFilterMap;
    private Map<FilterEnum, Set<Filter>> serverFilterMap;
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;

    // PROTECT
    private Label attackEventCount;
    private List<Button> attackTermRadios = new ArrayList<Button>();
    private Button attackTerm30days;
    private Button attackTermYesterday;
    private Button attackTermToday;
    private Button attackTermLastWeek;
    private Button attackTermThisWeek;
    private Button attackTermPeriod;
    private Text attackDetectedFilterTxt;
    private Date frDetectedDate;
    private Date toDetectedDate;
    private Table attackTable;
    private List<AttackEvent> attackEvents;
    private List<AttackEvent> filteredAttackEvents = new ArrayList<AttackEvent>();
    private Map<AttackEventDetectedDateFilterEnum, LocalDate> attackEventDetectedFilterMap;

    // SERVER
    private Table serverTable;
    private List<Server> servers;
    private List<Server> filteredServers = new ArrayList<Server>();

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private CookieManager cookieManager;

    public enum AuthType {
        TOKEN,
        BASIC
    }

    Logger logger = LogManager.getLogger("csvdltool");

    String currentTitle;
    private AuthType authType;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.authType = AuthType.TOKEN;
        if (System.getProperty("auth") != null && System.getProperty("auth").equals("basic")) {
            main.authType = AuthType.BASIC;
        }
        main.initialize();
        main.createPart();
    }

    public AuthType getAuthType() {
        return authType;
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home");
            this.ps = new PreferenceStore(homeDir + "\\csvdltool.properties");
            if (OS.isFamilyMac()) {
                this.ps = new PreferenceStore(homeDir + "/csvdltool.properties");
            }
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps = new PreferenceStore("csvdltool.properties");
                this.ps.load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PASS_TYPE, "input");
            this.ps.setDefault(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none");
            this.ps.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_VUL, VulCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_VUL, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_VUL, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_VUL, "'vul'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_LIB, LibCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_LIB, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_LIB, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_LIB, "'lib'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.ATTACK_START_WEEKDAY, 1); // ?????????
            this.ps.setDefault(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER, 0);
            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_ATTACKEVENT, AttackEventCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_ATTACKEVENT, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT, "'attackevent'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_SERVER, ServerCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_SERVER, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_SERVER, "'server'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.OPENED_MAIN_TAB_IDX, 0);
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.ps.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.ps.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.ps.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new CSVDLToolShell(display, this);
        shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
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
                int main_idx = mainTabFolder.getSelectionIndex();
                int sub_idx = subTabFolder.getSelectionIndex();
                ps.setValue(PreferenceConstants.OPENED_MAIN_TAB_IDX, main_idx);
                ps.setValue(PreferenceConstants.OPENED_SUB_TAB_IDX, sub_idx);
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.VUL_ONLY_PARENT_APP, vulOnlyParentAppChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_DESCRIPTION, includeDescChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_STACKTRACE, includeStackTraceChk.getSelection());
                ps.setValue(PreferenceConstants.ONLY_HAS_CVE, onlyHasCVEChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
                ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, "");
                ps.setValue(PreferenceConstants.XSRF_TOKEN, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
                ps.setValue(PreferenceConstants.TSV_STATUS, "");
                for (Button termBtn : attackTermRadios) {
                    if (termBtn.getSelection()) {
                        ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER, attackTermRadios.indexOf(termBtn));
                    }
                }
                try {
                    ps.save();
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
                    attackLoadBtn.setEnabled(false);
                    serverLoadBtn.setEnabled(false);
                    settingBtn.setText("????????????????????????????????????????????????????????????");
                    currentTitle = "";
                    uiReset();
                } else {
                    appLoadBtn.setEnabled(true);
                    vulExecuteBtn.setEnabled(true);
                    attackLoadBtn.setEnabled(true);
                    serverLoadBtn.setEnabled(true);
                    settingBtn.setText("??????");
                    List<String> orgNameList = new ArrayList<String>();
                    String title = String.join(", ", orgNameList);
                    if (currentTitle != null && !currentTitle.equals(title)) {
                        uiReset();
                        currentTitle = title;
                    }
                }
                updateProtectOption();
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
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

        mainTabFolder = new CTabFolder(shell, SWT.NONE);
        GridData mainTabFolderGrDt = new GridData(GridData.FILL_BOTH);
        mainTabFolder.setLayoutData(mainTabFolderGrDt);
        mainTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mainTabFolder.getSelectionIndex() == 1) {
                    updateProtectOption();
                }
            }
        });

        // #################### ASSESS #################### //
        CTabItem assessTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        assessTabItem.setText("ASSESS");
        assessTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("assess16.png")));

        Composite assessShell = new Composite(mainTabFolder, SWT.NONE);
        assessShell.setLayout(new GridLayout(1, false));

        Group appListGrp = new Group(assessShell, SWT.NONE);
        GridLayout appListGrpLt = new GridLayout(3, false);
        appListGrpLt.marginHeight = 0;
        appListGrpLt.verticalSpacing = 0;
        appListGrp.setLayout(appListGrpLt);
        GridData appListGrpGrDt = new GridData(GridData.FILL_BOTH);
        appListGrpGrDt.minimumHeight = 200;
        appListGrp.setLayoutData(appListGrpGrDt);
        // appListGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        appLoadBtn = new Button(appListGrp, SWT.PUSH);
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("?????????????????????????????????????????????");
        appLoadBtn.setToolTipText("TeamServer?????????????????????????????????????????????????????????????????????????????????");
        appLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                uiReset();

                AppsGetWithProgress progress = new AppsGetWithProgress(shell, ps, getValidOrganizations());
                ProgressMonitorDialog progDialog = new AppGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "???????????????????????????????????????", String.format("TeamServer???????????????????????????????????????\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "???????????????????????????????????????", String.format("????????????????????????????????????: %s\r\n?????????????????????????????????????????????", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "???????????????????????????????????????", errorMsg);
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openInformation(shell, "???????????????????????????????????????", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "???????????????????????????????????????", String.format("????????????????????????????????????????????????????????????????????????\r\n%s", errorMsg));
                    }
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullAppMap = progress.getFullAppMap();
                if (fullAppMap.isEmpty()) {
                    String userName = ps.getString(PreferenceConstants.USERNAME);
                    StringJoiner sj = new StringJoiner("\r\n");
                    sj.add("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                    sj.add("???????????????????????????????????????????????????????????????????????????View????????????????????????????????????");
                    sj.add(String.format("???%s", userName));
                    sj.add("???Assess???????????????????????????????????????????????????????????????????????????");
                    sj.add("???????????????????????????????????????????????????????????????????????????????????????");
                    MessageDialog.openInformation(shell, "???????????????????????????????????????", sj.toString());
                }
                for (String appLabel : fullAppMap.keySet()) {
                    srcList.add(appLabel); // UI list
                    srcApps.add(appLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                assessFilterMap = progress.getFilterMap();
                vulSeverityFilterTxt.setText("?????????");
                vulVulnTypeFilterTxt.setText("?????????");
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
                            continue; // ?????????????????????????????????????????????
                        }
                        srcList.add(appLabel); // UI List src
                        srcApps.add(appLabel); // memory src
                    }
                } else {
                    for (String appLabel : fullAppMap.keySet()) {
                        if (appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                            if (dstApps.contains(appLabel)) {
                                continue; // ?????????????????????????????????????????????
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
        this.srcList.setToolTipText("?????????????????????????????????????????????");
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

        Composite srcListLblComp = new Composite(srcGrp, SWT.NONE);
        GridLayout srcListLblLt = new GridLayout(2, false);
        srcListLblLt.marginHeight = 0;
        srcListLblLt.marginWidth = 0;
        srcListLblLt.marginLeft = 5;
        srcListLblLt.marginBottom = 0;
        srcListLblComp.setLayout(srcListLblLt);
        srcListLblComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label srcListDescLbl = new Label(srcListLblComp, SWT.LEFT);
        GridData srcListDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcListDescLblGrDt.minimumHeight = 12;
        srcListDescLblGrDt.heightHint = 12;
        srcListDescLbl.setLayoutData(srcListDescLblGrDt);
        srcListDescLbl.setFont(new Font(display, "?????? ????????????", 8, SWT.NORMAL));
        srcListDescLbl.setText("?????????????????????????????????????????????");
        srcListDescLbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.srcCount = new Label(srcListLblComp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 12;
        srcCountGrDt.heightHint = 12;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(display, "?????? ????????????", 8, SWT.NORMAL));
        this.srcCount.setText("0");
        this.srcCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        Composite btnGrp = new Composite(appListGrp, SWT.NONE);
        btnGrp.setLayout(new GridLayout(1, false));
        GridData btnGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        btnGrpGrDt.verticalAlignment = SWT.CENTER;
        btnGrp.setLayoutData(btnGrpGrDt);

        Button allRightBtn = new Button(btnGrp, SWT.PUSH);
        allRightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allRightBtn.setText(">>");
        allRightBtn.addSelectionListener(new SelectionAdapter() {
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
        });

        Button rightBtn = new Button(btnGrp, SWT.PUSH);
        rightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightBtn.setText(">");
        rightBtn.addSelectionListener(new SelectionAdapter() {
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
        });

        Button leftBtn = new Button(btnGrp, SWT.PUSH);
        leftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftBtn.setText("<");
        leftBtn.addSelectionListener(new SelectionAdapter() {
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
        });

        Button allLeftBtn = new Button(btnGrp, SWT.PUSH);
        allLeftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allLeftBtn.setText("<<");
        allLeftBtn.addSelectionListener(new SelectionAdapter() {
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
                            continue; // ?????????????????????????????????????????????
                        }
                        dstList.add(appName); // UI List dst
                        dstApps.add(appName); // memory dst
                    }
                } else {
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcApps.contains(appName)) {
                                continue; // ?????????????????????????????????????????????
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
        this.dstList.setToolTipText("?????????????????????????????????????????????");
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

        Composite dstListLblComp = new Composite(dstGrp, SWT.NONE);
        GridLayout dstListLblLt = new GridLayout(2, false);
        dstListLblLt.marginHeight = 0;
        dstListLblLt.marginWidth = 0;
        dstListLblLt.marginLeft = 5;
        dstListLblLt.marginBottom = 0;
        dstListLblComp.setLayout(dstListLblLt);
        dstListLblComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label dstListDescLbl = new Label(dstListLblComp, SWT.LEFT);
        GridData dstListDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstListDescLblGrDt.minimumHeight = 12;
        dstListDescLblGrDt.heightHint = 12;
        dstListDescLbl.setLayoutData(dstListDescLblGrDt);
        dstListDescLbl.setFont(new Font(display, "?????? ????????????", 8, SWT.NORMAL));
        dstListDescLbl.setText("?????????????????????????????????????????????");
        dstListDescLbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.dstCount = new Label(dstListLblComp, SWT.RIGHT);
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.minimumHeight = 12;
        dstCountGrDt.heightHint = 12;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setFont(new Font(display, "?????? ????????????", 8, SWT.NORMAL));
        this.dstCount.setText("0");
        this.dstCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        subTabFolder = new CTabFolder(assessShell, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_HORIZONTAL);
        subTabFolder.setLayoutData(tabFolderGrDt);
        subTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        // #################### ????????? #################### //
        CTabItem vulTabItem = new CTabItem(subTabFolder, SWT.NONE);
        vulTabItem.setText("?????????");

        // ========== ???????????? ==========
        Composite vulButtonGrp = new Composite(subTabFolder, SWT.NULL);
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
        vulFilterGrp.setText("??????????????????");

        new Label(vulFilterGrp, SWT.LEFT).setText("????????????");
        vulSeverityFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulSeverityFilterTxt.setText("???????????????????????????????????????????????????????????????");
        vulSeverityFilterTxt.setEditable(false);
        vulSeverityFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSeverityFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (assessFilterMap != null && assessFilterMap.containsKey(FilterEnum.SEVERITY)) {
                    FilterSeverityDialog filterDialog = new FilterSeverityDialog(shell, assessFilterMap.get(FilterEnum.SEVERITY));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        vulExecuteBtn.setFocus();
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : assessFilterMap.get(FilterEnum.SEVERITY)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulSeverityFilterTxt.setText("?????????");
                    } else {
                        vulSeverityFilterTxt.setText(String.join(", ", labels));
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText("?????????????????????");
        vulVulnTypeFilterTxt = new Text(vulFilterGrp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        vulVulnTypeFilterTxt.setText("???????????????????????????????????????????????????????????????");
        vulVulnTypeFilterTxt.setEditable(false);
        GridData vulVulnTypeFilterTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        vulVulnTypeFilterTxtGrDt.heightHint = 2 * vulVulnTypeFilterTxt.getLineHeight();
        vulVulnTypeFilterTxt.setLayoutData(vulVulnTypeFilterTxtGrDt);
        vulVulnTypeFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (assessFilterMap != null && assessFilterMap.containsKey(FilterEnum.VULNTYPE)) {
                    FilterVulnTypeDialog filterDialog = new FilterVulnTypeDialog(shell, assessFilterMap.get(FilterEnum.VULNTYPE));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        vulExecuteBtn.setFocus();
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : assessFilterMap.get(FilterEnum.VULNTYPE)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulVulnTypeFilterTxt.setText("?????????");
                    } else {
                        vulVulnTypeFilterTxt.setText(String.join(", ", labels));
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText("??????????????????");
        vulLastDetectedFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulLastDetectedFilterTxt.setText("?????????");
        vulLastDetectedFilterTxt.setEditable(false);
        vulLastDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulLastDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(shell, frLastDetectedDate, toLastDetectedDate);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    vulExecuteBtn.setFocus();
                    return;
                }
                frLastDetectedDate = filterDialog.getFrDate();
                toLastDetectedDate = filterDialog.getToDate();
                if (frLastDetectedDate != null && toLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("%s ??? %s", sdf.format(frLastDetectedDate), sdf.format(toLastDetectedDate)));
                } else if (frLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("%s ???", sdf.format(frLastDetectedDate)));
                } else if (toLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("??? %s", sdf.format(toLastDetectedDate)));
                } else {
                    vulLastDetectedFilterTxt.setText("?????????");
                }
                vulExecuteBtn.setFocus();
            }
        });

        // ========== ??????????????? ==========
        vulExecuteBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.heightHint = 50;
        vulExecuteBtn.setLayoutData(executeBtnGrDt);
        vulExecuteBtn.setText("??????");
        vulExecuteBtn.setToolTipText("??????????????????????????????CSV???????????????????????????");
        vulExecuteBtn.setFont(new Font(display, "?????? ????????????", 20, SWT.NORMAL));
        vulExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, "?????????????????????", "?????????????????????????????????????????????????????????????????????");
                    return;
                }
                VulGetWithProgress progress = new VulGetWithProgress(shell, ps, dstApps, fullAppMap, assessFilterMap, frLastDetectedDate, toLastDetectedDate,
                        vulOnlyParentAppChk.getSelection(), includeDescChk.getSelection(), includeStackTraceChk.getSelection());
                ProgressMonitorDialog progDialog = new VulGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "????????????????????????", String.format("TeamServer???????????????????????????????????????\r\n%s", exceptionMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "????????????????????????", String.format("????????????????????????????????????: %s\r\n?????????????????????????????????????????????", exceptionMsg));
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, "????????????????????????", exceptionMsg);
                    } else {
                        MessageDialog.openError(shell, "????????????????????????", String.format("????????????????????????????????????????????????????????????????????????\r\n%s", exceptionMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        vulOnlyParentAppChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyParentAppChk.setText("???????????????????????????????????????????????????????????????????????????????????????");
        if (this.ps.getBoolean(PreferenceConstants.VUL_ONLY_PARENT_APP)) {
            vulOnlyParentAppChk.setSelection(true);
        }

        includeDescChk = new Button(vulButtonGrp, SWT.CHECK);
        includeDescChk.setText("?????????????????????????????????????????????HTTP????????????????????????????????????????????????????????????????????????????????????????????????????????????");
        includeDescChk.setToolTipText("????????????HTTP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_DESCRIPTION)) {
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
        includeStackTraceChk.setText("???????????????????????????????????????????????????????????????????????????????????????????????????????????????");
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_STACKTRACE)) {
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

        // #################### ??????????????? #################### //
        CTabItem libTabItem = new CTabItem(subTabFolder, SWT.NONE);
        libTabItem.setText("???????????????");

        // ========== ???????????? ==========
        Composite libButtonGrp = new Composite(subTabFolder, SWT.NULL);
        GridLayout libButtonGrpLt = new GridLayout(1, false);
        libButtonGrpLt.marginWidth = 10;
        libButtonGrpLt.marginHeight = 10;
        libButtonGrp.setLayout(libButtonGrpLt);
        GridData libButtonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // libButtonGrpGrDt.horizontalSpan = 3;
        // libButtonGrpGrDt.widthHint = 100;
        libButtonGrp.setLayoutData(libButtonGrpGrDt);

        // ========== ??????????????? ==========
        libExecuteBtn = new Button(libButtonGrp, SWT.PUSH);
        GridData libExecuteBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        libExecuteBtnGrDt.heightHint = 50;
        libExecuteBtn.setLayoutData(libExecuteBtnGrDt);
        libExecuteBtn.setText("??????");
        libExecuteBtn.setToolTipText("????????????????????????????????????CSV???????????????????????????");
        libExecuteBtn.setFont(new Font(display, "?????? ????????????", 20, SWT.NORMAL));
        libExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, "???????????????????????????", "?????????????????????????????????????????????????????????????????????");
                    return;
                }
                LibGetWithProgress progress = new LibGetWithProgress(shell, ps, dstApps, fullAppMap, onlyHasCVEChk.getSelection(), includeCVEDetailChk.getSelection());
                ProgressMonitorDialog progDialog = new LibGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "??????????????????????????????", String.format("TeamServer???????????????????????????????????????\r\n%s", exceptionMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "??????????????????????????????", String.format("????????????????????????????????????: %s\r\n?????????????????????????????????????????????", exceptionMsg));
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, "??????????????????????????????", exceptionMsg);
                    } else {
                        MessageDialog.openError(shell, "??????????????????????????????", String.format("????????????????????????????????????????????????????????????????????????\r\n%s", exceptionMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        onlyHasCVEChk = new Button(libButtonGrp, SWT.CHECK);
        onlyHasCVEChk.setText("CVE????????????????????????????????????????????????????????????");
        if (this.ps.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            onlyHasCVEChk.setSelection(true);
        }
        includeCVEDetailChk = new Button(libButtonGrp, SWT.CHECK);
        includeCVEDetailChk.setText("CVE?????????????????????????????????????????????????????????");
        includeCVEDetailChk.setToolTipText("CVE????????????????????????????????????????????????????????????");
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_CVE_DETAIL)) {
            includeCVEDetailChk.setSelection(true);
        }
        libTabItem.setControl(libButtonGrp);

        int sub_idx = this.ps.getInt(PreferenceConstants.OPENED_SUB_TAB_IDX);
        subTabFolder.setSelection(sub_idx);

        assessTabItem.setControl(assessShell);

        // #################### PROTECT #################### //
        CTabItem protectTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        protectTabItem.setText("PROTECT");
        protectTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("protect16.png")));

        Composite protectShell = new Composite(mainTabFolder, SWT.NONE);
        protectShell.setLayout(new GridLayout(1, false));

        Group attackListGrp = new Group(protectShell, SWT.NONE);
        attackListGrp.setLayout(new GridLayout(3, false));
        GridData attackListGrpGrDt = new GridData(GridData.FILL_BOTH);
        attackListGrpGrDt.minimumHeight = 200;
        attackListGrp.setLayoutData(attackListGrpGrDt);

        Composite attackTermGrp = new Composite(attackListGrp, SWT.NONE);
        attackTermGrp.setLayout(new GridLayout(7, false));
        GridData attackTermGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackTermGrp.setLayoutData(attackTermGrpGrDt);
        attackTerm30days = new Button(attackTermGrp, SWT.RADIO);
        attackTerm30days.setText("????????????30?????????");
        attackTermRadios.add(attackTerm30days);
        attackTermYesterday = new Button(attackTermGrp, SWT.RADIO);
        attackTermYesterday.setText("??????");
        attackTermRadios.add(attackTermYesterday);
        attackTermToday = new Button(attackTermGrp, SWT.RADIO);
        attackTermToday.setText("??????");
        attackTermRadios.add(attackTermToday);
        attackTermLastWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermLastWeek.setText("??????");
        attackTermRadios.add(attackTermLastWeek);
        attackTermThisWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermThisWeek.setText("??????");
        attackTermRadios.add(attackTermThisWeek);
        attackTermPeriod = new Button(attackTermGrp, SWT.RADIO);
        attackTermPeriod.setText("??????");
        attackTermRadios.add(attackTermPeriod);
        attackDetectedFilterTxt = new Text(attackTermGrp, SWT.BORDER);
        attackDetectedFilterTxt.setText("");
        attackDetectedFilterTxt.setEditable(false);
        attackDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        attackDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(shell, frDetectedDate, toDetectedDate);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    vulExecuteBtn.setFocus();
                    return;
                }
                frDetectedDate = filterDialog.getFrDate();
                toDetectedDate = filterDialog.getToDate();
                if (frDetectedDate != null && toDetectedDate != null) {
                    attackDetectedFilterTxt.setText(String.format("%s ??? %s", sdf.format(frDetectedDate), sdf.format(toDetectedDate)));
                } else if (frDetectedDate != null) {
                    attackDetectedFilterTxt.setText(String.format("%s ???", sdf.format(frDetectedDate)));
                } else if (toDetectedDate != null) {
                    attackDetectedFilterTxt.setText(String.format("??? %s", sdf.format(toDetectedDate)));
                } else {
                    attackDetectedFilterTxt.setText("");
                }
                if (!attackDetectedFilterTxt.getText().isEmpty()) {
                    for (Button rdo : attackTermRadios) {
                        rdo.setSelection(false);
                    }
                    attackTermPeriod.setSelection(true);
                }
                attackLoadBtn.setFocus();
            }
        });
        for (Button termBtn : this.attackTermRadios) {
            termBtn.setSelection(false);
            if (this.attackTermRadios.indexOf(termBtn) == this.ps.getInt(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER)) {
                termBtn.setSelection(true);
            }
        }

        attackLoadBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackLoadBtnGrDt.horizontalSpan = 3;
        attackLoadBtnGrDt.heightHint = 50;
        attackLoadBtn.setLayoutData(attackLoadBtnGrDt);
        attackLoadBtn.setText("??????");
        attackLoadBtn.setToolTipText("????????????????????????????????????????????????");
        attackLoadBtn.setFont(new Font(display, "?????? ????????????", 20, SWT.NORMAL));
        attackLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                attackTable.clearAll();
                attackTable.removeAll();
                Date[] frToDate = getFrToDetectedDate();
                if (frToDate.length != 2) {
                    MessageDialog.openError(shell, "?????????????????????", "??????????????????????????????????????????");
                    return;
                }
                AttackEventsGetWithProgress progress = new AttackEventsGetWithProgress(shell, ps, getValidOrganizations(), frToDate[0], frToDate[1]);
                ProgressMonitorDialog progDialog = new AttackGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                    attackEvents = progress.getAllAttackEvents();
                    Collections.reverse(attackEvents);
                    filteredAttackEvents.addAll(attackEvents);
                    for (AttackEvent attackEvent : attackEvents) {
                        addColToAttackTable(attackEvent, -1);
                    }
                    protectFilterMap = progress.getFilterMap();
                    attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size()));
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "?????????????????????????????????", String.format("TeamServer???????????????????????????????????????\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "?????????????????????????????????", String.format("????????????????????????????????????: %s\r\n?????????????????????????????????????????????", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "?????????????????????????????????", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "?????????????????????????????????", String.format("????????????????????????????????????????????????????????????????????????\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        this.attackEventCount = new Label(attackListGrp, SWT.RIGHT);
        GridData attackEventCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackEventCountGrDt.minimumHeight = 12;
        attackEventCountGrDt.minimumWidth = 30;
        attackEventCountGrDt.heightHint = 12;
        attackEventCountGrDt.widthHint = 30;
        this.attackEventCount.setLayoutData(attackEventCountGrDt);
        this.attackEventCount.setFont(new Font(display, "?????? ????????????", 10, SWT.NORMAL));
        this.attackEventCount.setText("0/0");

        attackTable = new Table(attackListGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        attackTable.setLayoutData(tableGrDt);
        attackTable.setLinesVisible(true);
        attackTable.setHeaderVisible(true);
        Menu menuTable = new Menu(attackTable);
        attackTable.setMenu(menuTable);

        MenuItem miTag = new MenuItem(menuTable, SWT.NONE);
        miTag.setText("????????????");
        miTag.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                // TagInputDialog tagInputDialog = new TagInputDialog(shell);
                Set<String> existTagSet = new TreeSet<String>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    for (String existTag : attackEvent.getTags()) {
                        existTagSet.add(existTag);
                    }
                }
                TagEditDialog tagEditDialog = new TagEditDialog(shell, new ArrayList<>(existTagSet));
                int result = tagEditDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                String tag = tagEditDialog.getTag();
                List<String> removeTags = tagEditDialog.getRemoveTags();
                if (tag == null && removeTags.isEmpty()) {
                    return;
                }
                Map<Organization, List<AttackEvent>> orgMap = new HashMap<Organization, List<AttackEvent>>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    if (orgMap.containsKey(attackEvent.getOrganization())) {
                        orgMap.get(attackEvent.getOrganization()).add(attackEvent);
                    } else {
                        orgMap.put(attackEvent.getOrganization(), new ArrayList<AttackEvent>(Arrays.asList(attackEvent)));
                    }
                }
                try {
                    for (Organization org : orgMap.keySet()) {
                        List<AttackEvent> attackEvents = orgMap.get(org);
                        Api putApi = new PutTagsToAttackEventsApi(shell, ps, org, attackEvents, tag, removeTags);
                        String msg = (String) putApi.put();
                        if (Boolean.valueOf(msg)) {
                            for (AttackEvent attackEvent : attackEvents) {
                                Api attackEventTagsApi = new AttackEventTagsApi(shell, ps, org, attackEvent.getEvent_uuid());
                                List<String> tags = (List<String>) attackEventTagsApi.get();
                                attackEvent.setTags(tags);
                            }
                            attackTable.clearAll();
                            attackTable.removeAll();
                            for (AttackEvent attackEvent : filteredAttackEvents) {
                                addColToAttackTable(attackEvent, -1);
                            }
                            MessageDialog.openInformation(shell, "????????????????????????????????????", "????????????????????????????????????????????????????????????????????????");
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        MenuItem miExp = new MenuItem(menuTable, SWT.NONE);
        miExp.setText("CSV??????????????????");
        miExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String currentPath = System.getProperty("user.dir");
                String filePath = timestamp + ".csv";
                if (OS.isFamilyMac()) {
                    if (currentPath.contains(".app/Contents/Java")) {
                        filePath = "../../../" + timestamp + ".csv";
                    }
                }
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_ATTACKEVENT);
                List<AttackEventCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<AttackEventCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(shell, "?????????????????????????????????????????????", String.format("??????????????????????????????????????????????????????????????????\r\n%s", columnJsonStr));
                        columnList = new ArrayList<AttackEventCSVColumn>();
                    }
                } else {
                    columnList = new ArrayList<AttackEventCSVColumn>();
                    for (AttackEventCSVColmunEnum colEnum : AttackEventCSVColmunEnum.sortedValues()) {
                        columnList.add(new AttackEventCSVColumn(colEnum));
                    }
                }
                for (int idx : selectIndexes) {
                    List<String> csvLineList = new ArrayList<String>();
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    for (AttackEventCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case ATTACK_EVENT_01:
                                // ==================== 01. ???????????? ====================
                                csvLineList.add(attackEvent.getSource_name());
                                break;
                            case ATTACK_EVENT_02:
                                // ==================== 02. ?????????IP ====================
                                csvLineList.add(attackEvent.getSource());
                                break;
                            case ATTACK_EVENT_03:
                                // ==================== 03. ?????? ====================
                                csvLineList.add(attackEvent.getResult());
                                break;
                            case ATTACK_EVENT_04:
                                // ==================== 04. ???????????????????????? ====================
                                csvLineList.add(attackEvent.getApplication().getName());
                                break;
                            case ATTACK_EVENT_05:
                                // ==================== 05. ????????? ====================
                                csvLineList.add(attackEvent.getServer().getName());
                                break;
                            case ATTACK_EVENT_06:
                                // ==================== 06. ????????? ====================
                                csvLineList.add(attackEvent.getRule());
                                break;
                            case ATTACK_EVENT_07:
                                // ==================== 07. ?????? ====================
                                csvLineList.add(attackEvent.getFormatReceived());
                                break;
                            case ATTACK_EVENT_08:
                                // ==================== 08. URL ====================
                                csvLineList.add(attackEvent.getUrl());
                                break;
                            case ATTACK_EVENT_09:
                                // ==================== 09. ????????? ====================
                                csvLineList.add(attackEvent.getUser_input().getValue());
                                break;
                            case ATTACK_EVENT_10:
                                // ==================== 10. ?????? ====================
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), attackEvent.getTags()));
                                break;
                            case ATTACK_EVENT_11:
                                // ==================== 11. ????????? ====================
                                csvLineList.add(attackEvent.getOrganization().getName());
                                break;
                            case ATTACK_EVENT_12:
                                // ==================== 12. ??????ID ====================
                                csvLineList.add(attackEvent.getOrganization().getOrganization_uuid());
                                break;
                            case ATTACK_EVENT_13: {
                                // ==================== 13. ????????????????????????????????? ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL),
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(link);
                                break;
                            }
                            case ATTACK_EVENT_14: {
                                // ==================== 14. ???????????????????????????????????????????????????????????? ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL),
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(String.format("=HYPERLINK(\"%s\",\"TeamServer???\")", link));
                                break;
                            }
                        }
                    }
                    csvList.add(csvLineList);
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_ATTACKEVENT)) {
                        List<String> csvHeaderList = new ArrayList<String>();
                        for (AttackEventCSVColumn csvColumn : columnList) {
                            if (csvColumn.isValid()) {
                                csvHeaderList.add(csvColumn.getColumn().getCulumn());
                            }
                        }
                        printer.printRecord(csvHeaderList);
                    }
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                    MessageDialog.openInformation(shell, "?????????????????????????????????????????????", "csv????????????????????????????????????????????????");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miReport = new MenuItem(menuTable, SWT.NONE);
        miReport.setText("??????????????????");
        miReport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                Set<String> srcIpSet = new HashSet<String>();
                Set<String> ruleSet = new HashSet<String>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    srcIpSet.add(attackEvent.getSource());
                    ruleSet.add(attackEvent.getRule());
                }
                Map<String, Map<String, Integer>> srcIpMap = new HashMap<String, Map<String, Integer>>();
                for (String srcIp : srcIpSet) {
                    Map<String, Integer> ruleMap = new HashMap<String, Integer>();
                    for (String rule : ruleSet) {
                        ruleMap.put(rule, 0);
                    }
                    srcIpMap.put(srcIp, ruleMap);
                }
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    String srcIp = attackEvent.getSource();
                    String rule = attackEvent.getRule();
                    int cnt = srcIpMap.get(srcIp).get(rule).intValue();
                    srcIpMap.get(srcIp).put(rule, ++cnt);
                }
                String timestamp = new SimpleDateFormat("'protect_report'_yyyy-MM-dd_HHmmss").format(new Date());
                String currentPath = System.getProperty("user.dir");
                String filePath = timestamp + ".txt";
                if (OS.isFamilyMac()) {
                    if (currentPath.contains(".app/Contents/Java")) {
                        filePath = "../../../" + timestamp + ".txt";
                    }
                }
                String txt_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    txt_encoding = Main.CSV_MAC_ENCODING;
                }
                File f = new File(filePath);
                try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), txt_encoding)))) {
                    for (String srcIp : srcIpMap.keySet()) {
                        printWriter.println(String.format("- %s", srcIp));
                        Map<String, Integer> ruleMap = srcIpMap.get(srcIp);
                        for (String rule : ruleMap.keySet()) {
                            int cnt = ruleMap.get(rule).intValue();
                            if (cnt > 0) {
                                printWriter.println(String.format("  - %s: %d", rule, cnt));
                            }
                        }
                    }
                    MessageDialog.openInformation(shell, "?????????????????????????????????????????????", "txt????????????????????????????????????????????????");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miJump = new MenuItem(menuTable, SWT.NONE);
        miJump.setText("?????????????????????");
        miJump.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                if (selectIndexes.length > 10) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    messageBox.setText("??????????????????????????????????????????");
                    messageBox.setMessage(String.format("??????????????????????????????????????????%d?????????????????????????????????????????????", selectIndexes.length));
                    int response = messageBox.open();
                    if (response == SWT.NO) {
                        return;
                    }
                }
                try {
                    Desktop desktop = Desktop.getDesktop();
                    for (int idx : selectIndexes) {
                        AttackEvent attackEvent = filteredAttackEvents.get(idx);
                        String contrastUrl = ps.getString(PreferenceConstants.CONTRAST_URL);
                        String orgUuid = attackEvent.getOrganization().getOrganization_uuid();
                        String eventUuid = attackEvent.getEvent_uuid();
                        desktop.browse(new URI(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid)));
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (URISyntaxException urise) {
                    urise.printStackTrace();
                }
            }
        });

        MenuItem miUrlCopy = new MenuItem(menuTable, SWT.NONE);
        miUrlCopy.setText("TeamServer???URL????????????");
        miUrlCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectIndex = attackTable.getSelectionIndex();
                AttackEvent attackEvent = filteredAttackEvents.get(selectIndex);
                String contrastUrl = ps.getString(PreferenceConstants.CONTRAST_URL);
                String orgUuid = attackEvent.getOrganization().getOrganization_uuid();
                String eventUuid = attackEvent.getEvent_uuid();
                Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid));
                clipboard.setContents(selection, null);
            }
        });

        MenuItem miSelectAll = new MenuItem(menuTable, SWT.NONE);
        miSelectAll.setText("??????????????????Ctrl + A???");
        miSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                attackTable.selectAll();
            }
        });

        attackTable.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                miUrlCopy.setEnabled(true);
                if (attackTable.getSelectionCount() <= 0) {
                    event.doit = false;
                } else if (attackTable.getSelectionCount() != 1) {
                    miUrlCopy.setEnabled(false);
                }
            }
        });
        attackTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
                    attackTable.selectAll();
                    e.doit = false;
                }
            }
        });

        TableColumn column0 = new TableColumn(attackTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(attackTable, SWT.LEFT);
        column1.setWidth(120);
        column1.setText("????????????");
        TableColumn column2 = new TableColumn(attackTable, SWT.LEFT);
        column2.setWidth(120);
        column2.setText("?????????IP");
        TableColumn column3 = new TableColumn(attackTable, SWT.CENTER);
        column3.setWidth(100);
        column3.setText("??????");
        TableColumn column4 = new TableColumn(attackTable, SWT.LEFT);
        column4.setWidth(250);
        column4.setText("????????????????????????");
        TableColumn column5 = new TableColumn(attackTable, SWT.LEFT);
        column5.setWidth(200);
        column5.setText("?????????");
        TableColumn column6 = new TableColumn(attackTable, SWT.LEFT);
        column6.setWidth(200);
        column6.setText("?????????");
        TableColumn column7 = new TableColumn(attackTable, SWT.LEFT);
        column7.setWidth(150);
        column7.setText("??????");
        TableColumn column8 = new TableColumn(attackTable, SWT.LEFT);
        column8.setWidth(150);
        column8.setText("URL");
        TableColumn column9 = new TableColumn(attackTable, SWT.LEFT);
        column9.setWidth(250);
        column9.setText("?????????");
        TableColumn column10 = new TableColumn(attackTable, SWT.LEFT);
        column10.setWidth(250);
        column10.setText("??????");
        TableColumn column11 = new TableColumn(attackTable, SWT.LEFT);
        column11.setWidth(150);
        column11.setText("?????????");

        Button attackEventFilterBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackEventFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackEventFilterBtnGrDt.horizontalSpan = 3;
        attackEventFilterBtn.setLayoutData(attackEventFilterBtnGrDt);
        attackEventFilterBtn.setText("???????????????");
        attackEventFilterBtn.setToolTipText("????????????????????????????????????????????????????????????");
        attackEventFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (protectFilterMap == null) {
                    MessageDialog.openInformation(shell, "?????????????????????????????????", "?????????????????????????????????????????????????????????");
                    return;
                }
                String dayTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                String nightTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                if (!dayTimeHours.isEmpty() || !nightTimeHours.isEmpty()) {
                    Set<Filter> businessHoursFilterSet = new LinkedHashSet<Filter>();
                    if (!dayTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter("???????????????"));
                    }
                    if (!nightTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter("???????????????"));
                    }
                    businessHoursFilterSet.add(new Filter("??????????????????"));
                    protectFilterMap.put(FilterEnum.BUSINESS_HOURS, businessHoursFilterSet);
                }
                AttackEventFilterDialog filterDialog = new AttackEventFilterDialog(shell, protectFilterMap);
                filterDialog.addPropertyChangeListener(shell.getMain());
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
            }
        });

        protectTabItem.setControl(protectShell);

        // #################### SERVER #################### //
        CTabItem serverTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        serverTabItem.setText("SERVER(?????)");
        serverTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("server16.png")));

        Composite serverShell = new Composite(mainTabFolder, SWT.NONE);
        serverShell.setLayout(new GridLayout(1, false));

        Group serverListGrp = new Group(serverShell, SWT.NONE);
        serverListGrp.setLayout(new GridLayout(3, false));
        GridData serverListGrpGrDt = new GridData(GridData.FILL_BOTH);
        serverListGrpGrDt.minimumHeight = 200;
        serverListGrp.setLayoutData(serverListGrpGrDt);

        serverLoadBtn = new Button(serverListGrp, SWT.PUSH);
        GridData serverLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        serverLoadBtnGrDt.horizontalSpan = 3;
        serverLoadBtnGrDt.heightHint = 50;
        serverLoadBtn.setLayoutData(serverLoadBtnGrDt);
        serverLoadBtn.setText("??????");
        serverLoadBtn.setToolTipText("???????????????????????????????????????");
        serverLoadBtn.setFont(new Font(display, "?????? ????????????", 20, SWT.NORMAL));
        serverLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                serverTable.clearAll();
                serverTable.removeAll();
                ServersWithProgress progress = new ServersWithProgress(shell, ps, getValidOrganizations());
                ProgressMonitorDialog progDialog = new AttackGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                    servers = progress.getAllServers();
                    filteredServers.addAll(servers);
                    for (Server server : servers) {
                        addColToServerTable(server, -1);
                    }
                    serverFilterMap = progress.getFilterMap();
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "????????????????????????", String.format("TeamServer???????????????????????????????????????\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "????????????????????????", String.format("????????????????????????????????????: %s\r\n?????????????????????????????????????????????", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "????????????????????????", errorMsg);
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openInformation(shell, "????????????????????????", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "????????????????????????", String.format("????????????????????????????????????????????????????????????????????????\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        serverTable = new Table(serverListGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData serverTableGrDt = new GridData(GridData.FILL_BOTH);
        serverTableGrDt.horizontalSpan = 3;
        serverTable.setLayoutData(serverTableGrDt);
        serverTable.setLinesVisible(true);
        serverTable.setHeaderVisible(true);

        Menu menuServerTable = new Menu(serverTable);
        serverTable.setMenu(menuServerTable);

        MenuItem miServerExp = new MenuItem(menuServerTable, SWT.NONE);
        miServerExp.setText("CSV??????????????????");
        miServerExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = serverTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String currentPath = System.getProperty("user.dir");
                String filePath = timestamp + ".csv";
                if (OS.isFamilyMac()) {
                    if (currentPath.contains(".app/Contents/Java")) {
                        filePath = "../../../" + timestamp + ".csv";
                    }
                }
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_SERVER);
                List<ServerCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<ServerCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(shell, "????????????????????????????????????", String.format("?????????????????????????????????????????????????????????\r\n%s", columnJsonStr));
                        columnList = new ArrayList<ServerCSVColumn>();
                    }
                } else {
                    columnList = new ArrayList<ServerCSVColumn>();
                    for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                        columnList.add(new ServerCSVColumn(colEnum));
                    }
                }
                for (int idx : selectIndexes) {
                    List<String> csvLineList = new ArrayList<String>();
                    Server server = filteredServers.get(idx);
                    for (ServerCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case SERVER_01:
                                // ==================== 01. ???????????? ====================
                                csvLineList.add(server.getName());
                                break;
                            case SERVER_02:
                                // ==================== 02. ?????? ====================
                                csvLineList.add(server.getPath());
                                break;
                            case SERVER_03:
                                // ==================== 03. ?????? ====================
                                csvLineList.add(server.getLanguage());
                                break;
                            case SERVER_04:
                                // ==================== 04. ????????????????????????????????? ====================
                                csvLineList.add(server.getAgent_version());
                                break;
                        }
                    }
                    csvList.add(csvLineList);
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_SERVER)) {
                        List<String> csvHeaderList = new ArrayList<String>();
                        for (ServerCSVColumn csvColumn : columnList) {
                            if (csvColumn.isValid()) {
                                csvHeaderList.add(csvColumn.getColumn().getCulumn());
                            }
                        }
                        printer.printRecord(csvHeaderList);
                    }
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                    MessageDialog.openInformation(shell, "????????????????????????????????????", "csv????????????????????????????????????????????????");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        serverTable.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (serverTable.getSelectionCount() <= 0) {
                    event.doit = false;
                }
            }
        });
        serverTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
                    serverTable.selectAll();
                    e.doit = false;
                }
            }
        });

        TableColumn serverColumn1 = new TableColumn(serverTable, SWT.NONE);
        serverColumn1.setWidth(0);
        serverColumn1.setResizable(false);
        TableColumn serverColumn2 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn2.setWidth(150);
        serverColumn2.setText("????????????");
        TableColumn serverColumn3 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn3.setWidth(360);
        serverColumn3.setText("??????");
        TableColumn serverColumn4 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn4.setWidth(100);
        serverColumn4.setText("??????");
        TableColumn serverColumn5 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn5.setWidth(200);
        serverColumn5.setText("?????????????????????????????????");
        serverTabItem.setControl(serverShell);

        Button serverFilterBtn = new Button(serverListGrp, SWT.PUSH);
        GridData serverFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        serverFilterBtnGrDt.horizontalSpan = 3;
        serverFilterBtn.setLayoutData(serverFilterBtnGrDt);
        serverFilterBtn.setText("???????????????");
        serverFilterBtn.setToolTipText("???????????????????????????????????????????????????");
        serverFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (serverFilterMap == null) {
                    MessageDialog.openInformation(shell, "????????????????????????", "????????????????????????????????????????????????");
                    return;
                }
                ServerFilterDialog filterDialog = new ServerFilterDialog(shell, serverFilterMap);
                filterDialog.addPropertyChangeListener(shell.getMain());
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
            }
        });

        int main_idx = this.ps.getInt(PreferenceConstants.OPENED_MAIN_TAB_IDX);
        mainTabFolder.setSelection(main_idx);

        // ========== ??????????????? ==========
        settingBtn = new Button(shell, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText("??????");
        settingBtn.setToolTipText("??????????????????????????????????????????");
        settingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage(authType));
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage());
                PreferenceNode otherNode = new PreferenceNode("other", new OtherPreferencePage());
                PreferenceNode csvNode = new PreferenceNode("csv", new CSVPreferencePage());
                PreferenceNode vulCsvColumnNode = new PreferenceNode("vulcsvcolumn", new VulCSVColumnPreferencePage());
                PreferenceNode libCsvColumnNode = new PreferenceNode("libcsvcolumn", new LibCSVColumnPreferencePage());
                PreferenceNode evtCsvColumnNode = new PreferenceNode("evtcsvcolumn", new AttackEventCSVColumnPreferencePage());
                PreferenceNode svrCsvColumnNode = new PreferenceNode("svrcsvcolumn", new ServerCSVColumnPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
                mgr.addToRoot(csvNode);
                mgr.addTo(csvNode.getId(), vulCsvColumnNode);
                mgr.addTo(csvNode.getId(), libCsvColumnNode);
                mgr.addTo(csvNode.getId(), evtCsvColumnNode);
                mgr.addTo(csvNode.getId(), svrCsvColumnNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new MyPreferenceDialog(shell, mgr);
                dialog.setPreferenceStore(ps);
                dialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        uiUpdate();
        int width = this.ps.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.ps.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.setSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
            // shell.pack();
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

    private void addColToAttackTable(AttackEvent attackEvent, int index) {
        if (attackEvent == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(attackTable, SWT.CENTER, index);
        } else {
            item = new TableItem(attackTable, SWT.CENTER);
        }
        item.setText(1, attackEvent.getSource_name());
        item.setText(2, attackEvent.getSource());
        item.setText(3, attackEvent.getResult());
        item.setText(4, attackEvent.getApplication().getName());
        item.setText(5, attackEvent.getServer().getName());
        item.setText(6, attackEvent.getRule());
        item.setText(7, attackEvent.getFormatReceived());
        item.setText(8, attackEvent.getUrl());
        item.setText(9, attackEvent.getUser_input().getValue());
        String tags = String.join(",", attackEvent.getTags());
        item.setText(10, tags);
        item.setText(11, attackEvent.getOrganization().getName());
    }

    private void addColToServerTable(Server server, int index) {
        if (server == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(serverTable, SWT.CENTER, index);
        } else {
            item = new TableItem(serverTable, SWT.CENTER);
        }
        item.setText(1, server.getName());
        item.setText(2, server.getPath());
        item.setText(3, server.getLanguage());
        item.setText(4, server.getAgent_version());
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
        return ps;
    }

    public Organization getValidOrganization() {
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
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
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
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

    private void updateProtectOption() {
        this.attackEventDetectedFilterMap = getAttackEventDetectedDateMap();
        attackTermToday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")));
        attackTermYesterday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")));
        attackTerm30days.setToolTipText(String.format("%s ??? ??????",
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))));
        attackTermLastWeek.setToolTipText(String.format("%s ??? %s",
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")),
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))));
        attackTermThisWeek.setToolTipText(String.format("%s ??? %s",
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")),
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))));
    }

    private Date[] getFrToDetectedDate() {
        int idx = -1;
        for (Button termBtn : this.attackTermRadios) {
            if (termBtn.getSelection()) {
                idx = attackTermRadios.indexOf(termBtn);
                break;
            }
        }
        if (idx < 0) {
            idx = 0;
        }
        LocalDate frLocalDate = null;
        LocalDate toLocalDate = null;
        switch (idx) {
            case 0: // 30days
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                break;
            case 1: // Yesterday
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY);
                break;
            case 2: // Today
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                break;
            case 3: // LastWeek
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_START);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_END);
                break;
            case 4: // ThisWeek
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_START);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_END);
                break;
            case 5: // Specify
                if (frDetectedDate == null || toDetectedDate == null) {
                    return new Date[] {};
                }
                return new Date[] { frDetectedDate, toDetectedDate };
            default:
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
        }
        Date frDate = Date.from(frLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.set(toLocalDate.getYear(), toLocalDate.getMonthValue() - 1, toLocalDate.getDayOfMonth(), 23, 59, 59);
        Date toDate = cal.getTime();
        return new Date[] { frDate, toDate };
    }

    public Map<AttackEventDetectedDateFilterEnum, LocalDate> getAttackEventDetectedDateMap() {
        Map<AttackEventDetectedDateFilterEnum, LocalDate> map = new HashMap<AttackEventDetectedDateFilterEnum, LocalDate>();
        LocalDate today = LocalDate.now();
        map.put(AttackEventDetectedDateFilterEnum.TODAY, today);
        map.put(AttackEventDetectedDateFilterEnum.YESTERDAY, today.minusDays(1));
        map.put(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS, today.minusDays(30));
        LocalDate lastWeekStart = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        lastWeekStart = lastWeekStart.minusDays(7 - ps.getInt(PreferenceConstants.ATTACK_START_WEEKDAY));
        if (lastWeekStart.plusDays(7).isAfter(today)) {
            lastWeekStart = lastWeekStart.minusDays(7);
        }
        map.put(AttackEventDetectedDateFilterEnum.LAST_WEEK_START, lastWeekStart);
        map.put(AttackEventDetectedDateFilterEnum.LAST_WEEK_END, lastWeekStart.plusDays(6));
        map.put(AttackEventDetectedDateFilterEnum.THIS_WEEK_START, lastWeekStart.plusDays(7));
        map.put(AttackEventDetectedDateFilterEnum.THIS_WEEK_END, lastWeekStart.plusDays(14));
        return map;
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
            this.shell.setText(String.format(WINDOW_TITLE, "???????????????"));
        } else {
            this.shell.setText(String.format(WINDOW_TITLE, text));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("attackEventFilter".equals(event.getPropertyName())) {
            Map<FilterEnum, Set<Filter>> filterMap = (Map<FilterEnum, Set<Filter>>) event.getNewValue();
            attackTable.clearAll();
            attackTable.removeAll();
            filteredAttackEvents.clear();
            for (AttackEvent attackEvent : attackEvents) {
                boolean lostFlg = false;
                for (Filter filter : filterMap.get(FilterEnum.SOURCE_NAME)) {
                    if (attackEvent.getSource_name().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.SOURCE_IP)) {
                    if (attackEvent.getSource().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.APPLICATION)) {
                    if (attackEvent.getApplication().getName().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.RULE)) {
                    if (attackEvent.getRule().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.TAG)) {
                    if (filter.getLabel().equals("")) {
                        if (attackEvent.getTags().isEmpty()) {
                            if (!filter.isValid()) {
                                lostFlg |= true;
                            }
                        }
                    }
                    if (attackEvent.getTags() != null && attackEvent.getTags().contains(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }

                // ?????????????????????
                if (filterMap.containsKey(FilterEnum.BUSINESS_HOURS)) {
                    int target = Integer.parseInt(attackEvent.getTimeStrReceived());
                    String termDayTime = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                    String termNightTime = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                    for (Filter filter : filterMap.get(FilterEnum.BUSINESS_HOURS)) {
                        if (filter.isValid()) {
                            continue;
                        }
                        if (filter.getLabel().equals("???????????????")) {
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0];
                                String toDtStr = termDayTime.split("-")[1];
                                int frDt = Integer.parseInt(frDtStr);
                                int toDt = Integer.parseInt(toDtStr);
                                if (toDt < frDt) {
                                    if ((frDt <= target && target < 2400) || 0 <= target && target < toDt) {
                                        lostFlg |= true;
                                    }
                                } else {
                                    if (frDt <= target && target < toDt) {
                                        lostFlg |= true;
                                    }
                                }
                            }
                        }
                        if (filter.getLabel().equals("???????????????")) {
                            if (!termNightTime.isEmpty()) {
                                String frNtStr = termNightTime.split("-")[0];
                                String toNtStr = termNightTime.split("-")[1];
                                int frNt = Integer.parseInt(frNtStr);
                                int toNt = Integer.parseInt(toNtStr);
                                if (toNt < frNt) {
                                    if ((frNt <= target && target < 2400) || 0 <= target && target < toNt) {
                                        lostFlg |= true;
                                    }
                                } else {
                                    if (frNt <= target && target < toNt) {
                                        lostFlg |= true;
                                    }
                                }
                            }
                        }
                        if (filter.getLabel().equals("??????????????????")) {
                            boolean hitFlg = false;
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0];
                                String toDtStr = termDayTime.split("-")[1];
                                int frDt = Integer.parseInt(frDtStr);
                                int toDt = Integer.parseInt(toDtStr);
                                if (toDt < frDt) {
                                    if ((frDt <= target && target < 2400) || 0 <= target && target < toDt) {
                                        hitFlg |= true;
                                    }
                                } else {
                                    if (frDt <= target && target < toDt) {
                                        hitFlg |= true;
                                    }
                                }
                            }
                            if (!termNightTime.isEmpty()) {
                                String frNtStr = termNightTime.split("-")[0];
                                String toNtStr = termNightTime.split("-")[1];
                                int frNt = Integer.parseInt(frNtStr);
                                int toNt = Integer.parseInt(toNtStr);
                                if (toNt < frNt) {
                                    if ((frNt <= target && target < 2400) || 0 <= target && target < toNt) {
                                        hitFlg |= true;
                                    }
                                } else {
                                    if (frNt <= target && target < toNt) {
                                        hitFlg |= true;
                                    }
                                }
                            }
                            if (!hitFlg) {
                                lostFlg |= true;
                            }
                        }
                    }
                }
                if (!lostFlg) {
                    addColToAttackTable(attackEvent, -1);
                    filteredAttackEvents.add(attackEvent);
                }
            }
            attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size()));
        } else if ("serverFilter".equals(event.getPropertyName())) {
            Map<FilterEnum, Set<Filter>> filterMap = (Map<FilterEnum, Set<Filter>>) event.getNewValue();
            serverTable.clearAll();
            serverTable.removeAll();
            filteredServers.clear();
            for (Server server : servers) {
                boolean lostFlg = false;
                for (Filter filter : filterMap.get(FilterEnum.LANGUAGE)) {
                    if (server.getLanguage().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.AGENT_VERSION)) {
                    if (server.getAgent_version().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                if (!lostFlg) {
                    addColToServerTable(server, -1);
                    filteredServers.add(server);
                }
            }
        } else if ("tsv".equals(event.getPropertyName())) {
            System.out.println("tsv main");
        }

    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void setCookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
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
