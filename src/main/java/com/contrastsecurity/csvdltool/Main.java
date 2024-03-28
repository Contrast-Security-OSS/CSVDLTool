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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
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

import com.contrastsecurity.csvdltool.api.AccountsApi;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackEventTagsApi;
import com.contrastsecurity.csvdltool.api.LogoutApi;
import com.contrastsecurity.csvdltool.api.PutTagsToAttackEventsApi;
import com.contrastsecurity.csvdltool.api.ServerlessTokenApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.json.ServerlessTokenJson;
import com.contrastsecurity.csvdltool.model.Account;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.AttackEventCSVColumn;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.ServerCSVColumn;
import com.contrastsecurity.csvdltool.model.serverless.Function;
import com.contrastsecurity.csvdltool.model.serverless.Result;
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

import okhttp3.CookieJar;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "CSVDLTool - %s"; //$NON-NLS-1$
    // 以下のMASTER_PASSWORDはプロキシパスワードを保存する際に暗号化で使用するパスワードです。
    // 本ツールをリリース用にコンパイルする際はchangemeを別の文字列に置き換えてください。
    public static final String MASTER_PASSWORD = "changeme!"; //$NON-NLS-1$

    // 各出力ファイルの文字コード
    public static final String CSV_WIN_ENCODING = "Shift_JIS"; //$NON-NLS-1$
    public static final String CSV_MAC_ENCODING = "UTF-8"; //$NON-NLS-1$
    public static final String FILE_ENCODING = "UTF-8"; //$NON-NLS-1$

    public static final int MINIMUM_SIZE_WIDTH = 720;
    public static final int MINIMUM_SIZE_WIDTH_MAC = 720;
    public static final int MINIMUM_SIZE_HEIGHT = 670;

    private CSVDLToolShell shell;
    private PreferenceDialog preferenceDialog;
    private String validOrganizationsOldStr;

    // ASSESS
    private Button appLoadBtn;
    private Text srcListFilter;
    private Text srcListLanguagesFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private CTabFolder mainTabFolder;
    private CTabFolder subTabFolder;

    private Button vulExecuteBtn;
    private Button vulOnlyParentAppChk;
    private Button vulOnlyCurVulExpChk;
    private Button includeDescChk;
    private Button includeStackTraceChk;

    private Button libExecuteBtn;
    private Button onlyHasCVEChk;
    private Button withEPSSInfoChk;
    private Button includeCVEDetailChk;

    private Button attackLoadBtn;

    private Button serverLoadBtn;

    private Button settingBtn;
    private Button logOutBtn;

    private Label statusBar;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)"); //$NON-NLS-1$
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

    // SERVERLESS
    private List<Account> serverlessAccounts = new ArrayList<Account>();
    private int selectedAccountIndex;
    private Combo accountCombo;
    private Button serverlessResultLoadBtn;
    private Table resultTable;

    // SERVER
    private Table serverTable;
    private List<Server> servers;
    private List<Server> filteredServers = new ArrayList<Server>();

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private CookieJar cookieJar;

    public enum AuthType {
        TOKEN,
        PASSWORD
    }

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    private AuthType authType;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.authType = AuthType.TOKEN;
        if (System.getProperty("auth") != null && System.getProperty("auth").equals("password")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            main.authType = AuthType.PASSWORD;
        }
        main.initialize();
        main.createPart();
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setCookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    public CookieJar getCookieJar() {
        return this.cookieJar;
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
            this.ps = new PreferenceStore(homeDir + System.getProperty("file.separator") + "csvdltool.properties"); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps.save();
            }
            String outDirPath = this.ps.getString(PreferenceConstants.FILE_OUT_DIR);
            if (outDirPath != null && !outDirPath.isEmpty()) {
                if (!Files.isWritable(Paths.get(outDirPath))) {
                    this.ps.setValue(PreferenceConstants.FILE_OUT_DIR, "");
                }
            }
        } catch (FileNotFoundException fnfe) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PASS_TYPE, "input"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.AUTO_RELOGIN_INTERVAL, 105);
            this.ps.setDefault(PreferenceConstants.AUTH_RETRY_MAX, 3);

            this.ps.setDefault(PreferenceConstants.VUL_ONLY_CURVUL_EXP, true);

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_VUL, VulCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_VUL, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_VUL, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_VUL, "'vul'_yyyy-MM-dd_HHmmss"); //$NON-NLS-1$

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_LIB, LibCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_LIB, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_LIB, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_LIB, "'lib'_yyyy-MM-dd_HHmmss"); //$NON-NLS-1$

            this.ps.setDefault(PreferenceConstants.ATTACK_RANGE_DAYTIME, "0900-1800"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.ATTACK_RANGE_NIGHTTIME, "1800-0000"); //$NON-NLS-1$
            this.ps.setDefault(PreferenceConstants.ATTACK_START_WEEKDAY, 1); // 月曜日
            this.ps.setDefault(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER, 0);
            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_ATTACKEVENT, AttackEventCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_ATTACKEVENT, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT, "'attackevent'_yyyy-MM-dd_HHmmss"); //$NON-NLS-1$

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_SERVER, ServerCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_SERVER, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_SERVER, "'server'_yyyy-MM-dd_HHmmss"); //$NON-NLS-1$

            this.ps.setDefault(PreferenceConstants.OPENED_MAIN_TAB_IDX, 0);
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml"); //$NON-NLS-1$
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.ps.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.ps.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
            this.ps.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            if (this.authType == AuthType.PASSWORD) {
                this.ps.setValue(PreferenceConstants.SERVICE_KEY, ""); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new CSVDLToolShell(display, this);
        if (OS.isFamilyMac()) {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH_MAC, MINIMUM_SIZE_HEIGHT);
        } else {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        }
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png")); //$NON-NLS-1$
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png")); //$NON-NLS-1$
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png")); //$NON-NLS-1$
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png")); //$NON-NLS-1$
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png")); //$NON-NLS-1$
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
                setValidOrganizationsOldStr(new Gson().toJson(getValidOrganizations()));
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
                ps.setValue(PreferenceConstants.VUL_ONLY_CURVUL_EXP, vulOnlyCurVulExpChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_DESCRIPTION, includeDescChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_STACKTRACE, includeStackTraceChk.getSelection());
                ps.setValue(PreferenceConstants.ONLY_HAS_CVE, onlyHasCVEChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
                ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.XSRF_TOKEN, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.TSV_STATUS, ""); //$NON-NLS-1$
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
                boolean ngRequiredFields = false;
                String url = ps.getString(PreferenceConstants.CONTRAST_URL);
                String usr = ps.getString(PreferenceConstants.USERNAME);
                if (authType == AuthType.PASSWORD) {
                    if (url.isEmpty() || usr.isEmpty()) {
                        ngRequiredFields = true;
                    }
                } else {
                    String svc = ps.getString(PreferenceConstants.SERVICE_KEY);
                    if (url.isEmpty() || usr.isEmpty() || svc.isEmpty()) {
                        ngRequiredFields = true;
                    }
                }
                List<Organization> orgs = getValidOrganizations();
                if (ngRequiredFields || orgs.isEmpty()) {
                    appLoadBtn.setEnabled(false);
                    vulExecuteBtn.setEnabled(false);
                    attackLoadBtn.setEnabled(false);
                    serverLoadBtn.setEnabled(false);
                    settingBtn.setText(Messages.getString("main.settings.initial.button.title")); //$NON-NLS-1$
                    uiReset();
                } else {
                    String validOrganizationsNewStr = new Gson().toJson(orgs);
                    if (!validOrganizationsNewStr.equals(getValidOrganizationsOldStr())) {
                        ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
                        uiReset();
                    }
                    appLoadBtn.setEnabled(true);
                    vulExecuteBtn.setEnabled(true);
                    attackLoadBtn.setEnabled(true);
                    serverLoadBtn.setEnabled(true);
                    settingBtn.setText(Messages.getString("main.settings.button.title")); //$NON-NLS-1$
                }
                updateProtectOption();
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
                    String proxy_usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String proxy_pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (proxy_usr == null || proxy_usr.isEmpty() || proxy_pwd == null || proxy_pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
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
                        support.firePropertyChange("userswitch", 0, num); //$NON-NLS-1$
                    }
                }
            }
        };
        display.addFilter(SWT.KeyUp, listener);

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 0;
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

        Font bigFont = new Font(display, "Arial", 20, SWT.NORMAL);

        // #################### ASSESS #################### //
        CTabItem assessTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        assessTabItem.setText(Messages.getString("main.tab.assess.title")); //$NON-NLS-1$
        assessTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-assess-iast-02.png"))); //$NON-NLS-1$

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
        appLoadBtn.setText(Messages.getString("main.application.load.button.title")); //$NON-NLS-1$
        appLoadBtn.setToolTipText(Messages.getString("main.application.load.button.tooltip")); //$NON-NLS-1$
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
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(shell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullAppMap = progress.getFullAppMap();
                if (fullAppMap.isEmpty()) {
                    String userName = ps.getString(PreferenceConstants.USERNAME);
                    StringJoiner sj = new StringJoiner("\r\n"); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.1")); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.2")); //$NON-NLS-1$
                    sj.add(String.format("　%s", userName)); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.3")); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.4")); //$NON-NLS-1$
                    MessageDialog.openInformation(shell, Messages.getString("main.application.load.message.dialog.title"), sj.toString()); //$NON-NLS-1$
                }
                for (String appLabel : fullAppMap.keySet()) {
                    srcList.add(appLabel); // UI list
                    srcApps.add(appLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                assessFilterMap = progress.getFilterMap();
                vulSeverityFilterTxt.setText(Messages.getString("main.vul.filter.condition.severity.all")); //$NON-NLS-1$
                vulVulnTypeFilterTxt.setText(Messages.getString("main.vul.filter.condition.vulntype.all")); //$NON-NLS-1$
            }
        });

        Composite srcGrp = new Composite(appListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(2, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage(Messages.getString("main.src.apps.filter.name.message")); //$NON-NLS-1$
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });

        srcListLanguagesFilter = new Text(srcGrp, SWT.BORDER);
        GridData srcListLanguagesFilterGrDt = new GridData();
        srcListLanguagesFilterGrDt.minimumWidth = 40;
        srcListLanguagesFilter.setLayoutData(srcListLanguagesFilterGrDt);
        srcListLanguagesFilter.setMessage(Messages.getString("main.src.apps.filter.language.message")); //$NON-NLS-1$
        srcListLanguagesFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });

        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData srcListGrDt = new GridData(GridData.FILL_BOTH);
        srcListGrDt.horizontalSpan = 2;
        this.srcList.setLayoutData(srcListGrDt);
        this.srcList.setToolTipText(Messages.getString("main.available.app.list.tooltip")); //$NON-NLS-1$
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
        GridData srcListLblCompGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcListLblCompGrDt.horizontalSpan = 2;
        srcListLblComp.setLayoutData(srcListLblCompGrDt);
        Label srcListDescLbl = new Label(srcListLblComp, SWT.LEFT);
        GridData srcListDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcListDescLblGrDt.minimumHeight = 12;
        srcListDescLbl.setLayoutData(srcListDescLblGrDt);
        srcListDescLbl.setFont(new Font(display, "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        srcListDescLbl.setText(Messages.getString("main.available.app.list.count.label")); //$NON-NLS-1$
        srcListDescLbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.srcCount = new Label(srcListLblComp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 12;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(display, "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.srcCount.setText("0"); //$NON-NLS-1$
        this.srcCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        Composite btnGrp = new Composite(appListGrp, SWT.NONE);
        btnGrp.setLayout(new GridLayout(1, false));
        GridData btnGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        btnGrpGrDt.verticalAlignment = SWT.CENTER;
        btnGrp.setLayoutData(btnGrpGrDt);

        Button allRightBtn = new Button(btnGrp, SWT.PUSH);
        allRightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allRightBtn.setText(">>"); //$NON-NLS-1$
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
        rightBtn.setText(">"); //$NON-NLS-1$
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
        leftBtn.setText("<"); //$NON-NLS-1$
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
        allLeftBtn.setText("<<"); //$NON-NLS-1$
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
        dstListFilter.setMessage(Messages.getString("main.dst.apps.filter.name.message")); //$NON-NLS-1$
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
        this.dstList.setToolTipText(Messages.getString("main.selected.app.list.tooltip")); //$NON-NLS-1$
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
        dstListDescLbl.setLayoutData(dstListDescLblGrDt);
        dstListDescLbl.setFont(new Font(display, "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        dstListDescLbl.setText(Messages.getString("main.selected.app.list.count.label")); //$NON-NLS-1$
        dstListDescLbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.dstCount = new Label(dstListLblComp, SWT.RIGHT);
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.minimumHeight = 12;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setFont(new Font(display, "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.dstCount.setText("0"); //$NON-NLS-1$
        this.dstCount.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        subTabFolder = new CTabFolder(assessShell, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_HORIZONTAL);
        subTabFolder.setLayoutData(tabFolderGrDt);
        subTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        // #################### 脆弱性 #################### //
        CTabItem vulTabItem = new CTabItem(subTabFolder, SWT.NONE);
        vulTabItem.setText(Messages.getString("main.vul.tab.title")); //$NON-NLS-1$

        // ========== グループ ==========
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
        vulFilterGrp.setText(Messages.getString("main.vul.filter.condition.group.title")); //$NON-NLS-1$

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.severity.label")); //$NON-NLS-1$
        vulSeverityFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulSeverityFilterTxt.setText(Messages.getString("main.load.application.message")); //$NON-NLS-1$
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
                        vulSeverityFilterTxt.setText(Messages.getString("main.vul.filter.condition.severity.all")); //$NON-NLS-1$
                    } else {
                        vulSeverityFilterTxt.setText(String.join(", ", labels)); //$NON-NLS-1$
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.vulntype.label")); //$NON-NLS-1$
        vulVulnTypeFilterTxt = new Text(vulFilterGrp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        vulVulnTypeFilterTxt.setText(Messages.getString("main.load.application.message")); //$NON-NLS-1$
        vulVulnTypeFilterTxt.setEditable(false);
        GridData vulVulnTypeFilterTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        vulVulnTypeFilterTxtGrDt.minimumHeight = 2 * vulVulnTypeFilterTxt.getLineHeight();
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
                        vulVulnTypeFilterTxt.setText(Messages.getString("main.vul.filter.condition.vulntype.all")); //$NON-NLS-1$
                    } else {
                        vulVulnTypeFilterTxt.setText(String.join(", ", labels)); //$NON-NLS-1$
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.lastdetected.label")); //$NON-NLS-1$
        vulLastDetectedFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulLastDetectedFilterTxt.setText(Messages.getString("main.vul.filter.condition.lastdetected.all")); //$NON-NLS-1$
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
                    vulLastDetectedFilterTxt.setText(String.format("%s ～ %s", sdf.format(frLastDetectedDate), sdf.format(toLastDetectedDate))); //$NON-NLS-1$
                } else if (frLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("%s ～", sdf.format(frLastDetectedDate))); //$NON-NLS-1$
                } else if (toLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("～ %s", sdf.format(toLastDetectedDate))); //$NON-NLS-1$
                } else {
                    vulLastDetectedFilterTxt.setText(Messages.getString("main.vul.filter.condition.lastdetected.all")); //$NON-NLS-1$
                }
                vulExecuteBtn.setFocus();
            }
        });

        // ========== 取得ボタン ==========
        vulExecuteBtn = new Button(vulButtonGrp, SWT.PUSH);
        GC gc = new GC(vulExecuteBtn);
        gc.setFont(bigFont);
        Point bigBtnSize = gc.textExtent(Messages.getString("main.vul.export.button.title"));
        gc.dispose();
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.minimumHeight = 50;
        executeBtnGrDt.heightHint = bigBtnSize.y + 20;
        vulExecuteBtn.setLayoutData(executeBtnGrDt);
        vulExecuteBtn.setText(Messages.getString("main.vul.export.button.title")); //$NON-NLS-1$
        vulExecuteBtn.setToolTipText(Messages.getString("main.vul.export.button.tooltip")); //$NON-NLS-1$
        vulExecuteBtn.setFont(new Font(display, "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        vulExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.export.application.unselected.error.message")); //$NON-NLS-1$
                    return;
                }
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = getOutDirPath();
                }
                if (outDirPath == null || outDirPath.isEmpty()) {
                    return;
                }
                VulGetWithProgress progress = new VulGetWithProgress(shell, ps, outDirPath, dstApps, fullAppMap, assessFilterMap, frLastDetectedDate, toLastDetectedDate,
                        vulOnlyParentAppChk.getSelection(), vulOnlyCurVulExpChk.getSelection(), includeDescChk.getSelection(), includeStackTraceChk.getSelection());
                ProgressMonitorDialog progDialog = new VulGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), exceptionMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, trace, exceptionMsg);
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.vul.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.vul.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(shell, Messages.getString("main.application.load.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        vulOnlyParentAppChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyParentAppChk.setText(Messages.getString("main.vul.export.option.only.parent.application")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.VUL_ONLY_PARENT_APP)) {
            vulOnlyParentAppChk.setSelection(true);
        }

        vulOnlyCurVulExpChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyCurVulExpChk.setText(Messages.getString("main.vul.export.option.only.latest")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.VUL_ONLY_CURVUL_EXP)) {
            vulOnlyCurVulExpChk.setSelection(true);
        }

        includeDescChk = new Button(vulButtonGrp, SWT.CHECK);
        includeDescChk.setText(Messages.getString("main.vul.export.option.include.detail")); //$NON-NLS-1$
        includeDescChk.setToolTipText(Messages.getString("main.vul.export.option.include.detail.hint")); //$NON-NLS-1$
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
        includeStackTraceChk.setText(Messages.getString("main.vul.export.option.include.stacktrace")); //$NON-NLS-1$
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

        // #################### ライブラリ #################### //
        CTabItem libTabItem = new CTabItem(subTabFolder, SWT.NONE);
        libTabItem.setText(Messages.getString("main.lib.tab.title")); //$NON-NLS-1$

        // ========== グループ ==========
        Composite libButtonGrp = new Composite(subTabFolder, SWT.NULL);
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
        libExecuteBtnGrDt.minimumHeight = 50;
        libExecuteBtnGrDt.heightHint = bigBtnSize.y + 20;
        libExecuteBtn.setLayoutData(libExecuteBtnGrDt);
        libExecuteBtn.setText(Messages.getString("main.lib.export.button.title")); //$NON-NLS-1$
        libExecuteBtn.setToolTipText(Messages.getString("main.lib.export.button.tooltip")); //$NON-NLS-1$
        libExecuteBtn.setFont(new Font(display, "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        libExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(shell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.export.application.unselected.error.message")); //$NON-NLS-1$
                    return;
                }
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = getOutDirPath();
                }
                if (outDirPath == null || outDirPath.isEmpty()) {
                    return;
                }
                LibGetWithProgress progress = new LibGetWithProgress(shell, ps, outDirPath, dstApps, fullAppMap, onlyHasCVEChk.getSelection(), withEPSSInfoChk.getSelection(),
                        includeCVEDetailChk.getSelection());
                ProgressMonitorDialog progDialog = new LibGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), exceptionMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(shell, trace, exceptionMsg);
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(shell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(shell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        onlyHasCVEChk = new Button(libButtonGrp, SWT.CHECK);
        onlyHasCVEChk.setText(Messages.getString("main.lib.export.option.only.has.cve")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            onlyHasCVEChk.setSelection(true);
        }
        withEPSSInfoChk = new Button(libButtonGrp, SWT.CHECK);
        withEPSSInfoChk.setText(Messages.getString("main.lib.export.option.with.epss")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            withEPSSInfoChk.setSelection(true);
        }
        includeCVEDetailChk = new Button(libButtonGrp, SWT.CHECK);
        includeCVEDetailChk.setText(Messages.getString("main.lib.export.option.include.detail")); //$NON-NLS-1$
        includeCVEDetailChk.setToolTipText(Messages.getString("main.lib.export.option.include.detail.tooltip")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_CVE_DETAIL)) {
            includeCVEDetailChk.setSelection(true);
        }
        libTabItem.setControl(libButtonGrp);

        int sub_idx = this.ps.getInt(PreferenceConstants.OPENED_SUB_TAB_IDX);
        subTabFolder.setSelection(sub_idx);

        assessTabItem.setControl(assessShell);

        // #################### PROTECT #################### //
        CTabItem protectTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        protectTabItem.setText(Messages.getString("main.tab.protect.title")); //$NON-NLS-1$
        protectTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-protect-rasp-02.png"))); //$NON-NLS-1$

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
        attackTerm30days.setText(Messages.getString("main.attackevent.data.range.radio.all")); //$NON-NLS-1$
        attackTermRadios.add(attackTerm30days);
        attackTermYesterday = new Button(attackTermGrp, SWT.RADIO);
        attackTermYesterday.setText(Messages.getString("main.attackevent.data.range.radio.yesterday")); //$NON-NLS-1$
        attackTermRadios.add(attackTermYesterday);
        attackTermToday = new Button(attackTermGrp, SWT.RADIO);
        attackTermToday.setText(Messages.getString("main.attackevent.data.range.radio.today")); //$NON-NLS-1$
        attackTermRadios.add(attackTermToday);
        attackTermLastWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermLastWeek.setText(Messages.getString("main.attackevent.data.range.radio.lastweek")); //$NON-NLS-1$
        attackTermRadios.add(attackTermLastWeek);
        attackTermThisWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermThisWeek.setText(Messages.getString("main.attackevent.data.range.radio.thisweek")); //$NON-NLS-1$
        attackTermRadios.add(attackTermThisWeek);
        attackTermPeriod = new Button(attackTermGrp, SWT.RADIO);
        attackTermPeriod.setText(Messages.getString("main.attackevent.data.range.radio.custom")); //$NON-NLS-1$
        attackTermPeriod.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (attackTermPeriod.getSelection()) {
                    long frLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR);
                    long toLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO);
                    Date fr = frLong > 0 ? new Date(frLong) : null;
                    Date to = frLong > 0 ? new Date(toLong) : null;
                    if (fr == null && to == null) {
                        attackDetectedTermUpdate();
                        attackLoadBtn.setFocus();
                    }
                    attackDetectedFilterTxt.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
                } else {
                    attackDetectedFilterTxt.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
                }
            }
        });
        attackTermRadios.add(attackTermPeriod);
        attackDetectedFilterTxt = new Text(attackTermGrp, SWT.BORDER);
        long frLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR);
        long toLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO);
        this.frDetectedDate = frLong > 0 ? new Date(frLong) : null;
        this.toDetectedDate = frLong > 0 ? new Date(toLong) : null;
        attackDetectedTermTextSet(this.frDetectedDate, this.toDetectedDate);
        attackDetectedFilterTxt.setEditable(false);
        attackDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        attackDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                attackDetectedTermUpdate();
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
        if (attackTermPeriod.getSelection()) {
            attackDetectedFilterTxt.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        } else {
            attackDetectedFilterTxt.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
        }

        attackLoadBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackLoadBtnGrDt.horizontalSpan = 3;
        attackLoadBtnGrDt.minimumHeight = 50;
        attackLoadBtnGrDt.heightHint = bigBtnSize.y + 20;
        attackLoadBtn.setLayoutData(attackLoadBtnGrDt);
        attackLoadBtn.setText(Messages.getString("main.attackevent.load.button.title")); //$NON-NLS-1$
        attackLoadBtn.setToolTipText(Messages.getString("main.attackevent.load.button.tooltip")); //$NON-NLS-1$
        attackLoadBtn.setFont(new Font(display, "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        attackLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filteredAttackEvents.clear();
                attackTable.clearAll();
                attackTable.removeAll();
                Date[] frToDate = getFrToDetectedDate();
                if (frToDate.length != 2) {
                    MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.load.message.dialog.undefined.term.error.message")); //$NON-NLS-1$
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
                    attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size())); //$NON-NLS-1$
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
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(shell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
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
        this.attackEventCount.setLayoutData(attackEventCountGrDt);
        this.attackEventCount.setFont(new Font(display, "Arial", 10, SWT.NORMAL)); //$NON-NLS-1$
        this.attackEventCount.setText("0/0"); //$NON-NLS-1$

        attackTable = new Table(attackListGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        attackTable.setLayoutData(tableGrDt);
        attackTable.setLinesVisible(true);
        attackTable.setHeaderVisible(true);
        Menu menuTable = new Menu(attackTable);
        attackTable.setMenu(menuTable);

        MenuItem miTag = new MenuItem(menuTable, SWT.NONE);
        miTag.setText(Messages.getString("main.attackevent.menu.item.edit.tag")); //$NON-NLS-1$
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
                            MessageDialog.openInformation(shell, Messages.getString("main.attackevent.message.dialog.edit.tag.title"), //$NON-NLS-1$
                                    Messages.getString("main.attackevent.message.dialog.edit.tag.message")); //$NON-NLS-1$
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        MenuItem miExp = new MenuItem(menuTable, SWT.NONE);
        miExp.setText(Messages.getString("main.attackevent.menu.item.export.csv")); //$NON-NLS-1$
        miExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = getOutDirPath();
                }
                int[] selectIndexes = attackTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String filePath = timestamp + ".csv"; //$NON-NLS-1$
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                filePath = outDirPath + System.getProperty("file.separator") + filePath;
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_ATTACKEVENT);
                List<AttackEventCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<AttackEventCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.message.dialog.json.load.error.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.attackevent.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
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
                                // ==================== 01. ソース名 ====================
                                csvLineList.add(attackEvent.getSource_name());
                                break;
                            case ATTACK_EVENT_02:
                                // ==================== 02. ソースIP ====================
                                csvLineList.add(attackEvent.getSource());
                                break;
                            case ATTACK_EVENT_03:
                                // ==================== 03. 結果 ====================
                                csvLineList.add(attackEvent.getResult());
                                break;
                            case ATTACK_EVENT_04:
                                // ==================== 04. アプリケーション ====================
                                csvLineList.add(attackEvent.getApplication().getName());
                                break;
                            case ATTACK_EVENT_05:
                                // ==================== 05. サーバ ====================
                                csvLineList.add(attackEvent.getServer().getName());
                                break;
                            case ATTACK_EVENT_06:
                                // ==================== 06. ルール ====================
                                csvLineList.add(attackEvent.getRule());
                                break;
                            case ATTACK_EVENT_07:
                                // ==================== 07. 時間 ====================
                                csvLineList.add(attackEvent.getFormatReceived());
                                break;
                            case ATTACK_EVENT_08:
                                // ==================== 08. URL ====================
                                csvLineList.add(attackEvent.getUrl());
                                break;
                            case ATTACK_EVENT_09:
                                // ==================== 09. 攻撃値 ====================
                                csvLineList.add(attackEvent.getUser_input().getValue());
                                break;
                            case ATTACK_EVENT_10:
                                // ==================== 10. タグ ====================
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), attackEvent.getTags())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                break;
                            case ATTACK_EVENT_11:
                                // ==================== 11. 組織名 ====================
                                csvLineList.add(attackEvent.getOrganization().getName());
                                break;
                            case ATTACK_EVENT_12:
                                // ==================== 12. 組織ID ====================
                                csvLineList.add(attackEvent.getOrganization().getOrganization_uuid());
                                break;
                            case ATTACK_EVENT_13: {
                                // ==================== 13. 攻撃イベントへのリンク ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(link);
                                break;
                            }
                            case ATTACK_EVENT_14: {
                                // ==================== 14. 攻撃イベントへのリンク（ハイパーリンク） ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(String.format("=HYPERLINK(\"%s\",\"%s\")", link, Messages.getString("main.to.teamserver.hyperlink.text"))); //$NON-NLS-1$//$NON-NLS-2$
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
                    MessageDialog.openInformation(shell, Messages.getString("main.attackevent.message.dialog.export.csv.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.message.dialog.export.csv.message")); //$NON-NLS-1$
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miReport = new MenuItem(menuTable, SWT.NONE);
        miReport.setText(Messages.getString("main.attackevent.menu.item.output.report")); //$NON-NLS-1$
        miReport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = getOutDirPath();
                }
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
                String timestamp = new SimpleDateFormat("'protect_report'_yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                String filePath = timestamp + ".txt"; //$NON-NLS-1$
                String txt_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    txt_encoding = Main.CSV_MAC_ENCODING;
                }
                filePath = outDirPath + System.getProperty("file.separator") + filePath;
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File f = new File(filePath);
                try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), txt_encoding)))) {
                    for (String srcIp : srcIpMap.keySet()) {
                        printWriter.println(String.format("- %s", srcIp)); //$NON-NLS-1$
                        Map<String, Integer> ruleMap = srcIpMap.get(srcIp);
                        for (String rule : ruleMap.keySet()) {
                            int cnt = ruleMap.get(rule).intValue();
                            if (cnt > 0) {
                                printWriter.println(String.format("  - %s: %d", rule, cnt)); //$NON-NLS-1$
                            }
                        }
                    }
                    MessageDialog.openInformation(shell, Messages.getString("main.attackevent.message.dialog.export.txt.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.message.dialog.export.txt.message")); //$NON-NLS-1$
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miJump = new MenuItem(menuTable, SWT.NONE);
        miJump.setText(Messages.getString("main.attackevent.menu.item.browser.open")); //$NON-NLS-1$
        miJump.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                if (selectIndexes.length > 10) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    messageBox.setText(Messages.getString("main.attackevent.message.dialog.browser.open.title")); //$NON-NLS-1$
                    messageBox.setMessage(String.format(Messages.getString("main.attackevent.message.dialog.browser.open.confirm.message"), selectIndexes.length)); //$NON-NLS-1$
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
                        desktop.browse(new URI(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid))); //$NON-NLS-1$
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (URISyntaxException urise) {
                    urise.printStackTrace();
                }
            }
        });

        MenuItem miUrlCopy = new MenuItem(menuTable, SWT.NONE);
        miUrlCopy.setText(Messages.getString("main.attackevent.menu.item.copy.teamserver.url")); //$NON-NLS-1$
        miUrlCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectIndex = attackTable.getSelectionIndex();
                AttackEvent attackEvent = filteredAttackEvents.get(selectIndex);
                String contrastUrl = ps.getString(PreferenceConstants.CONTRAST_URL);
                String orgUuid = attackEvent.getOrganization().getOrganization_uuid();
                String eventUuid = attackEvent.getEvent_uuid();
                Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid)); //$NON-NLS-1$
                clipboard.setContents(selection, null);
            }
        });

        MenuItem miSelectAll = new MenuItem(menuTable, SWT.NONE);
        miSelectAll.setText(Messages.getString("main.attackevent.menu.item.select.all")); //$NON-NLS-1$
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
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && e.keyCode == 'a') {
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
        column1.setText(Messages.getString("main.attackevent.table.column0.title")); //$NON-NLS-1$
        TableColumn column2 = new TableColumn(attackTable, SWT.LEFT);
        column2.setWidth(120);
        column2.setText(Messages.getString("main.attackevent.table.column1.title")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(attackTable, SWT.CENTER);
        column3.setWidth(100);
        column3.setText(Messages.getString("main.attackevent.table.column2.title")); //$NON-NLS-1$
        TableColumn column4 = new TableColumn(attackTable, SWT.LEFT);
        column4.setWidth(250);
        column4.setText(Messages.getString("main.attackevent.table.column3.title")); //$NON-NLS-1$
        TableColumn column5 = new TableColumn(attackTable, SWT.LEFT);
        column5.setWidth(200);
        column5.setText(Messages.getString("main.attackevent.table.column4.title")); //$NON-NLS-1$
        TableColumn column6 = new TableColumn(attackTable, SWT.LEFT);
        column6.setWidth(200);
        column6.setText(Messages.getString("main.attackevent.table.column5.title")); //$NON-NLS-1$
        TableColumn column7 = new TableColumn(attackTable, SWT.LEFT);
        column7.setWidth(150);
        column7.setText(Messages.getString("main.attackevent.table.column6.title")); //$NON-NLS-1$
        TableColumn column8 = new TableColumn(attackTable, SWT.LEFT);
        column8.setWidth(150);
        column8.setText(Messages.getString("main.attackevent.table.column7.title")); //$NON-NLS-1$
        TableColumn column9 = new TableColumn(attackTable, SWT.LEFT);
        column9.setWidth(250);
        column9.setText(Messages.getString("main.attackevent.table.column8.title")); //$NON-NLS-1$
        TableColumn column10 = new TableColumn(attackTable, SWT.LEFT);
        column10.setWidth(250);
        column10.setText(Messages.getString("main.attackevent.table.column9.title")); //$NON-NLS-1$
        TableColumn column11 = new TableColumn(attackTable, SWT.LEFT);
        column11.setWidth(150);
        column11.setText(Messages.getString("main.attackevent.table.column10.title")); //$NON-NLS-1$

        Button attackEventFilterBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackEventFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackEventFilterBtnGrDt.horizontalSpan = 3;
        attackEventFilterBtn.setLayoutData(attackEventFilterBtnGrDt);
        attackEventFilterBtn.setText(Messages.getString("main.attackevent.filter.button.title")); //$NON-NLS-1$
        attackEventFilterBtn.setToolTipText(Messages.getString("main.attackevent.filter.button.tooltip")); //$NON-NLS-1$
        attackEventFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (protectFilterMap == null) {
                    MessageDialog.openInformation(shell, Messages.getString("main.attackevent.filter.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.filter.not.loaded.error.message")); //$NON-NLS-1$
                    return;
                }
                String dayTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                String nightTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                if (!dayTimeHours.isEmpty() || !nightTimeHours.isEmpty()) {
                    Set<Filter> businessHoursFilterSet = new LinkedHashSet<Filter>();
                    if (!dayTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.daytime"))); //$NON-NLS-1$
                    }
                    if (!nightTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.nighttime"))); //$NON-NLS-1$
                    }
                    businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.othertime"))); //$NON-NLS-1$
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

        // #################### SERVERLESS #################### //
        CTabItem serverlessTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        serverlessTabItem.setText("SERVERLESS(β版)");
        serverlessTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-serverless-02.png"))); //$NON-NLS-1$

        Composite serverlessShell = new Composite(mainTabFolder, SWT.NONE);
        serverlessShell.setLayout(new GridLayout(1, false));

        Group serverlessGrp = new Group(serverlessShell, SWT.NONE);
        serverlessGrp.setLayout(new GridLayout(3, false));
        GridData serverlessGrpGrDt = new GridData(GridData.FILL_BOTH);
        serverlessGrpGrDt.minimumHeight = 200;
        serverlessGrp.setLayoutData(serverlessGrpGrDt);

        Group serverlessAccountGrp = new Group(serverlessGrp, SWT.NONE);
        serverlessAccountGrp.setLayout(new GridLayout(2, false));
        GridData serverlessAccountGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        serverlessAccountGrp.setLayoutData(serverlessAccountGrpGrDt);
        serverlessAccountGrp.setText("アカウント");

        accountCombo = new Combo(serverlessAccountGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData accountComboGrDt = new GridData(GridData.FILL_HORIZONTAL);
        accountCombo.setLayoutData(accountComboGrDt);
        this.selectedAccountIndex = -1;
        accountCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Combo combo = (Combo) event.getSource();
                selectedAccountIndex = combo.getSelectionIndex();
            }
        });

        Button accountLoadBtn = new Button(serverlessAccountGrp, SWT.PUSH);
        GridData accountLoadBtnGrDt = new GridData();
        accountLoadBtn.setLayoutData(accountLoadBtnGrDt);
        accountLoadBtn.setText("　リロード　");
        accountLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    for (Organization org : getValidOrganizations()) {
                        Api tokenApi = new ServerlessTokenApi(shell, ps, org);
                        tokenApi.setConnectTimeoutOverride(15000);
                        tokenApi.setSocketTimeoutOverride(15000);
                        ServerlessTokenJson tokenJson = (ServerlessTokenJson) tokenApi.get();
                        ps.setValue(PreferenceConstants.SERVERLESS_HOST, tokenJson.getHost());
                        ps.setValue(PreferenceConstants.SERVERLESS_TOKEN, tokenJson.getAccessToken());
                        Api accountsApi = new AccountsApi(shell, ps, tokenJson, org);
                        @SuppressWarnings("unchecked")
                        List<Account> accounts = (List<Account>) accountsApi.get();
                        serverlessAccounts.clear();
                        serverlessAccounts.addAll(accounts);
                        accountCombo.removeAll();
                        for (Account account : accounts) {
                            accountCombo.add(String.format("%s - %s", org.getName(), account.getName()));
                        }
                        accountCombo.select(0);
                        selectedAccountIndex = 0;
                    }
                } catch (Exception e) {
                    MessageDialog.openError(shell, "アカウント一覧の読み込み", e.getMessage());
                }
            }
        });

        serverlessResultLoadBtn = new Button(serverlessGrp, SWT.PUSH);
        GridData serverlessResultLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        serverlessResultLoadBtnGrDt.horizontalSpan = 3;
        serverlessResultLoadBtnGrDt.minimumHeight = 50;
        serverlessResultLoadBtnGrDt.heightHint = bigBtnSize.y + 20;
        serverlessResultLoadBtn.setLayoutData(serverlessResultLoadBtnGrDt);
        serverlessResultLoadBtn.setText("取得");
        serverlessResultLoadBtn.setToolTipText("サーバレス結果一覧を読み込みます。");
        serverlessResultLoadBtn.setFont(new Font(display, "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        serverlessResultLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (selectedAccountIndex < 0) {
                    MessageDialog.openInformation(shell, Messages.getString("main.attackevent.load.message.dialog.title"), "先にアカウントを読み込んでください。"); //$NON-NLS-1$
                    return;
                }
                resultTable.clearAll();
                resultTable.removeAll();
                Account targetAccount = serverlessAccounts.get(selectedAccountIndex);
                Organization targetOrg = null;
                for (Organization org : getValidOrganizations()) {
                    if (org.getOrganization_uuid().equals(targetAccount.getOrgId())) {
                        targetOrg = org;
                        break;
                    }
                }
                if (targetOrg == null) {
                    return;
                }
                ServerlessResultGetWithProgress progress = new ServerlessResultGetWithProgress(shell, ps, targetOrg, targetAccount);
                ServerlessResultGetProgressMonitorDialog progDialog = new ServerlessResultGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                    List<Function> functions = progress.getFunctions();
                    // Collections.reverse(attackEvents);
                    for (Function function : functions) {
                        addColToResultTable(function, -1);
                    }
                    // attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size())); //$NON-NLS-1$
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
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(shell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        resultTable = new Table(serverlessGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData resultTableGrDt = new GridData(GridData.FILL_BOTH);
        resultTableGrDt.horizontalSpan = 3;
        resultTable.setLayoutData(resultTableGrDt);
        resultTable.setLinesVisible(true);
        resultTable.setHeaderVisible(true);
        Menu menuResultTable = new Menu(resultTable);
        resultTable.setMenu(menuResultTable);

        TableColumn resultColumn0 = new TableColumn(resultTable, SWT.NONE);
        resultColumn0.setWidth(0);
        resultColumn0.setResizable(false);
        TableColumn resultColumn1 = new TableColumn(resultTable, SWT.LEFT);
        resultColumn1.setWidth(100);
        resultColumn1.setText("深刻度");
        TableColumn resultColumn2 = new TableColumn(resultTable, SWT.LEFT);
        resultColumn2.setWidth(100);
        resultColumn2.setText("脆弱性数");
        TableColumn resultColumn3 = new TableColumn(resultTable, SWT.LEFT);
        resultColumn3.setWidth(100);
        resultColumn3.setText("カテゴリ");
        TableColumn resultColumn4 = new TableColumn(resultTable, SWT.LEFT);
        resultColumn4.setWidth(360);
        resultColumn4.setText("関数");

        serverlessTabItem.setControl(serverlessShell);

        // #################### SERVER #################### //
        CTabItem serverTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        serverTabItem.setText(Messages.getString("main.tab.server.title")); //$NON-NLS-1$
        // serverTabItem.setImage(new Image(shell.getDisplay(), getClass().getClassLoader().getResourceAsStream("server16.png"))); //$NON-NLS-1$

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
        serverLoadBtnGrDt.minimumHeight = 50;
        serverLoadBtnGrDt.heightHint = bigBtnSize.y + 20;
        serverLoadBtn.setLayoutData(serverLoadBtnGrDt);
        serverLoadBtn.setText(Messages.getString("main.server.load.button.title")); //$NON-NLS-1$
        serverLoadBtn.setToolTipText(Messages.getString("main.server.load.button.tooltip")); //$NON-NLS-1$
        serverLoadBtn.setFont(new Font(display, "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        serverLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                serverTable.clearAll();
                serverTable.removeAll();
                ServerWithProgress progress = new ServerWithProgress(shell, ps, getValidOrganizations());
                ProgressMonitorDialog progDialog = new ServerGetProgressMonitorDialog(shell);
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
                        MessageDialog.openError(shell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(shell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(shell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(shell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(shell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
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
        miServerExp.setText(Messages.getString("main.server.menu.item.export.csv")); //$NON-NLS-1$
        miServerExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = getOutDirPath();
                }

                int[] selectIndexes = serverTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String filePath = timestamp + ".csv"; //$NON-NLS-1$
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                filePath = outDirPath + System.getProperty("file.separator") + filePath;
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_SERVER);
                List<ServerCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<ServerCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(shell, Messages.getString("main.server.message.dialog.json.load.error.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.server.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
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
                                // ==================== 01. サーバ名 ====================
                                csvLineList.add(server.getName());
                                break;
                            case SERVER_02:
                                // ==================== 02. パス ====================
                                csvLineList.add(server.getPath());
                                break;
                            case SERVER_03:
                                // ==================== 03. 言語 ====================
                                csvLineList.add(server.getLanguage());
                                break;
                            case SERVER_04:
                                // ==================== 04. エージェントバージョン ====================
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
                    MessageDialog.openInformation(shell, Messages.getString("main.server.message.dialog.export.csv.title"), //$NON-NLS-1$
                            Messages.getString("main.server.message.dialog.export.csv.message")); //$NON-NLS-1$
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
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && e.keyCode == 'a') {
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
        serverColumn2.setText(Messages.getString("main.server.table.column0.title")); //$NON-NLS-1$
        TableColumn serverColumn3 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn3.setWidth(360);
        serverColumn3.setText(Messages.getString("main.server.table.column1.title")); //$NON-NLS-1$
        TableColumn serverColumn4 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn4.setWidth(100);
        serverColumn4.setText(Messages.getString("main.server.table.column2.title")); //$NON-NLS-1$
        TableColumn serverColumn5 = new TableColumn(serverTable, SWT.LEFT);
        serverColumn5.setWidth(200);
        serverColumn5.setText(Messages.getString("main.server.table.column3.title")); //$NON-NLS-1$
        serverTabItem.setControl(serverShell);

        Button serverFilterBtn = new Button(serverListGrp, SWT.PUSH);
        GridData serverFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        serverFilterBtnGrDt.horizontalSpan = 3;
        serverFilterBtn.setLayoutData(serverFilterBtnGrDt);
        serverFilterBtn.setText(Messages.getString("main.server.filter.button.title")); //$NON-NLS-1$
        serverFilterBtn.setToolTipText(Messages.getString("main.server.filter.button.tooltip")); //$NON-NLS-1$
        serverFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (serverFilterMap == null) {
                    MessageDialog.openInformation(shell, Messages.getString("main.server.filter.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.server.filter.not.loaded.error.message")); //$NON-NLS-1$
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

        Composite bottomBtnGrp = new Composite(shell, SWT.NONE);
        GridLayout bottomBtnGrpLt = new GridLayout();
        if (this.authType == AuthType.PASSWORD) {
            bottomBtnGrpLt.numColumns = 2;
        } else {
            bottomBtnGrpLt.numColumns = 1;
        }
        bottomBtnGrpLt.makeColumnsEqualWidth = false;
        bottomBtnGrpLt.marginHeight = 0;
        bottomBtnGrp.setLayout(bottomBtnGrpLt);
        GridData bottomBtnGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        bottomBtnGrp.setLayoutData(bottomBtnGrpGrDt);

        // ========== 設定ボタン ==========
        settingBtn = new Button(bottomBtnGrp, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText(Messages.getString("main.settings.button.title")); //$NON-NLS-1$
        settingBtn.setToolTipText(Messages.getString("main.settings.button.tooltip")); //$NON-NLS-1$
        settingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage(shell, authType)); //$NON-NLS-1$
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage(authType)); //$NON-NLS-1$
                PreferenceNode otherNode = new PreferenceNode("other", new OtherPreferencePage()); //$NON-NLS-1$
                PreferenceNode csvNode = new PreferenceNode("csv", new CSVPreferencePage()); //$NON-NLS-1$
                PreferenceNode vulCsvColumnNode = new PreferenceNode("vulcsvcolumn", new VulCSVColumnPreferencePage()); //$NON-NLS-1$
                PreferenceNode libCsvColumnNode = new PreferenceNode("libcsvcolumn", new LibCSVColumnPreferencePage()); //$NON-NLS-1$
                PreferenceNode evtCsvColumnNode = new PreferenceNode("evtcsvcolumn", new AttackEventCSVColumnPreferencePage()); //$NON-NLS-1$
                PreferenceNode svrCsvColumnNode = new PreferenceNode("svrcsvcolumn", new ServerCSVColumnPreferencePage()); //$NON-NLS-1$
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
                mgr.addToRoot(csvNode);
                mgr.addTo(csvNode.getId(), vulCsvColumnNode);
                mgr.addTo(csvNode.getId(), libCsvColumnNode);
                mgr.addTo(csvNode.getId(), evtCsvColumnNode);
                mgr.addTo(csvNode.getId(), svrCsvColumnNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage()); //$NON-NLS-1$
                mgr.addToRoot(aboutNode);
                preferenceDialog = new MyPreferenceDialog(shell, mgr);
                preferenceDialog.setPreferenceStore(ps);
                preferenceDialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        // ========== ログアウトボタン ==========
        if (this.authType == AuthType.PASSWORD) {
            this.logOutBtn = new Button(bottomBtnGrp, SWT.PUSH);
            this.logOutBtn.setLayoutData(new GridData());
            this.logOutBtn.setText(Messages.getString("main.logout.button.title")); //$NON-NLS-1$
            this.logOutBtn.setToolTipText(Messages.getString("main.logout.button.tooltip")); //$NON-NLS-1$
            this.logOutBtn.setEnabled(false);
            this.logOutBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    logOut();
                }
            });
        }

        this.statusBar = new Label(shell, SWT.RIGHT);
        GridData statusBarGrDt = new GridData(GridData.FILL_HORIZONTAL);
        statusBarGrDt.minimumHeight = 11;
        this.statusBar.setLayoutData(statusBarGrDt);
        this.statusBar.setFont(new Font(display, "Arial", 9, SWT.NORMAL)); //$NON-NLS-1$
        this.statusBar.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

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

    private void attackDetectedTermUpdate() {
        FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(shell, frDetectedDate, toDetectedDate);
        int result = filterDialog.open();
        if (IDialogConstants.OK_ID != result) {
            attackLoadBtn.setFocus();
            return;
        }
        frDetectedDate = filterDialog.getFrDate();
        toDetectedDate = filterDialog.getToDate();
        attackDetectedTermTextSet(frDetectedDate, toDetectedDate);
        ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR, frDetectedDate != null ? frDetectedDate.getTime() : 0);
        ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO, toDetectedDate != null ? toDetectedDate.getTime() : 0);
    }

    private void attackDetectedTermTextSet(Date fr, Date to) {
        if (fr != null && to != null) {
            attackDetectedFilterTxt.setText(String.format("%s - %s", sdf.format(fr), sdf.format(to))); //$NON-NLS-1$
        } else if (frDetectedDate != null) {
            attackDetectedFilterTxt.setText(String.format("%s -", sdf.format(fr))); //$NON-NLS-1$
        } else if (toDetectedDate != null) {
            attackDetectedFilterTxt.setText(String.format("- %s", sdf.format(to))); //$NON-NLS-1$
        } else {
            attackDetectedFilterTxt.setText(""); //$NON-NLS-1$
        }
    }

    public void loggedIn() {
        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()); //$NON-NLS-1$
        String userName = ps.getString(PreferenceConstants.USERNAME);
        this.statusBar.setText(String.format("%s %s successfully logged in", userName, timestamp)); //$NON-NLS-1$
        this.logOutBtn.setEnabled(true);
    }

    public void logOut() {
        Api logoutApi = new LogoutApi(shell, ps, getValidOrganization());
        try {
            logoutApi.getWithoutCheckTsv();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loggedOut();
    }

    public void loggedOut() {
        this.cookieJar = null;
        this.statusBar.setText(""); //$NON-NLS-1$
        ps.setValue(PreferenceConstants.XSRF_TOKEN, ""); //$NON-NLS-1$
        ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, BasicAuthStatusEnum.NONE.name());
        ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
        logOutBtn.setEnabled(false);
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
        String tags = String.join(",", attackEvent.getTags()); //$NON-NLS-1$
        item.setText(10, tags);
        item.setText(11, attackEvent.getOrganization().getName());
    }

    private void addColToResultTable(Function function, int index) {
        if (function == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(resultTable, SWT.CENTER, index);
        } else {
            item = new TableItem(resultTable, SWT.CENTER);
        }
        item.setText(2, String.valueOf(function.getResults().size()));
        Set<String> categorySet = new HashSet<String>();
        for (Result result : function.getResults()) {
            categorySet.add(result.getCategoryText());
        }
        List<String> categories = new ArrayList<String>(categorySet);
        if (categories.size() > 1) {
            item.setText(3, String.valueOf(categories.size()));
        } else if (!categories.isEmpty()) {
            item.setText(3, categories.get(0));
        }
        item.setText(4, function.getFunctionName());
        // item.setText(2, result.getCategoryText());
        // item.setText(3, result.getTitle());
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
        srcListFilter.setText(""); //$NON-NLS-1$
        srcList.removeAll();
        srcApps.clear();
        // dst
        dstListFilter.setText(""); //$NON-NLS-1$
        dstList.removeAll();
        dstApps.clear();
        // full
        if (fullAppMap != null) {
            fullAppMap.clear();
        }
    }

    private void uiUpdate() {
    }

    private void srcListFilterUpdate() {
        srcList.removeAll(); // UI List src
        srcApps.clear(); // memory src
        if (fullAppMap == null) {
            srcCount.setText(String.valueOf(srcList.getItemCount()));
            return;
        }
        String keyword = srcListFilter.getText().trim();
        String language = srcListLanguagesFilter.getText().trim();
        if (keyword.isEmpty() && language.isEmpty()) {
            for (String appLabel : fullAppMap.keySet()) {
                if (dstApps.contains(appLabel)) {
                    continue; // 既に選択済みのアプリはスキップ
                }
                srcList.add(appLabel); // UI List src
                srcApps.add(appLabel); // memory src
            }
        } else {
            for (String appLabel : fullAppMap.keySet()) {
                boolean isKeywordValid = true;
                if (!keyword.isEmpty()) {
                    if (!appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                        if (dstApps.contains(appLabel)) {
                            continue; // 既に選択済みのアプリはスキップ
                        }
                        isKeywordValid = false;
                    }
                }
                boolean isLanguageValid = true;
                if (!language.isEmpty()) {
                    AppInfo appInfo = fullAppMap.get(appLabel);
                    if (!appInfo.getLanguageLabel().toLowerCase().contains(language)) {
                        isLanguageValid = false;
                    }
                }
                if (isKeywordValid && isLanguageValid) {
                    srcList.add(appLabel);
                    srcApps.add(appLabel);
                }
            }
        }
        srcCount.setText(String.valueOf(srcList.getItemCount()));
    }

    public PreferenceStore getPreferenceStore() {
        return ps;
    }

    public PreferenceDialog getPreferenceDialog() {
        return preferenceDialog;
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

    public String getValidOrganizationsOldStr() {
        return validOrganizationsOldStr;
    }

    public void setValidOrganizationsOldStr(String validOrganizationsOldStr) {
        this.validOrganizationsOldStr = validOrganizationsOldStr;
    }

    private void updateProtectOption() {
        this.attackEventDetectedFilterMap = getAttackEventDetectedDateMap();
        attackTermToday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))); //$NON-NLS-1$
        attackTermYesterday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))); //$NON-NLS-1$
        attackTerm30days.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
        attackTermLastWeek.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
        attackTermThisWeek.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
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
        map.put(AttackEventDetectedDateFilterEnum.THIS_WEEK_END, lastWeekStart.plusDays(13));
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
            text = String.join(", ", orgNameList); //$NON-NLS-1$
        }
        if (text == null || text.isEmpty()) {
            this.shell.setText(String.format(WINDOW_TITLE, Messages.getString("main.window.title.organization.undefined"))); //$NON-NLS-1$
        } else {
            this.shell.setText(String.format(WINDOW_TITLE, text));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("attackEventFilter".equals(event.getPropertyName())) { //$NON-NLS-1$
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
                    if (filter.getLabel().equals("")) { //$NON-NLS-1$
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

                // 時間帯フィルタ
                if (filterMap.containsKey(FilterEnum.BUSINESS_HOURS)) {
                    int target = Integer.parseInt(attackEvent.getTimeStrReceived());
                    String termDayTime = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                    String termNightTime = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                    for (Filter filter : filterMap.get(FilterEnum.BUSINESS_HOURS)) {
                        if (filter.isValid()) {
                            continue;
                        }
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.daytime"))) { //$NON-NLS-1$
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0]; //$NON-NLS-1$
                                String toDtStr = termDayTime.split("-")[1]; //$NON-NLS-1$
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
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.nighttime"))) { //$NON-NLS-1$
                            if (!termNightTime.isEmpty()) {
                                String frNtStr = termNightTime.split("-")[0]; //$NON-NLS-1$
                                String toNtStr = termNightTime.split("-")[1]; //$NON-NLS-1$
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
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.othertime"))) { //$NON-NLS-1$
                            boolean hitFlg = false;
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0]; //$NON-NLS-1$
                                String toDtStr = termDayTime.split("-")[1]; //$NON-NLS-1$
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
                                String frNtStr = termNightTime.split("-")[0]; //$NON-NLS-1$
                                String toNtStr = termNightTime.split("-")[1]; //$NON-NLS-1$
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
            attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size())); //$NON-NLS-1$
        } else if ("serverFilter".equals(event.getPropertyName())) { //$NON-NLS-1$
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
        } else if ("tsv".equals(event.getPropertyName())) { //$NON-NLS-1$
            System.out.println("tsv main"); //$NON-NLS-1$
        }

    }

    private String getOutDirPath() {
        DirectoryDialog dirDialog = new DirectoryDialog(shell);
        dirDialog.setFilterPath(System.getProperty("user.dir"));
        if (!ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save")) {
            dirDialog.setMessage("この出力先を記憶する場合は、出力設定で記憶するを選択してください。");
        }
        String outDirPath = dirDialog.open();
        if (outDirPath == null) {
            return null;
        }
        if (!Files.isWritable(Paths.get(outDirPath))) {
            MessageDialog.openError(shell, "出力先ディレクトリの指定", String.format("このディレクトリには書き込み権限がありません。他のディレクトリを指定してください。\r\n%s", outDirPath));
            return null;
        }
        if (ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save")) {
            ps.setValue(PreferenceConstants.FILE_OUT_DIR, outDirPath);
        }
        return outDirPath;
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
