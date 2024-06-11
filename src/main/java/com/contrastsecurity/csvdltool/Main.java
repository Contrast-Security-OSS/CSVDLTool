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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.exec.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.LogoutApi;
import com.contrastsecurity.csvdltool.model.ContrastSecurityYaml;
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
import com.contrastsecurity.csvdltool.ui.AssessTabItem;
import com.contrastsecurity.csvdltool.ui.ProtectTabItem;
import com.contrastsecurity.csvdltool.ui.ScanTabItem;
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

    private CTabFolder mainTabFolder;

    private Button settingBtn;
    private Button logOutBtn;

    private Label statusBar;

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

            this.ps.setDefault(PreferenceConstants.INCLUDE_ARCHIVED_PROJ, true);
            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_SCANRESULT, ScanResultCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_SCANRESULT, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_SCANRESULT, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_SCANRESULT, "'scanresult'_yyyy-MM-dd_HHmmss"); //$NON-NLS-1$

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
                ps.setValue(PreferenceConstants.OPENED_MAIN_TAB_IDX, main_idx);
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
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
                    support.firePropertyChange("buttonDisabled", null, false); //$NON-NLS-1$
                    support.firePropertyChange("validOrgChanged", null, null); //$NON-NLS-1$
                    settingBtn.setText(Messages.getString("main.settings.initial.button.title")); //$NON-NLS-1$
                } else {
                    String validOrganizationsNewStr = new Gson().toJson(orgs);
                    if (!validOrganizationsNewStr.equals(getValidOrganizationsOldStr())) {
                        ps.setValue(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
                        support.firePropertyChange("validOrgChanged", null, null); //$NON-NLS-1$
                    }
                    support.firePropertyChange("buttonEnabled", null, true); //$NON-NLS-1$
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

        // #################### ASSESS #################### //
        addPropertyChangeListener(new AssessTabItem(mainTabFolder, shell, ps));

        // #################### PROTECT #################### //
        addPropertyChangeListener(new ProtectTabItem(mainTabFolder, shell, ps));

        // #################### SERVERLESS #################### //
        addPropertyChangeListener(new ServerlessTabItem(mainTabFolder, shell, ps));

        // #################### SERVER #################### //
        addPropertyChangeListener(new ServerTabItem(mainTabFolder, shell, ps));

        // #################### SCAN #################### //
        addPropertyChangeListener(new ScanTabItem(mainTabFolder, shell, ps));

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

    private void uiUpdate() {
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
