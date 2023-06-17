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

import com.contrastsecurity.csvdltool.Messages;

public class CSVPreferencePage extends PreferencePage {

    private Text vulCSVFileFmtTxt;
    private Text libCSVFileFmtTxt;
    private Text evtCSVFileFmtTxt;
    private Text svrCSVFileFmtTxt;

    public CSVPreferencePage() {
        super(Messages.getString("csvpreferencepage.title")); //$NON-NLS-1$
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
        csvFileFmtGrp.setText(Messages.getString("csvpreferencepage.csv.file.format.group.title")); //$NON-NLS-1$

        Group vulCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout vulCSVFileFmtGrpLt = new GridLayout(1, false);
        vulCSVFileFmtGrpLt.marginWidth = 10;
        vulCSVFileFmtGrpLt.marginHeight = 10;
        vulCSVFileFmtGrpLt.horizontalSpacing = 10;
        vulCSVFileFmtGrp.setLayout(vulCSVFileFmtGrpLt);
        GridData vulCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // vulCSVFileFmtGrpGrDt.horizontalSpan = 2;
        vulCSVFileFmtGrp.setLayoutData(vulCSVFileFmtGrpGrDt);
        vulCSVFileFmtGrp.setText(Messages.getString("csvpreferencepage.vulnerability.group.title")); //$NON-NLS-1$

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
        libCSVFileFmtGrp.setText(Messages.getString("csvpreferencepage.libary.group.title")); //$NON-NLS-1$

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
        evtCSVFileFmtGrp.setText(Messages.getString("csvpreferencepage.attackevent.group.title")); //$NON-NLS-1$

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
        svrCSVFileFmtGrp.setText(Messages.getString("csvpreferencepage.server.group.title")); //$NON-NLS-1$

        svrCSVFileFmtTxt = new Text(svrCSVFileFmtGrp, SWT.BORDER);
        svrCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        svrCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));
        svrCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));

        Label csvFileFormatHint = new Label(csvFileFmtGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText(Messages.getString("csvpreferencepage.csv.file.format.desc")); //$NON-NLS-1$

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
        defaultBtnGrDt.minimumWidth = 100;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText(Messages.getString("preferencepage.restoredefaults.button.title")); //$NON-NLS-1$
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                vulCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL));
                libCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
                evtCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT));
                svrCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.minimumWidth = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText(Messages.getString("preferencepage.apply.button.title")); //$NON-NLS-1$
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
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("csvpreferencepage.dialog.title"), String.join("\r\n", errors)); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        } else {
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_VUL, this.vulCSVFileFmtTxt.getText());
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_LIB, this.libCSVFileFmtTxt.getText());
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT, this.evtCSVFileFmtTxt.getText());
            ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_SERVER, this.svrCSVFileFmtTxt.getText());
        }
        return true;
    }
}
