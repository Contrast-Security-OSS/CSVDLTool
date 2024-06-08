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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.LogoutApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
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
import com.contrastsecurity.csvdltool.ui.ProtectTabItem;
import com.contrastsecurity.csvdltool.ui.ServerTabItem;
import com.contrastsecurity.csvdltool.ui.ServerlessTabItem;
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
    private Button withCVSSInfoChk;
    private Button withEPSSInfoChk;
    private Button includeCVEDetailChk;

    private Button settingBtn;
    private Button logOutBtn;

    private Label statusBar;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)"); //$NON-NLS-1$
    private Text vulSeverityFilterTxt;
    private Text vulVulnTypeFilterTxt;
    private Text vulLastDetectedFilterTxt;

    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> assessFilterMap;
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;

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
                ps.setValue(PreferenceConstants.WITH_CVSS, withCVSSInfoChk.getSelection());
                ps.setValue(PreferenceConstants.WITH_EPSS, withEPSSInfoChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
                ps.setValue(PreferenceConstants.BASIC_AUTH_STATUS, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.XSRF_TOKEN, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.TSV_STATUS, ""); //$NON-NLS-1$
                support.firePropertyChange("shellClosed", null, null); //$NON-NLS-1$
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
                    support.firePropertyChange("buttonEnabled", null, false); //$NON-NLS-1$
                    appLoadBtn.setEnabled(false);
                    vulExecuteBtn.setEnabled(false);
                    settingBtn.setText(Messages.getString("main.settings.initial.button.title")); //$NON-NLS-1$
                    uiReset();
                } else {
                    String validOrganizationsNewStr = new Gson().toJson(orgs);
                    if (!validOrganizationsNewStr.equals(getValidOrganizationsOldStr())) {
                        ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
                        uiReset();
                    }
                    support.firePropertyChange("buttonEnabled", null, true); //$NON-NLS-1$
                    appLoadBtn.setEnabled(true);
                    vulExecuteBtn.setEnabled(true);
                    settingBtn.setText(Messages.getString("main.settings.button.title")); //$NON-NLS-1$
                }
                support.firePropertyChange("shellActivated", null, null); //$NON-NLS-1$
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
                support.firePropertyChange("tabSelected", null, mainTabFolder.getSelection()); //$NON-NLS-1$
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
                LibGetWithProgress progress = new LibGetWithProgress(shell, ps, outDirPath, dstApps, fullAppMap, onlyHasCVEChk.getSelection(), withCVSSInfoChk.getSelection(),
                        withEPSSInfoChk.getSelection(), includeCVEDetailChk.getSelection());
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
        withCVSSInfoChk = new Button(libButtonGrp, SWT.CHECK);
        withCVSSInfoChk.setText(Messages.getString("main.lib.export.option.with.cvss")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.WITH_CVSS)) {
            withCVSSInfoChk.setSelection(true);
        }
        withEPSSInfoChk = new Button(libButtonGrp, SWT.CHECK);
        withEPSSInfoChk.setText(Messages.getString("main.lib.export.option.with.epss")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.WITH_EPSS)) {
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
        addPropertyChangeListener(new ProtectTabItem(mainTabFolder, shell, ps, bigBtnSize));

        // #################### SERVERLESS #################### //
        addPropertyChangeListener(new ServerlessTabItem(mainTabFolder, shell, ps, bigBtnSize));

        // #################### SERVER #################### //
        addPropertyChangeListener(new ServerTabItem(mainTabFolder, shell, ps, bigBtnSize));

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
        if ("tsv".equals(event.getPropertyName())) { //$NON-NLS-1$
            System.out.println("tsv main"); //$NON-NLS-1$
        }
    }

    public String getOutDirPath() {
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
