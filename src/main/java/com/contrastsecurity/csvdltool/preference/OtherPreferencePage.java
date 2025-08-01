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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.Messages;

public class OtherPreferencePage extends PreferencePage {

    private Text dayTimeTxt;
    private Text nightTimeTxt;
    Pattern ptn = Pattern.compile("^[0-9]{4}-[0-9]{4}$"); //$NON-NLS-1$
    private static String[] WEEKDAYS = { Messages.getString("otherpreferencepage.first.day.of.week.group.sunday"), //$NON-NLS-1$
            Messages.getString("otherpreferencepage.first.day.of.week.group.monday"), Messages.getString("otherpreferencepage.first.day.of.week.group.tuesday"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("otherpreferencepage.first.day.of.week.group.wednesday"), Messages.getString("otherpreferencepage.first.day.of.week.group.thursday"), //$NON-NLS-1$ //$NON-NLS-2$
            Messages.getString("otherpreferencepage.first.day.of.week.group.friday"), Messages.getString("otherpreferencepage.first.day.of.week.group.saturday") }; //$NON-NLS-1$ //$NON-NLS-2$
    private List<Button> weekDayBtns = new ArrayList<Button>();
    private Button customInterceptorBtn;
    private Button tryCatchBtn;
    private Text maxRetriesTxt;
    private Text retryIntervalTxt;
    private Text vulSleepTxt;
    private Text libSleepTxt;
    private Text routeCoverageSleepTxt;
    private Text sbomSleepTxt;
    private Text scanResultSleepTxt;

    public OtherPreferencePage() {
        super(Messages.getString("otherpreferencepage.title")); //$NON-NLS-1$
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

        Group protectGrp = new Group(composite, SWT.NONE);
        GridLayout protectGrpLt = new GridLayout(2, false);
        protectGrpLt.marginWidth = 15;
        protectGrpLt.horizontalSpacing = 10;
        protectGrp.setLayout(protectGrpLt);
        GridData protectGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        protectGrp.setLayoutData(protectGrpGrDt);
        protectGrp.setText(Messages.getString("otherpreferencepage.protect.group.title")); //$NON-NLS-1$

        // ========== 日中時間帯 ========== //
        new Label(protectGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.daytime.label")); //$NON-NLS-1$
        dayTimeTxt = new Text(protectGrp, SWT.BORDER);
        dayTimeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dayTimeTxt.setText(ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME));
        dayTimeTxt.setMessage("0900-1800"); //$NON-NLS-1$
        dayTimeTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                dayTimeTxt.selectAll();
            }
        });
        // ========== 夜間時間帯 ========== //
        new Label(protectGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.nighttime.label")); //$NON-NLS-1$
        nightTimeTxt = new Text(protectGrp, SWT.BORDER);
        nightTimeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nightTimeTxt.setText(ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME));
        nightTimeTxt.setMessage("1800-0100"); //$NON-NLS-1$
        nightTimeTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                nightTimeTxt.selectAll();
            }
        });

        Label descLabel = new Label(protectGrp, SWT.LEFT);
        List<String> descLabelList = new ArrayList<String>();
        descLabelList.add(Messages.getString("otherpreferencepage.time.slot.desc.format.label")); //$NON-NLS-1$
        descLabelList.add(Messages.getString("otherpreferencepage.time.slot.desc.hint")); //$NON-NLS-1$
        descLabel.setText(String.join("\r\n", descLabelList)); //$NON-NLS-1$
        GridData descLabelGrDt = new GridData(GridData.FILL_HORIZONTAL);
        descLabelGrDt.horizontalSpan = 3;
        descLabel.setLayoutData(descLabelGrDt);

        Group weekDayGrp = new Group(protectGrp, SWT.NONE);
        GridLayout weekDayGrpLt = new GridLayout(7, false);
        weekDayGrpLt.horizontalSpacing = 10;
        weekDayGrp.setLayout(weekDayGrpLt);
        GridData weekDayGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        weekDayGrpGrDt.horizontalSpan = 2;
        weekDayGrp.setLayoutData(weekDayGrpGrDt);
        weekDayGrp.setText(Messages.getString("otherpreferencepage.first.day.of.week.group.title")); //$NON-NLS-1$
        int weekDayIdx = 0;
        for (String weekDay : WEEKDAYS) {
            Button weekDayBtn = new Button(weekDayGrp, SWT.RADIO);
            weekDayBtn.setText(weekDay);
            if (ps.getInt(PreferenceConstants.ATTACK_START_WEEKDAY) == weekDayIdx) {
                weekDayBtn.setSelection(true);
            } else {
                weekDayBtn.setSelection(false);
            }
            weekDayBtns.add(weekDayBtn);
            weekDayIdx++;
        }

        Group retryGrp = new Group(composite, SWT.NONE);
        GridLayout retryGrpLt = new GridLayout(2, false);
        retryGrpLt.marginWidth = 15;
        retryGrpLt.horizontalSpacing = 10;
        retryGrp.setLayout(retryGrpLt);
        GridData retryGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // retryGrpGrDt.horizontalSpan = 4;
        retryGrp.setLayoutData(retryGrpGrDt);
        retryGrp.setText(Messages.getString("otherpreferencepage.retry.group.title")); //$NON-NLS-1$

        Group retryMethodGrp = new Group(retryGrp, SWT.NONE);
        GridLayout retryMethodGrpLt = new GridLayout(7, false);
        retryMethodGrpLt.horizontalSpacing = 10;
        retryMethodGrp.setLayout(retryMethodGrpLt);
        GridData retryMethodGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        retryMethodGrpGrDt.horizontalSpan = 2;
        retryMethodGrp.setLayoutData(retryMethodGrpGrDt);
        retryMethodGrp.setText(Messages.getString("otherpreferencepage.retry.method")); //$NON-NLS-1$
        customInterceptorBtn = new Button(retryMethodGrp, SWT.RADIO);
        customInterceptorBtn.setText(Messages.getString("otherpreferencepage.retry.method.interceptor")); //$NON-NLS-1$
        tryCatchBtn = new Button(retryMethodGrp, SWT.RADIO);
        tryCatchBtn.setText(Messages.getString("otherpreferencepage.retry.method.trycatch")); //$NON-NLS-1$
        if (ps.getString(PreferenceConstants.RETRY_METHOD).equals("interceptor")) { //$NON-NLS-1$
            customInterceptorBtn.setSelection(true);
            tryCatchBtn.setSelection(false);
        } else {
            customInterceptorBtn.setSelection(false);
            tryCatchBtn.setSelection(true);
        }

        new Label(retryGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.retry.maxretries")); //$NON-NLS-1$
        maxRetriesTxt = new Text(retryGrp, SWT.BORDER);
        maxRetriesTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        maxRetriesTxt.setText(ps.getString(PreferenceConstants.MAX_RETRIES));
        maxRetriesTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                maxRetriesTxt.selectAll();
            }
        });

        new Label(retryGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.retry.retryinterval")); //$NON-NLS-1$
        retryIntervalTxt = new Text(retryGrp, SWT.BORDER);
        retryIntervalTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        retryIntervalTxt.setText(ps.getString(PreferenceConstants.RETRY_INTERVAL));
        retryIntervalTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                retryIntervalTxt.selectAll();
            }
        });

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(2, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText(Messages.getString("otherpreferencepage.interval.group.title")); //$NON-NLS-1$

        // ========== 脆弱性取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.interval.vulnerability.label")); //$NON-NLS-1$
        vulSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        vulSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_VUL));
        vulSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                vulSleepTxt.selectAll();
            }
        });

        // ========== ライブラリ取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.interval.library.label")); //$NON-NLS-1$
        libSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        libSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_LIB));
        libSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                libSleepTxt.selectAll();
            }
        });

        // ========== ルートカバレッジごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.interval.routecoverage.label")); //$NON-NLS-1$
        routeCoverageSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        routeCoverageSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        routeCoverageSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_ROUTECOVERAGE));
        routeCoverageSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                routeCoverageSleepTxt.selectAll();
            }
        });

        // ========== SBOMごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.interval.sbom.label")); //$NON-NLS-1$
        sbomSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        sbomSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sbomSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_SBOM));
        sbomSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                sbomSleepTxt.selectAll();
            }
        });

        // ========== Scanごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText(Messages.getString("otherpreferencepage.interval.scanresult.label")); //$NON-NLS-1$
        scanResultSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        scanResultSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scanResultSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_SCANRESULT));
        scanResultSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                scanResultSleepTxt.selectAll();
            }
        });

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
            public void widgetSelected(SelectionEvent e) {
                for (Button btn : weekDayBtns) {
                    btn.setSelection(false);
                }
                Button btn = weekDayBtns.get(ps.getDefaultInt(PreferenceConstants.ATTACK_START_WEEKDAY));
                btn.setSelection(true);
                customInterceptorBtn.setSelection(true);
                tryCatchBtn.setSelection(false);
                maxRetriesTxt.setText(ps.getDefaultString(PreferenceConstants.MAX_RETRIES));
                retryIntervalTxt.setText(ps.getDefaultString(PreferenceConstants.RETRY_INTERVAL));
                vulSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_VUL));
                libSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_LIB));
                routeCoverageSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_ROUTECOVERAGE));
                sbomSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_SBOM));
                scanResultSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_SCANRESULT));
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

        if (!this.dayTimeTxt.getText().isEmpty()) {
            Matcher m = ptn.matcher(this.dayTimeTxt.getText());
            if (!m.matches()) {
                errors.add(Messages.getString("otherpreferencepage.daytime.format.error.message")); //$NON-NLS-1$
            }
        }

        if (!this.nightTimeTxt.getText().isEmpty()) {
            Matcher m = ptn.matcher(this.nightTimeTxt.getText());
            if (!m.matches()) {
                errors.add(Messages.getString("otherpreferencepage.nighttime.format.error.message")); //$NON-NLS-1$
            }
        }
        if (this.maxRetriesTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.retry.maxretries.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.maxRetriesTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.retry.maxretries.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.retryIntervalTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.retry.retryinterval.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.retryIntervalTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.retry.retryinterval.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.vulSleepTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.interval.vulnerability.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.vulSleepTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.interval.vulnerability.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.libSleepTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.interval.library.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.libSleepTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.interval.library.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.routeCoverageSleepTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.interval.routecoverage.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.routeCoverageSleepTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.interval.routecoverage.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.sbomSleepTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.interval.sbom.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.sbomSleepTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.interval.sbom.nondigit.error.message")); //$NON-NLS-1$
            }
        }
        if (this.scanResultSleepTxt.getText().isEmpty()) {
            errors.add(Messages.getString("otherpreferencepage.interval.scanresult.empty.error.message")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.scanResultSleepTxt.getText())) {
                errors.add(Messages.getString("otherpreferencepage.interval.scanresult.nondigit.error.message")); //$NON-NLS-1$
            }
        }

        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("otherpreferencepage.title"), String.join("\r\n", errors)); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        } else {
            ps.setValue(PreferenceConstants.ATTACK_RANGE_DAYTIME, this.dayTimeTxt.getText());
            ps.setValue(PreferenceConstants.ATTACK_RANGE_NIGHTTIME, this.nightTimeTxt.getText());
            int weekDaySelection = 0;
            for (Button btn : weekDayBtns) {
                if (btn.getSelection()) {
                    break;
                }
                weekDaySelection++;
            }
            ps.setValue(PreferenceConstants.ATTACK_START_WEEKDAY, weekDaySelection);
            ps.setValue(PreferenceConstants.MAX_RETRIES, this.maxRetriesTxt.getText());
            if (customInterceptorBtn.getSelection()) {
                ps.setValue(PreferenceConstants.RETRY_METHOD, "interceptor"); //$NON-NLS-1$
            } else {
                ps.setValue(PreferenceConstants.RETRY_METHOD, "trycatch"); //$NON-NLS-1$
            }
            ps.setValue(PreferenceConstants.RETRY_INTERVAL, this.retryIntervalTxt.getText());
            ps.setValue(PreferenceConstants.SLEEP_VUL, this.vulSleepTxt.getText());
            ps.setValue(PreferenceConstants.SLEEP_LIB, this.libSleepTxt.getText());
            ps.setValue(PreferenceConstants.SLEEP_ROUTECOVERAGE, this.routeCoverageSleepTxt.getText());
            ps.setValue(PreferenceConstants.SLEEP_SBOM, this.sbomSleepTxt.getText());
            ps.setValue(PreferenceConstants.SLEEP_SCANRESULT, this.scanResultSleepTxt.getText());
        }
        return true;
    }
}
