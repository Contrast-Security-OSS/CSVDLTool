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

package com.contrastsecurity.csvdltool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ProxyPreferencePage extends PreferencePage {

    private Button validFlg;
    private Text hostTxt;
    private Text portTxt;
    private Text userTxt;
    private Text passTxt;
    private List<Text> textList;

    public ProxyPreferencePage() {
        super("プロキシ設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        composite.setLayout(compositeLt);

        IPreferenceStore preferenceStore = getPreferenceStore();

        validFlg = new Button(composite, SWT.CHECK);
        // GridData validFlgGrDt = new GridData();
        // validFlgGrDt.horizontalSpan = 4;
        // validFlg.setLayoutData(validFlgGrDt);
        validFlg.setText("プロキシ経由");
        if (preferenceStore.getBoolean(PreferenceConstants.PROXY_YUKO)) {
            validFlg.setSelection(true);
        }

        Group proxyGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(4, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        proxyGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        proxyGrp.setLayoutData(proxyGrpGrDt);

        // ========== ホスト ========== //
        new Label(proxyGrp, SWT.LEFT).setText("ホスト：");
        hostTxt = new Text(proxyGrp, SWT.BORDER);
        hostTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hostTxt.setText(preferenceStore.getString(PreferenceConstants.PROXY_HOST));

        // ========== ポート ========== //
        new Label(proxyGrp, SWT.LEFT).setText("ポート：");
        portTxt = new Text(proxyGrp, SWT.BORDER);
        portTxt.setLayoutData(new GridData());
        portTxt.setText(preferenceStore.getString(PreferenceConstants.PROXY_PORT));

        Group dirGrp = new Group(composite, SWT.NONE);
        GridLayout dirGrpLt = new GridLayout(2, false);
        dirGrpLt.marginWidth = 15;
        dirGrpLt.horizontalSpacing = 10;
        dirGrp.setLayout(dirGrpLt);
        GridData dirGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // dirGrpGrDt.horizontalSpan = 4;
        dirGrp.setLayoutData(dirGrpGrDt);
        dirGrp.setText("認証");

        this.textList = new ArrayList<Text>();

        // ========== ユーザー ========== //
        new Label(dirGrp, SWT.LEFT).setText("ユーザー：");
        userTxt = new Text(dirGrp, SWT.BORDER);
        userTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userTxt.setText(preferenceStore.getString(PreferenceConstants.PROXY_USER));
        this.textList.add(userTxt);

        // ========== パスワード ========== //
        new Label(dirGrp, SWT.LEFT).setText("パスワード：");
        passTxt = new Text(dirGrp, SWT.BORDER);
        passTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passTxt.setEchoChar('*');
        passTxt.setText(preferenceStore.getString(PreferenceConstants.PROXY_PASS));
        this.textList.add(passTxt);

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
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
        IPreferenceStore ps = getPreferenceStore();
        List<String> errors = new ArrayList<String>();
        if (ps == null) {
            return true;
        }
        ps.setValue(PreferenceConstants.PROXY_YUKO, this.validFlg.getSelection());
        if (this.hostTxt != null) {
            if (this.validFlg.getSelection()) {
                if (this.hostTxt.getText().isEmpty()) {
                    errors.add("・ホストを指定してください。");
                }
            }
            ps.setValue(PreferenceConstants.PROXY_HOST, this.hostTxt.getText());
        }
        if (this.portTxt != null) {
            if (this.validFlg.getSelection()) {
                if (this.portTxt.getText().isEmpty()) {
                    errors.add("・ポート番号を指定してください。");
                } else {
                    if (!StringUtils.isNumeric(this.portTxt.getText())) {
                        errors.add("・ポート番号は数値を指定してください。");
                    }
                }
            }
            ps.setValue(PreferenceConstants.PROXY_PORT, this.portTxt.getText());
        }
        if (this.userTxt != null) {
            ps.setValue(PreferenceConstants.PROXY_USER, this.userTxt.getText());
        }
        if (this.passTxt != null) {
            ps.setValue(PreferenceConstants.PROXY_PASS, this.passTxt.getText());
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "プロキシ設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
