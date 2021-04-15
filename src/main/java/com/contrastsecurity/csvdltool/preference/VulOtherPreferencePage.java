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

public class VulOtherPreferencePage extends PreferencePage {

    private Text sleepTxt;
    private Text csvSepTagTxt;
    private Text csvSepBuildNoTxt;
    private Text csvSepGroupTxt;
    private Text csvSepServerTxt;
    private Text csvFileForamtTxt;

    public VulOtherPreferencePage() {
        super("その他設定");
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

        Group csvGrp = new Group(composite, SWT.NONE);
        GridLayout csvGrpLt = new GridLayout(1, false);
        csvGrpLt.marginWidth = 10;
        csvGrpLt.marginHeight = 10;
        csvGrpLt.horizontalSpacing = 5;
        csvGrpLt.verticalSpacing = 10;
        csvGrp.setLayout(csvGrpLt);
        GridData csvGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvGrpGrDt.horizontalSpan = 2;
        csvGrp.setLayoutData(csvGrpGrDt);
        csvGrp.setText("CSV出力");

        Group csvSepGrp = new Group(csvGrp, SWT.NONE);
        GridLayout csvSepGrpLt = new GridLayout(2, false);
        csvSepGrpLt.marginWidth = 10;
        csvSepGrpLt.marginHeight = 10;
        csvSepGrpLt.horizontalSpacing = 10;
        csvSepGrp.setLayout(csvSepGrpLt);
        GridData csvSepGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvSepGrpGrDt.horizontalSpan = 2;
        csvSepGrp.setLayoutData(csvSepGrpGrDt);
        csvSepGrp.setText("区切り文字");

        new Label(csvSepGrp, SWT.LEFT).setText("タグ：");
        csvSepTagTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepTagTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepTagTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_TAG));

        new Label(csvSepGrp, SWT.LEFT).setText("ビルド番号：");
        csvSepBuildNoTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepBuildNoTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepBuildNoTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_BUILDNO));

        new Label(csvSepGrp, SWT.LEFT).setText("アプリケーショングループ：");
        csvSepGroupTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepGroupTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepGroupTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_GROUP));

        new Label(csvSepGrp, SWT.LEFT).setText("サーバ：");
        csvSepServerTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepServerTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepServerTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_SERVER));

        Group csvFileFormatGrp = new Group(csvGrp, SWT.NONE);
        GridLayout csvFileFormatGrpLt = new GridLayout(1, false);
        csvFileFormatGrpLt.marginWidth = 10;
        csvFileFormatGrpLt.marginHeight = 10;
        csvFileFormatGrpLt.horizontalSpacing = 10;
        csvFileFormatGrp.setLayout(csvFileFormatGrpLt);
        GridData csvFileFormatGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFormatGrpGrDt.horizontalSpan = 2;
        csvFileFormatGrp.setLayoutData(csvFileFormatGrpGrDt);
        csvFileFormatGrp.setText("CSV出力ファイルフォーマット（またはフォルダ名）");

        csvFileForamtTxt = new Text(csvFileFormatGrp, SWT.BORDER);
        csvFileForamtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvFileForamtTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
        csvFileForamtTxt.setMessage(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
        Label csvFileFormatHint = new Label(csvFileFormatGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText("java.text.SimpleDateFormatの書式としてください。\r\n例) 'vul_'yyyy-MM-dd_HHmmss");

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(4, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("制御");

        // ========== 脆弱性取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("脆弱性取得間隔スリープ（ミリ秒）：");
        sleepTxt = new Text(ctrlGrp, SWT.BORDER);
        sleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sleepTxt.setText(preferenceStore.getString(PreferenceConstants.SLEEP_VUL));

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.widthHint = 90;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText("デフォルトに戻す");
        defaultBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                csvSepTagTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_SEPARATOR_TAG));
                csvSepBuildNoTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_SEPARATOR_BUILDNO));
                csvSepGroupTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_SEPARATOR_GROUP));
                csvSepServerTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_SEPARATOR_SERVER));
                csvFileForamtTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
                sleepTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.SLEEP_VUL));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
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
        if (this.sleepTxt != null) {
            if (this.sleepTxt.getText().isEmpty()) {
                errors.add("・脆弱性取得間隔スリープを指定してください。");
            } else {
                if (!StringUtils.isNumeric(this.sleepTxt.getText())) {
                    errors.add("・脆弱性取得間隔スリープは数値を指定してください。");
                }
            }
            ps.setValue(PreferenceConstants.SLEEP_VUL, this.sleepTxt.getText());
        }
        if (this.csvSepTagTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_TAG, this.csvSepTagTxt.getText());
        }
        if (this.csvSepBuildNoTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_BUILDNO, this.csvSepBuildNoTxt.getText());
        }
        if (this.csvSepGroupTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_GROUP, this.csvSepGroupTxt.getText());
        }
        if (this.csvSepServerTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_SERVER, this.csvSepServerTxt.getText());
        }
        if (this.csvFileForamtTxt != null) {
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_VUL, this.csvFileForamtTxt.getText());
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
