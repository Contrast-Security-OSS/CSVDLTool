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

public class CSVPreferencePage extends PreferencePage {

    private Text vulCSVFileForamtTxt;
    private Text libCSVFileForamtTxt;
    private Text vulSleepTxt;
    private Text libSleepTxt;

    public CSVPreferencePage() {
        super("CSV出力設定");
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

        Group csvFileFormatGrp = new Group(composite, SWT.NONE);
        GridLayout csvGrpLt = new GridLayout(1, false);
        csvGrpLt.marginWidth = 10;
        csvGrpLt.marginHeight = 10;
        csvGrpLt.horizontalSpacing = 5;
        csvGrpLt.verticalSpacing = 10;
        csvFileFormatGrp.setLayout(csvGrpLt);
        GridData csvGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvGrpGrDt.horizontalSpan = 2;
        csvFileFormatGrp.setLayoutData(csvGrpGrDt);
        csvFileFormatGrp.setText("CSV出力ファイルフォーマット（フォルダ名にも適用されます）");

        Group vulCSVFileFormatGrp = new Group(csvFileFormatGrp, SWT.NONE);
        GridLayout vulCSVFileFormatGrpLt = new GridLayout(1, false);
        vulCSVFileFormatGrpLt.marginWidth = 10;
        vulCSVFileFormatGrpLt.marginHeight = 10;
        vulCSVFileFormatGrpLt.horizontalSpacing = 10;
        vulCSVFileFormatGrp.setLayout(vulCSVFileFormatGrpLt);
        GridData vulCSVFileFormatGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFormatGrpGrDt.horizontalSpan = 2;
        vulCSVFileFormatGrp.setLayoutData(vulCSVFileFormatGrpGrDt);
        vulCSVFileFormatGrp.setText("脆弱性");

        vulCSVFileForamtTxt = new Text(vulCSVFileFormatGrp, SWT.BORDER);
        vulCSVFileForamtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulCSVFileForamtTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
        vulCSVFileForamtTxt.setMessage(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
        Label vulCSVFileFormatHint = new Label(vulCSVFileFormatGrp, SWT.LEFT);
        GridData vulCSVFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        vulCSVFileFormatHint.setLayoutData(vulCSVFileFormatHintGrDt);
        vulCSVFileFormatHint.setText("※ java.text.SimpleDateFormatの書式としてください。\r\n例) 'vul_'yyyy-MM-dd_HHmmss");

        Group libCSVFileFormatGrp = new Group(csvFileFormatGrp, SWT.NONE);
        GridLayout libCSVFileFormatGrpLt = new GridLayout(1, false);
        libCSVFileFormatGrpLt.marginWidth = 10;
        libCSVFileFormatGrpLt.marginHeight = 10;
        libCSVFileFormatGrpLt.horizontalSpacing = 10;
        libCSVFileFormatGrp.setLayout(libCSVFileFormatGrpLt);
        GridData libCSVFileFormatGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFormatGrpGrDt.horizontalSpan = 2;
        libCSVFileFormatGrp.setLayoutData(libCSVFileFormatGrpGrDt);
        libCSVFileFormatGrp.setText("ライブラリ");

        libCSVFileForamtTxt = new Text(libCSVFileFormatGrp, SWT.BORDER);
        libCSVFileForamtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libCSVFileForamtTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
        libCSVFileForamtTxt.setMessage(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
        Label libCSVFileFormatHint = new Label(libCSVFileFormatGrp, SWT.LEFT);
        GridData libCSVFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        libCSVFileFormatHint.setLayoutData(libCSVFileFormatHintGrDt);
        libCSVFileFormatHint.setText("※ java.text.SimpleDateFormatの書式としてください。\r\n例) 'lib_'yyyy-MM-dd_HHmmss");

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(2, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("スリープ制御");

        // ========== 脆弱性取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("脆弱性取得間隔スリープ（ミリ秒）：");
        vulSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        vulSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSleepTxt.setText(preferenceStore.getString(PreferenceConstants.SLEEP_VUL));

        // ========== ライブラリ取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("ライブラリ取得間隔スリープ（ミリ秒）：");
        libSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        libSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libSleepTxt.setText(preferenceStore.getString(PreferenceConstants.SLEEP_LIB));

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
                vulCSVFileForamtTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
                libCSVFileForamtTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
                vulSleepTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.SLEEP_VUL));
                libSleepTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.SLEEP_LIB));
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
        if (ps == null) {
            return true;
        }
        List<String> errors = new ArrayList<String>();
        if (this.vulSleepTxt.getText().isEmpty()) {
            errors.add("・脆弱性取得間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.vulSleepTxt.getText())) {
                errors.add("・脆弱性取得間隔スリープは数値を指定してください。");
            }
        }
        if (this.libSleepTxt.getText().isEmpty()) {
            errors.add("・ライブラリ取得間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.libSleepTxt.getText())) {
                errors.add("・ライブラリ取得間隔スリープは数値を指定してください。");
            }
        }
        ps.setValue(PreferenceConstants.SLEEP_VUL, this.vulSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_LIB, this.libSleepTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_VUL, this.vulCSVFileForamtTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_LIB, this.libCSVFileForamtTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
