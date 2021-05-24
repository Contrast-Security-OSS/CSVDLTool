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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text serviceKeyTxt;
    private Text userNameTxt;
    private List<Organization> orgList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private int selectedIdx = -1;
    private Table table;

    Logger logger = Logger.getLogger("csvdltool");

    public BasePreferencePage() {
        super("基本設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore preferenceStore = getPreferenceStore();

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
        GridData baseGrpLtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        baseGrp.setLayoutData(baseGrpLtGrDt);

        new Label(baseGrp, SWT.LEFT).setText("Contrast URL：");
        new Label(baseGrp, SWT.LEFT).setText("");
        contrastUrlTxt = new Text(baseGrp, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(preferenceStore.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.setMessage("http://xxx.xxx.xxx.xxx/Contrast");
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Service Key：");
        new Label(baseGrp, SWT.LEFT).setText("");
        serviceKeyTxt = new Text(baseGrp, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));
        serviceKeyTxt.setMessage("個人のサービスキー");
        serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                serviceKeyTxt.selectAll();
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Username：");
        Label icon = new Label(baseGrp, SWT.NONE);
        Image iconImg = new Image(parent.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png"));
        icon.setImage(iconImg);
        icon.setToolTipText("設定するユーザーの権限について\r\n・組織ロールはView権限以上が必要です。\r\n・Admin権限を持つユーザーの場合、アプリケーショングループの情報も取得できます。\r\n・アプリケーションアクセスグループはView権限以上が必要です。");
        userNameTxt = new Text(baseGrp, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));
        userNameTxt.setMessage("メールアドレス");
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });

        // ========== 組織テーブル ========== //
        Group orgTableGrp = new Group(composite, SWT.NONE);
        GridLayout orgTableGrpLt = new GridLayout(2, false);
        orgTableGrpLt.marginWidth = 15;
        orgTableGrpLt.horizontalSpacing = 10;
        orgTableGrp.setLayout(orgTableGrpLt);
        GridData orgTableGrpGrDt = new GridData(GridData.FILL_BOTH);
        orgTableGrp.setLayoutData(orgTableGrpGrDt);
        orgTableGrp.setText("組織一覧");

        String orgJsonStr = preferenceStore.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
            }.getType());
        } else {
            orgList = new ArrayList<Organization>();
        }

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
        column1.setText("有効");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(150);
        column2.setText("組織名");
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(250);
        column3.setText("組織ID");
        TableColumn column4 = new TableColumn(table, SWT.CENTER);
        column4.setWidth(250);
        column4.setText("API Key");

        for (Organization org : orgList) {
            this.addOrgToTable(org);
        }
        for (Button button : checkBoxList) {
            if (button.getSelection()) {
                this.selectedIdx = checkBoxList.indexOf(button);
            }
        }

        Composite buttonGrp = new Composite(orgTableGrp, SWT.NONE);
        buttonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttonGrp.setLayout(new GridLayout(1, true));

        final Button addBtn = new Button(buttonGrp, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addBtn.setText("追加");
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                OrganizationDialog pathDialog = new OrganizationDialog(getShell(), preferenceStore, contrastUrlTxt.getText(), userNameTxt.getText(), serviceKeyTxt.getText());
                int result = pathDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                Organization rtnOrg = pathDialog.getOrg();
                // String path = pathDialog.getDirPath();
                if (orgList.contains(rtnOrg)) {
                    MessageDialog.openError(composite.getShell(), "組織", "すでに設定されている組織です。");
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
                        selectedIdx = checkBoxList.indexOf(button);
                    }
                }
            }
        });

        final Button rmvBtn = new Button(buttonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText("削除");
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
                table.removeAll();
                for (Organization org : orgList) {
                    addOrgToTable(org);
                }
                if (checkBoxList.isEmpty()) {
                    selectedIdx = -1;
                } else {
                    for (Button button : checkBoxList) {
                        if (button.getSelection()) {
                            selectedIdx = checkBoxList.indexOf(button);
                        }
                    }
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
        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtnGrDt.horizontalSpan = 2;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
        applyBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });
        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        String url = contrastUrlTxt.getText();
        String usr = userNameTxt.getText();
        String svc = serviceKeyTxt.getText();
        if (url.isEmpty() || usr.isEmpty() || svc.isEmpty()) {
            MessageDialog.openError(getShell(), "基本設定", "4つの項目を埋めて、組織情報を取得してください。");
            return false;
        }
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.contrastUrlTxt != null) {
            ps.setValue(PreferenceConstants.CONTRAST_URL, this.contrastUrlTxt.getText());
        }
        if (this.serviceKeyTxt != null) {
            ps.setValue(PreferenceConstants.SERVICE_KEY, this.serviceKeyTxt.getText());
        }
        if (this.userNameTxt != null) {
            ps.setValue(PreferenceConstants.USERNAME, this.userNameTxt.getText());
        }
        if (selectedIdx > -1) {
            TableItem selectedItem = table.getItem(selectedIdx);
            for (Organization org : this.orgList) {
                if (org.getOrganization_uuid().equals(selectedItem.getText(3))) {
                    org.setValid(true);
                } else {
                    org.setValid(false);
                }
            }
        }
        ps.setValue(PreferenceConstants.TARGET_ORGS, new Gson().toJson(this.orgList));
        return true;
    }

    private void addOrgToTable(Organization org) {
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        if (org.isValid()) {
            button.setSelection(true);
        }
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button triggerBtn = (Button) e.getSource();
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
                triggerBtn.setSelection(true);
                selectedIdx = checkBoxList.indexOf(triggerBtn);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
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
