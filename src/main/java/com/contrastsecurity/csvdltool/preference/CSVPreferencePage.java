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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CSVPreferencePage extends PreferencePage {

    private Text vulCSVFileFmtTxt;
    private Text libCSVFileFmtTxt;
    private Text evtCSVFileFmtTxt;
    private Text svrCSVFileFmtTxt;
    private Text vulSleepTxt;
    private Text libSleepTxt;

    public CSVPreferencePage() {
        super("CSV????????????");
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

        Group csvFileFmtGrp = new Group(composite, SWT.NONE);
        GridLayout csvFileFmtGrpLt = new GridLayout(1, false);
        csvFileFmtGrpLt.marginWidth = 10;
        csvFileFmtGrpLt.marginHeight = 10;
        csvFileFmtGrpLt.horizontalSpacing = 5;
        csvFileFmtGrpLt.verticalSpacing = 10;
        csvFileFmtGrp.setLayout(csvFileFmtGrpLt);
        GridData csvFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFmtGrpGrDt.horizontalSpan = 2;
        csvFileFmtGrp.setLayoutData(csvFileFmtGrpGrDt);
        csvFileFmtGrp.setText("CSV?????????????????????????????????????????????????????????????????????????????????");

        Group vulCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout vulCSVFileFmtGrpLt = new GridLayout(1, false);
        vulCSVFileFmtGrpLt.marginWidth = 10;
        vulCSVFileFmtGrpLt.marginHeight = 10;
        vulCSVFileFmtGrpLt.horizontalSpacing = 10;
        vulCSVFileFmtGrp.setLayout(vulCSVFileFmtGrpLt);
        GridData vulCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // vulCSVFileFmtGrpGrDt.horizontalSpan = 2;
        vulCSVFileFmtGrp.setLayoutData(vulCSVFileFmtGrpGrDt);
        vulCSVFileFmtGrp.setText("?????????");

        vulCSVFileFmtTxt = new Text(vulCSVFileFmtGrp, SWT.BORDER);
        vulCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
        vulCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));

        Group libCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout libCSVFileFmtGrpLt = new GridLayout(1, false);
        libCSVFileFmtGrpLt.marginWidth = 10;
        libCSVFileFmtGrpLt.marginHeight = 10;
        libCSVFileFmtGrpLt.horizontalSpacing = 10;
        libCSVFileFmtGrp.setLayout(libCSVFileFmtGrpLt);
        GridData libCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // libCSVFileFmtGrpGrDt.horizontalSpan = 2;
        libCSVFileFmtGrp.setLayoutData(libCSVFileFmtGrpGrDt);
        libCSVFileFmtGrp.setText("???????????????");

        libCSVFileFmtTxt = new Text(libCSVFileFmtGrp, SWT.BORDER);
        libCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
        libCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));

        Group evtCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout evtCSVFileFmtGrpLt = new GridLayout(1, false);
        evtCSVFileFmtGrpLt.marginWidth = 10;
        evtCSVFileFmtGrpLt.marginHeight = 10;
        evtCSVFileFmtGrpLt.horizontalSpacing = 10;
        evtCSVFileFmtGrp.setLayout(evtCSVFileFmtGrpLt);
        GridData evtCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // evtCSVFileFmtGrpGrDt.horizontalSpan = 2;
        evtCSVFileFmtGrp.setLayoutData(evtCSVFileFmtGrpGrDt);
        evtCSVFileFmtGrp.setText("??????????????????");

        evtCSVFileFmtTxt = new Text(evtCSVFileFmtGrp, SWT.BORDER);
        evtCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        evtCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT));
        evtCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT));

        Group svrCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout svrCSVFileFmtGrpLt = new GridLayout(1, false);
        svrCSVFileFmtGrpLt.marginWidth = 10;
        svrCSVFileFmtGrpLt.marginHeight = 10;
        svrCSVFileFmtGrpLt.horizontalSpacing = 10;
        svrCSVFileFmtGrp.setLayout(svrCSVFileFmtGrpLt);
        GridData svrCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // svrCSVFileFmtGrpGrDt.horizontalSpan = 2;
        svrCSVFileFmtGrp.setLayoutData(svrCSVFileFmtGrpGrDt);
        svrCSVFileFmtGrp.setText("?????????");

        svrCSVFileFmtTxt = new Text(svrCSVFileFmtGrp, SWT.BORDER);
        svrCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        svrCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));
        svrCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));

        Label csvFileFormatHint = new Label(csvFileFmtGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText("??? java.text.SimpleDateFormat?????????????????????????????????\r\n???) 'vul_'yyyy-MM-dd_HHmmss???'lib_'yyyy-MM-dd_HHmmss");

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(2, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("??????????????????");

        // ========== ????????????????????????????????? ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("???????????????????????????????????????????????????");
        vulSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        vulSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_VUL));

        // ========== ??????????????????????????????????????? ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("?????????????????????????????????????????????????????????");
        libSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        libSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_LIB));

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
        defaultBtn.setText("????????????????????????");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                vulCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
                libCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
                evtCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT));
                svrCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));
                vulSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_VUL));
                libSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_LIB));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("??????");
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
        if (this.vulSleepTxt.getText().isEmpty()) {
            errors.add("??????????????????????????????????????????????????????????????????");
        } else {
            if (!StringUtils.isNumeric(this.vulSleepTxt.getText())) {
                errors.add("???????????????????????????????????????????????????????????????????????????");
            }
        }
        if (this.libSleepTxt.getText().isEmpty()) {
            errors.add("????????????????????????????????????????????????????????????????????????");
        } else {
            if (!StringUtils.isNumeric(this.libSleepTxt.getText())) {
                errors.add("?????????????????????????????????????????????????????????????????????????????????");
            }
        }
        ps.setValue(PreferenceConstants.SLEEP_VUL, this.vulSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_LIB, this.libSleepTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_VUL, this.vulCSVFileFmtTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_LIB, this.libCSVFileFmtTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT, this.evtCSVFileFmtTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_SERVER, this.svrCSVFileFmtTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "???????????????", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
