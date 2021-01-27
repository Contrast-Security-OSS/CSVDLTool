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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ProxyPreferencePage extends PreferencePage {

    private Text hostTxt;
    private Text portTxt;
    private Text workDirTxt;
    private Text logDirTxt;
    private Text iniFileDirTxt;
    private List<Text> textList;

    public ProxyPreferencePage() {
        super("プロキシ設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(4, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 15;
        compositeLt.horizontalSpacing = 10;
        composite.setLayout(compositeLt);
        IPreferenceStore preferenceStore = getPreferenceStore();

        // ========== ホスト ========== //
        new Label(composite, SWT.LEFT).setText("ホスト：");
        hostTxt = new Text(composite, SWT.BORDER);
        hostTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hostTxt.setText(preferenceStore.getString(PreferenceConstants.TTPMACRO_EXE));

        // ========== ポート ========== //
        new Label(composite, SWT.LEFT).setText("ポート：");
        portTxt = new Text(composite, SWT.BORDER);
        portTxt.setLayoutData(new GridData());
        portTxt.setText(preferenceStore.getString(PreferenceConstants.TTPMACRO_EXE));

        Group dirGrp = new Group(composite, SWT.NONE);
        GridLayout dirGrpLt = new GridLayout(2, false);
        dirGrpLt.marginWidth = 15;
        dirGrpLt.horizontalSpacing = 10;
        dirGrp.setLayout(dirGrpLt);
        GridData dirGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dirGrpGrDt.horizontalSpan = 4;
        dirGrp.setLayoutData(dirGrpGrDt);
        dirGrp.setText("認証");

        this.textList = new ArrayList<Text>();

        // ========== ユーザー ========== //
        new Label(dirGrp, SWT.LEFT).setText("ユーザー：");
        workDirTxt = new Text(dirGrp, SWT.BORDER);
        workDirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        workDirTxt.setText(preferenceStore.getString(PreferenceConstants.WORK_DIR));
        this.textList.add(workDirTxt);

        // ========== パスワード ========== //
        new Label(dirGrp, SWT.LEFT).setText("パスワード：");
        logDirTxt = new Text(dirGrp, SWT.BORDER);
        logDirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        logDirTxt.setText(preferenceStore.getString(PreferenceConstants.LOG_DIR));
        this.textList.add(logDirTxt);

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.hostTxt != null) {
            ps.setValue(PreferenceConstants.TTPMACRO_EXE, this.hostTxt.getText());
        }
        if (this.workDirTxt != null) {
            ps.setValue(PreferenceConstants.WORK_DIR, this.workDirTxt.getText());
        }
        if (this.logDirTxt != null) {
            ps.setValue(PreferenceConstants.LOG_DIR, this.logDirTxt.getText());
        }
        if (this.iniFileDirTxt != null) {
            ps.setValue(PreferenceConstants.INIFILE_DIR, this.iniFileDirTxt.getText());
        }
        return true;
    }
}
