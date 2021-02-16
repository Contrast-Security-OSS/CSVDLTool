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

public class OtherPreferencePage extends PreferencePage {

    private Button outCsvHeaderFlg;
    private Text traceSleepTxt;
    private Text csvSepTagTxt;
    private Text csvSepBuildNoTxt;
    private Text csvSepServerTxt;
    private Text csvSepRouteTxt;
    private Text csvFileForamtTxt;

    public OtherPreferencePage() {
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

        outCsvHeaderFlg = new Button(csvGrp, SWT.CHECK);
        GridData outCsvHeaderFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        outCsvHeaderFlgGrDt.horizontalSpan = 2;
        outCsvHeaderFlg.setLayoutData(outCsvHeaderFlgGrDt);
        outCsvHeaderFlg.setText("カラムヘッダを出力");
        if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER)) {
            outCsvHeaderFlg.setSelection(true);
        }

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

        new Label(csvSepGrp, SWT.LEFT).setText("サーバ：");
        csvSepServerTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepServerTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepServerTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_SERVER));

        new Label(csvSepGrp, SWT.LEFT).setText("ルート：");
        csvSepRouteTxt = new Text(csvSepGrp, SWT.BORDER);
        csvSepRouteTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvSepRouteTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_ROUTE));

        Group csvFileFormatGrp = new Group(csvGrp, SWT.NONE);
        GridLayout csvFileFormatGrpLt = new GridLayout(1, false);
        csvFileFormatGrpLt.marginWidth = 10;
        csvFileFormatGrpLt.marginHeight = 10;
        csvFileFormatGrpLt.horizontalSpacing = 10;
        csvFileFormatGrp.setLayout(csvFileFormatGrpLt);
        GridData csvFileFormatGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFormatGrpGrDt.horizontalSpan = 2;
        csvFileFormatGrp.setLayoutData(csvFileFormatGrpGrDt);
        csvFileFormatGrp.setText("CSV出力ファイルフォーマット");

        csvFileForamtTxt = new Text(csvFileFormatGrp, SWT.BORDER);
        csvFileForamtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvFileForamtTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT));
        csvFileForamtTxt.setMessage(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT));
        Label csvFileFormatHint = new Label(csvFileFormatGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText("java.text.SimpleDateFormatの書式としてください。\r\n例) 'contrast_'yyyy-MM-dd_HHmmss");

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
        traceSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        traceSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        traceSleepTxt.setText(preferenceStore.getString(PreferenceConstants.SLEEP_TRACE));

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
        ps.setValue(PreferenceConstants.CSV_OUT_HEADER, this.outCsvHeaderFlg.getSelection());
        if (this.traceSleepTxt != null) {
            if (this.traceSleepTxt.getText().isEmpty()) {
                errors.add("・脆弱性取得間隔スリープを指定してください。");
            } else {
                if (!StringUtils.isNumeric(this.traceSleepTxt.getText())) {
                    errors.add("・脆弱性取得間隔スリープは数値を指定してください。");
                }
            }
            ps.setValue(PreferenceConstants.SLEEP_TRACE, this.traceSleepTxt.getText());
        }
        if (this.csvSepTagTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_TAG, this.csvSepTagTxt.getText());
        }
        if (this.csvSepBuildNoTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_BUILDNO, this.csvSepBuildNoTxt.getText());
        }
        if (this.csvSepServerTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_SERVER, this.csvSepServerTxt.getText());
        }
        if (this.csvSepRouteTxt != null) {
            ps.setValue(PreferenceConstants.CSV_SEPARATOR_ROUTE, this.csvSepRouteTxt.getText());
        }
        if (this.csvFileForamtTxt != null) {
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT, this.csvFileForamtTxt.getText());
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
