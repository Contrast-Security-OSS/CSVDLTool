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

package com.contrastsecurity.csvdltool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.Main.AuthType;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.ProxyAuthDialog;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text userNameTxt;
    private Text serviceKeyTxt;
    private Button passInput;
    private Button passSave;
    private Text passTxt;
    private List<Organization> orgList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Integer> selectedIdxes = new ArrayList<Integer>();
    private Table table;
    private Button addBtn;
    private Button bulkOnBtn;
    private Button bulkOffBtn;
    private CSVDLToolShell shell;
    private AuthType authType;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public BasePreferencePage(CSVDLToolShell shell, AuthType authType) {
        super(Messages.getString("basepreferencepage.title")); //$NON-NLS-1$
        this.shell = shell;
        this.authType = authType;
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Composite baseGrp = new Composite(composite, SWT.NONE);
        GridLayout baseGrpLt = new GridLayout(3, false);
        baseGrpLt.marginWidth = 15;
        baseGrpLt.horizontalSpacing = 10;
        baseGrp.setLayout(baseGrpLt);
        GridData baseGrpLtGrDt = new GridData(GridData.FILL_BOTH);
        baseGrp.setLayoutData(baseGrpLtGrDt);

        new Label(baseGrp, SWT.LEFT).setText(Messages.getString("basepreferencepage.contrast.url.label")); //$NON-NLS-1$
        new Label(baseGrp, SWT.LEFT).setText(""); //$NON-NLS-1$
        contrastUrlTxt = new Text(baseGrp, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(ps.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.setMessage(Messages.getString("basepreferencepage.contrast.url.text.message")); //$NON-NLS-1$
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });
        contrastUrlTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String contrastUrlStr = contrastUrlTxt.getText().trim();
                String userNameStr = userNameTxt.getText().trim();
                if (authType == AuthType.PASSWORD) {
                    if (contrastUrlStr.isEmpty() || userNameStr.isEmpty()) {
                        addBtn.setEnabled(false);
                    } else {
                        addBtn.setEnabled(true);
                    }
                } else {
                    String serviceKeyStr = serviceKeyTxt.getText().trim();
                    if (contrastUrlStr.isEmpty() || userNameStr.isEmpty() || serviceKeyStr.isEmpty()) {
                        addBtn.setEnabled(false);
                    } else {
                        addBtn.setEnabled(true);
                    }
                }
            }
        });

        new Label(baseGrp, SWT.LEFT).setText(Messages.getString("basepreferencepage.username.label")); //$NON-NLS-1$
        Label icon = new Label(baseGrp, SWT.NONE);
        Image iconImg = new Image(parent.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png")); //$NON-NLS-1$
        icon.setImage(iconImg);
        icon.setToolTipText(Messages.getString("basepreferencepage.role.hint.info.message")); //$NON-NLS-1$
        userNameTxt = new Text(baseGrp, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(ps.getString(PreferenceConstants.USERNAME));
        userNameTxt.setMessage(Messages.getString("basepreferencepage.username.text.message")); //$NON-NLS-1$
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });
        userNameTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String contrastUrlStr = contrastUrlTxt.getText().trim();
                String userNameStr = userNameTxt.getText().trim();
                if (authType == AuthType.PASSWORD) {
                    if (contrastUrlStr.isEmpty() || userNameStr.isEmpty()) {
                        addBtn.setEnabled(false);
                    } else {
                        addBtn.setEnabled(true);
                    }
                } else {
                    String serviceKeyStr = serviceKeyTxt.getText().trim();
                    if (contrastUrlStr.isEmpty() || userNameStr.isEmpty() || serviceKeyStr.isEmpty()) {
                        addBtn.setEnabled(false);
                    } else {
                        addBtn.setEnabled(true);
                    }
                }
            }
        });

        if (this.authType == AuthType.PASSWORD) {
            Label passwordLbl = new Label(baseGrp, SWT.LEFT);
            GridData passwordLblGrDt = new GridData();
            passwordLblGrDt.verticalAlignment = SWT.TOP;
            passwordLbl.setLayoutData(passwordLblGrDt);
            passwordLbl.setText(Messages.getString("basepreferencepage.password.label")); //$NON-NLS-1$
            new Label(baseGrp, SWT.LEFT).setText(""); //$NON-NLS-1$
            Group passwordGrp = new Group(baseGrp, SWT.NONE);
            GridLayout passwordGrpLt = new GridLayout(2, false);
            passwordGrpLt.marginWidth = 15;
            passwordGrpLt.horizontalSpacing = 10;
            passwordGrp.setLayout(passwordGrpLt);
            GridData passwordGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
            passwordGrp.setLayoutData(passwordGrpGrDt);
            // ========== Save or Input ========== //
            Composite passInputTypeGrp = new Composite(passwordGrp, SWT.NONE);
            GridLayout passInputTypeGrpLt = new GridLayout(1, false);
            passInputTypeGrpLt.marginWidth = 0;
            passInputTypeGrpLt.marginBottom = -5;
            passInputTypeGrpLt.verticalSpacing = 10;
            passInputTypeGrp.setLayout(passInputTypeGrpLt);
            GridData passInputTypeGrpGrDt = new GridData();
            passInputTypeGrpGrDt.horizontalSpan = 2;
            passInputTypeGrp.setLayoutData(passInputTypeGrpGrDt);

            passInput = new Button(passInputTypeGrp, SWT.RADIO);
            passInput.setText(Messages.getString("basepreferencepage.password.auth.group.auth.with.auth.input")); //$NON-NLS-1$
            passInput.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button source = (Button) e.getSource();
                    if (source.getSelection()) {
                        passTxt.setText(""); //$NON-NLS-1$
                        passTxt.setEnabled(false);
                    }
                }

            });

            passSave = new Button(passInputTypeGrp, SWT.RADIO);
            passSave.setText(Messages.getString("basepreferencepage.password.auth.group.auth.with.auth.save")); //$NON-NLS-1$
            passSave.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button source = (Button) e.getSource();
                    if (source.getSelection()) {
                        passTxt.setEnabled(true);
                    }
                }

            });

            Composite passTxtGrp = new Composite(passwordGrp, SWT.NONE);
            GridLayout passTxtGrpLt = new GridLayout(2, false);
            passTxtGrpLt.marginTop = -5;
            passTxtGrpLt.marginLeft = 12;
            passTxtGrp.setLayout(passTxtGrpLt);
            GridData passTxtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
            passTxtGrp.setLayoutData(passTxtGrpGrDt);
            // ========== パスワード ========== //
            passTxt = new Text(passTxtGrp, SWT.BORDER);
            passTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            passTxt.setEchoChar('*');
            passTxt.addListener(SWT.FocusIn, new Listener() {
                public void handleEvent(Event e) {
                    passTxt.selectAll();
                }
            });

            if (ps.getString(PreferenceConstants.PASS_TYPE).equals("input")) { //$NON-NLS-1$
                passInput.setSelection(true);
                passTxt.setText(""); //$NON-NLS-1$
                passTxt.setEnabled(false);
            } else if (ps.getString(PreferenceConstants.PASS_TYPE).equals("save")) { //$NON-NLS-1$
                passSave.setSelection(true);
                BasicTextEncryptor encryptor = new BasicTextEncryptor();
                encryptor.setPassword(Main.MASTER_PASSWORD);
                try {
                    passTxt.setText(encryptor.decrypt(ps.getString(PreferenceConstants.PASSWORD)));
                } catch (Exception e) {
                    MessageDialog.openError(getShell(), Messages.getString("basepreferencepage.dialog.title"), Messages.getString("basepreferencepage.password.decrypt.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
                    passTxt.setText(""); //$NON-NLS-1$
                }
            }
        } else {
            new Label(baseGrp, SWT.LEFT).setText(Messages.getString("basepreferencepage.servicekey.label")); //$NON-NLS-1$
            new Label(baseGrp, SWT.LEFT).setText(""); //$NON-NLS-1$
            serviceKeyTxt = new Text(baseGrp, SWT.BORDER);
            serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            serviceKeyTxt.setText(ps.getString(PreferenceConstants.SERVICE_KEY));
            serviceKeyTxt.setMessage(Messages.getString("basepreferencepage.servicekey.text.message")); //$NON-NLS-1$
            serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
                public void handleEvent(Event e) {
                    serviceKeyTxt.selectAll();
                }
            });
            serviceKeyTxt.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    String contrastUrlStr = contrastUrlTxt.getText().trim();
                    String serviceKeyStr = serviceKeyTxt.getText().trim();
                    String userNameStr = userNameTxt.getText().trim();
                    if (contrastUrlStr.isEmpty() || serviceKeyStr.isEmpty() || userNameStr.isEmpty()) {
                        addBtn.setEnabled(false);
                    } else {
                        addBtn.setEnabled(true);
                    }
                }
            });
        }

        // ========== 組織テーブル ========== //
        Group orgTableGrp = new Group(baseGrp, SWT.NONE);
        GridLayout orgTableGrpLt = new GridLayout(2, false);
        orgTableGrpLt.marginWidth = 10;
        orgTableGrpLt.horizontalSpacing = 10;
        orgTableGrp.setLayout(orgTableGrpLt);
        GridData orgTableGrpGrDt = new GridData(GridData.FILL_BOTH);
        orgTableGrpGrDt.horizontalSpan = 3;
        orgTableGrp.setLayoutData(orgTableGrpGrDt);
        orgTableGrp.setText(Messages.getString("basepreferencepage.organizations.table.title")); //$NON-NLS-1$

        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(getShell(), Messages.getString("basepreferencepage.dialog.title"), String.format("%s\r\n%s", Messages.getString("basepreferencepage.message.dialog.organization.json.load.error.message"), orgJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                orgList = new ArrayList<Organization>();
            }
        } else {
            orgList = new ArrayList<Organization>();
        }
        // Clean up ここから
        List<Integer> irregularIndexes = new ArrayList<Integer>();
        for (int i = 0; i < orgList.size(); i++) {
            Object obj = orgList.get(i);
            if (!(obj instanceof Organization)) {
                irregularIndexes.add(i);
            }
        }
        int[] irregularArray = irregularIndexes.stream().mapToInt(i -> i).toArray();
        for (int i = irregularArray.length - 1; i >= 0; i--) {
            orgList.remove(i);
        }
        // Clean up ここまで

        table = new Table(orgTableGrp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        // tableGrDt.horizontalSpan = 2;
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.CENTER);
        column1.setWidth(50);
        column1.setText(Messages.getString("basepreferencepage.org.table.column0.title")); //$NON-NLS-1$
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(150);
        column2.setText(Messages.getString("basepreferencepage.org.table.column1.title")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(250);
        column3.setText(Messages.getString("basepreferencepage.org.table.column2.title")); //$NON-NLS-1$
        TableColumn column4 = new TableColumn(table, SWT.CENTER);
        column4.setWidth(250);
        column4.setText(Messages.getString("basepreferencepage.org.table.column3.title")); //$NON-NLS-1$

        for (Organization org : orgList) {
            this.addOrgToTable(org);
        }
        for (Button button : checkBoxList) {
            if (button.getSelection()) {
                this.selectedIdxes.add(checkBoxList.indexOf(button));
            }
        }

        Composite buttonGrp = new Composite(orgTableGrp, SWT.NONE);
        buttonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttonGrp.setLayout(new GridLayout(1, true));

        addBtn = new Button(buttonGrp, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addBtn.setText(Messages.getString("basepreferencepage.org.add.button.title")); //$NON-NLS-1$
        if (this.authType == AuthType.PASSWORD) {
            if (contrastUrlTxt.getText().trim().isEmpty() || userNameTxt.getText().trim().isEmpty()) {
                addBtn.setEnabled(false);
            } else {
                addBtn.setEnabled(true);
            }
        } else {
            if (contrastUrlTxt.getText().trim().isEmpty() || userNameTxt.getText().trim().isEmpty() || serviceKeyTxt.getText().trim().isEmpty()) {
                addBtn.setEnabled(false);
            } else {
                addBtn.setEnabled(true);
            }
        }
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
                    String usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(getShell());
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
                OrganizationDialog orgDialog = null;
                if (authType == AuthType.PASSWORD) {
                    orgDialog = new OrganizationDialog(shell, ps, contrastUrlTxt.getText().trim(), userNameTxt.getText().trim());
                } else {
                    orgDialog = new OrganizationDialog(shell, ps, contrastUrlTxt.getText().trim(), userNameTxt.getText().trim(), serviceKeyTxt.getText().trim());
                }
                int result = orgDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                Organization rtnOrg = orgDialog.getOrg();
                if (rtnOrg == null) {
                    return;
                }
                // String path = pathDialog.getDirPath();
                if (orgList.contains(rtnOrg)) {
                    MessageDialog.openError(composite.getShell(), Messages.getString("basepreferencepage.add.organization.dialog"), //$NON-NLS-1$
                            Messages.getString("basepreferencepage.add.organization.dialog.already.exist.error.message")); //$NON-NLS-1$
                    return;
                }
                orgList.add(rtnOrg);
                addOrgToTable(rtnOrg);
                if (orgList.size() == 1) {
                    checkBoxList.get(0).setSelection(true);
                    rtnOrg.setValid(true);
                }
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });

        final Button rmvBtn = new Button(buttonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText(Messages.getString("basepreferencepage.org.rmv.button.title")); //$NON-NLS-1$
        rmvBtn.setEnabled(false);
        rmvBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] indexes = table.getSelectionIndices();
                for (int i = indexes.length - 1; i >= 0; i--) {
                    orgList.remove(indexes[i]);
                }
                for (Button button : checkBoxList) {
                    button.dispose();
                }
                checkBoxList.clear();
                table.clearAll();
                table.removeAll();
                for (Organization org : orgList) {
                    addOrgToTable(org);
                }
                if (checkBoxList.isEmpty()) {
                    selectedIdxes.clear();
                } else {
                    selectedIdxes.clear();
                    for (Button button : checkBoxList) {
                        if (button.getSelection()) {
                            selectedIdxes.add(checkBoxList.indexOf(button));
                        }
                    }
                }
            }
        });

        bulkOnBtn = new Button(buttonGrp, SWT.NULL);
        bulkOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkOnBtn.setText(Messages.getString("basepreferencepage.org.all.on.button.title")); //$NON-NLS-1$
        bulkOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Organization org : orgList) {
                    org.setValid(true);
                }
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    button.setSelection(true);
                    selectedIdxes.add(checkBoxList.indexOf(button));
                }
            }
        });

        bulkOffBtn = new Button(buttonGrp, SWT.NULL);
        bulkOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkOffBtn.setText(Messages.getString("basepreferencepage.org.all.off.button.title")); //$NON-NLS-1$
        bulkOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Organization org : orgList) {
                    org.setValid(false);
                }
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
            }
        });

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index < 0) {
                    rmvBtn.setEnabled(false);
                } else {
                    rmvBtn.setEnabled(true);
                }
            }
        });

        Link connectionHint = new Link(orgTableGrp, SWT.NONE);
        connectionHint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        connectionHint.setText(Messages.getString("basepreferencepage.use.proxy.desc")); //$NON-NLS-1$
        connectionHint.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                PreferenceDialog preferenceDialog = shell.getMain().getPreferenceDialog();
                PreferenceManager pm = preferenceDialog.getPreferenceManager();
                IPreferenceNode node = pm.find("connection"); //$NON-NLS-1$
                TreeViewer viewer = preferenceDialog.getTreeViewer();
                viewer.setSelection(new StructuredSelection(node));
            }
        });

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtnGrDt.horizontalSpan = 2;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText(Messages.getString("basepreferencepage.apply.button.title")); //$NON-NLS-1$
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
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
        List<String> errors = new ArrayList<String>();
        String url = this.contrastUrlTxt.getText().trim();
        String usr = this.userNameTxt.getText().trim();
        if (this.authType == AuthType.PASSWORD) {
            if (url.isEmpty() || usr.isEmpty()) {
                if (!this.orgList.isEmpty()) {
                    errors.add(Messages.getString("basepreferencepage.message.dialog.org.exist.password.required.field.empty.error.message")); //$NON-NLS-1$
                    contrastUrlTxt.setText(ps.getString(PreferenceConstants.CONTRAST_URL));
                    userNameTxt.setText(ps.getString(PreferenceConstants.USERNAME));
                }
            }
        } else {
            String svc = this.serviceKeyTxt.getText().trim();
            if (url.isEmpty() || usr.isEmpty() || svc.isEmpty()) {
                if (!this.orgList.isEmpty()) {
                    errors.add(Messages.getString("basepreferencepage.message.dialog.org.exist.token.required.field.empty.error.message")); //$NON-NLS-1$
                    contrastUrlTxt.setText(ps.getString(PreferenceConstants.CONTRAST_URL));
                    serviceKeyTxt.setText(ps.getString(PreferenceConstants.SERVICE_KEY));
                    userNameTxt.setText(ps.getString(PreferenceConstants.USERNAME));
                }
            }
        }
        ps.setValue(PreferenceConstants.CONTRAST_URL, url);
        ps.setValue(PreferenceConstants.USERNAME, usr);
        for (Organization org : this.orgList) {
            org.setValid(false);
            for (Integer selectedIdx : selectedIdxes) {
                TableItem selectedItem = table.getItem(selectedIdx);
                if (org.getOrganization_uuid().equals(selectedItem.getText(3))) {
                    org.setValid(true);
                }
            }
        }
        if (this.authType == AuthType.PASSWORD) {
            if (passInput.getSelection()) {
                ps.setValue(PreferenceConstants.PASS_TYPE, "input"); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PASSWORD, ""); //$NON-NLS-1$
            } else if (passSave.getSelection()) {
                ps.setValue(PreferenceConstants.PASS_TYPE, "save"); //$NON-NLS-1$
                if (this.passTxt.getText().isEmpty()) {
                    errors.add(Messages.getString("basepreferencepage.message.dialog.password.empty.error.message")); //$NON-NLS-1$
                } else {
                    BasicTextEncryptor encryptor = new BasicTextEncryptor();
                    encryptor.setPassword(Main.MASTER_PASSWORD);
                    ps.setValue(PreferenceConstants.PASSWORD, encryptor.encrypt(this.passTxt.getText()));
                }
            }
        } else {
            String svc = this.serviceKeyTxt.getText().trim();
            ps.setValue(PreferenceConstants.SERVICE_KEY, svc);
        }

        ps.setValue(PreferenceConstants.TARGET_ORGS, new Gson().toJson(this.orgList));
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("basepreferencepage.title"), String.join("\r\n", errors)); //$NON-NLS-1$//$NON-NLS-2$
            return false;
        }
        return true;
    }

    private void addOrgToTable(Organization org) {
        if (org == null) {
            return;
        }
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        if (org.isValid()) {
            button.setSelection(true);
        }
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });
        button.pack();
        TableItem item = new TableItem(table, SWT.CENTER);
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.CENTER;
        editor.setEditor(button, item, 1);
        checkBoxList.add(button);
        item.setText(2, org.getName());
        item.setText(3, org.getOrganization_uuid());
        item.setText(4, org.getApikey());
    }
}
