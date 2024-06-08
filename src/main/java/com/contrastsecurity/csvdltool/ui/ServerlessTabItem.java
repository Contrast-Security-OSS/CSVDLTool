package com.contrastsecurity.csvdltool.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.ServerlessResultGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.ServerlessResultGetWithProgress;
import com.contrastsecurity.csvdltool.api.AccountsApi;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ServerlessTokenApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.json.ServerlessTokenJson;
import com.contrastsecurity.csvdltool.model.Account;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.serverless.Function;
import com.contrastsecurity.csvdltool.model.serverless.Result;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class ServerlessTabItem extends CTabItem implements PropertyChangeListener {

    private List<Account> serverlessAccounts = new ArrayList<Account>();
    private int selectedAccountIndex;
    private Combo accountCombo;
    private Button loadBtn;
    private Table table;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ServerlessTabItem(CTabFolder mainTabFolder, CSVDLToolShell toolShell, PreferenceStore ps, Point bigBtnSize) {
        super(mainTabFolder, SWT.NONE);
        setText("SERVERLESS(β版)");
        setImage(new Image(toolShell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-serverless-02.png"))); //$NON-NLS-1$

        Composite shell = new Composite(mainTabFolder, SWT.NONE);
        shell.setLayout(new GridLayout(1, false));

        Group serverlessGrp = new Group(shell, SWT.NONE);
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
                    for (Organization org : toolShell.getMain().getValidOrganizations()) {
                        Api tokenApi = new ServerlessTokenApi(toolShell, ps, org);
                        tokenApi.setConnectTimeoutOverride(15000);
                        tokenApi.setSocketTimeoutOverride(15000);
                        ServerlessTokenJson tokenJson = (ServerlessTokenJson) tokenApi.get();
                        ps.setValue(PreferenceConstants.SERVERLESS_HOST, tokenJson.getHost());
                        ps.setValue(PreferenceConstants.SERVERLESS_TOKEN, tokenJson.getAccessToken());
                        Api accountsApi = new AccountsApi(toolShell, ps, tokenJson, org);
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
                    MessageDialog.openError(toolShell, "アカウント一覧の読み込み", e.getMessage());
                }
            }
        });

        loadBtn = new Button(serverlessGrp, SWT.PUSH);
        GridData loadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        loadBtnGrDt.horizontalSpan = 3;
        loadBtnGrDt.minimumHeight = 50;
        loadBtnGrDt.heightHint = bigBtnSize.y + 20;
        loadBtn.setLayoutData(loadBtnGrDt);
        loadBtn.setText("取得");
        loadBtn.setToolTipText("サーバレス結果一覧を読み込みます。");
        loadBtn.setFont(new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        loadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (selectedAccountIndex < 0) {
                    MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), "先にアカウントを読み込んでください。"); //$NON-NLS-1$
                    return;
                }
                table.clearAll();
                table.removeAll();
                Account targetAccount = serverlessAccounts.get(selectedAccountIndex);
                Organization targetOrg = null;
                for (Organization org : toolShell.getMain().getValidOrganizations()) {
                    if (org.getOrganization_uuid().equals(targetAccount.getOrgId())) {
                        targetOrg = org;
                        break;
                    }
                }
                if (targetOrg == null) {
                    return;
                }
                ServerlessResultGetWithProgress progress = new ServerlessResultGetWithProgress(toolShell, ps, targetOrg, targetAccount);
                ServerlessResultGetProgressMonitorDialog progDialog = new ServerlessResultGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                    List<Function> functions = progress.getFunctions();
                    // Collections.reverse(attackEvents);
                    for (Function function : functions) {
                        addColToTable(function, -1);
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
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        table = new Table(serverlessGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        Menu tableMenu = new Menu(table);
        table.setMenu(tableMenu);

        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setWidth(100);
        column1.setText("深刻度");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(100);
        column2.setText("脆弱性数");
        TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setWidth(100);
        column3.setText("カテゴリ");
        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(360);
        column4.setText("関数");

        setControl(shell);
    }

    private void addColToTable(Function function, int index) {
        if (function == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(table, SWT.CENTER, index);
        } else {
            item = new TableItem(table, SWT.CENTER);
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
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("shellActivated".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("shellClosed".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("tabSelected".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("buttonEnabled".equals(event.getPropertyName())) { //$NON-NLS-1$
        }
    }

}
