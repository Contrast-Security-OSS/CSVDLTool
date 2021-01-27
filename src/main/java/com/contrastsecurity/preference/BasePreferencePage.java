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

package com.contrastsecurity.preference;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text apiKeyTxt;
    private Text serviceKeyTxt;
    private Text userNameTxt;
    private Text orgIdTxt;

    public BasePreferencePage() {
        super("基本設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        new Label(composite, SWT.LEFT).setText("Contrast URL：");
        contrastUrlTxt = new Text(composite, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(preferenceStore.getString(PreferenceConstants.CONTRAST_URL));

        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(preferenceStore.getString(PreferenceConstants.API_KEY));

        new Label(composite, SWT.LEFT).setText("Service Key：");
        serviceKeyTxt = new Text(composite, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(preferenceStore.getString(PreferenceConstants.SERVICE_KEY));

        new Label(composite, SWT.LEFT).setText("Username：");
        userNameTxt = new Text(composite, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(preferenceStore.getString(PreferenceConstants.USERNAME));

        Button mkDirBtn = new Button(composite, SWT.NULL);
        GridData mkDirBtnGrDt = new GridData();
        mkDirBtnGrDt.horizontalSpan = 2;
        mkDirBtnGrDt.horizontalAlignment = SWT.RIGHT;
        mkDirBtn.setLayoutData(mkDirBtnGrDt);
        mkDirBtn.setText("組織IDを取得");
        mkDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // if (mkNum > 0) {
                // MessageDialog.openInformation(composite.getShell(),
                // "ディレクトリ作成", "ディレクトリを作成しました。");
                // } else {
                // MessageDialog.openWarning(composite.getShell(), "ディレクトリ作成",
                // "ディレクトリ作成対象が設定されていません。");
                // }
            }
        });

        new Label(composite, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(composite, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.setText(preferenceStore.getString(PreferenceConstants.ORG_ID));
        orgIdTxt.setEditable(false);

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.contrastUrlTxt != null) {
            ps.setValue(PreferenceConstants.CONTRAST_URL, this.contrastUrlTxt.getText());
        }
        if (this.apiKeyTxt != null) {
            ps.setValue(PreferenceConstants.API_KEY, this.apiKeyTxt.getText());
        }
        if (this.serviceKeyTxt != null) {
            ps.setValue(PreferenceConstants.SERVICE_KEY, this.serviceKeyTxt.getText());
        }
        if (this.userNameTxt != null) {
            ps.setValue(PreferenceConstants.USERNAME, this.userNameTxt.getText());
        }
        return true;
    }
}
