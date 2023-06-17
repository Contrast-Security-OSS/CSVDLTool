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

package com.contrastsecurity.csvdltool;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.contrastsecurity.csvdltool.model.Filter;

public class AttackEventFilterDialog extends Dialog {

    private Map<FilterEnum, Set<Filter>> filterMap;
    private CheckboxTableViewer srcNameViewer;
    private CheckboxTableViewer srcIpViewer;
    private CheckboxTableViewer appViewer;
    private CheckboxTableViewer ruleViewer;
    private CheckboxTableViewer tagViewer;
    private CheckboxTableViewer termViewer;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public AttackEventFilterDialog(Shell parentShell, Map<FilterEnum, Set<Filter>> filterMap) {
        super(parentShell);
        this.filterMap = filterMap;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(3, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 5;
        compositeLt.horizontalSpacing = 5;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(compositeGrDt);

        // #################### ソース名 #################### //
        Group srcNameGrp = new Group(composite, SWT.NONE);
        GridLayout srcNameGrpLt = new GridLayout(1, false);
        srcNameGrpLt.marginWidth = 10;
        srcNameGrpLt.marginHeight = 10;
        srcNameGrp.setLayout(srcNameGrpLt);
        GridData srcNameGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcNameGrpGrDt.minimumWidth = 200;
        srcNameGrp.setLayoutData(srcNameGrpGrDt);
        srcNameGrp.setText(Messages.getString("attackeventfilterdialog.filter.source.name.group.title")); //$NON-NLS-1$

        final Table srcNameTable = new Table(srcNameGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData srcNameTableGrDt = new GridData(GridData.FILL_BOTH);
        srcNameTable.setLayoutData(srcNameTableGrDt);
        srcNameViewer = new CheckboxTableViewer(srcNameTable);
        srcNameViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> srcNameLabelList = new ArrayList<String>();
        List<String> srcNameValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.SOURCE_NAME)) {
            srcNameLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                srcNameValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (srcNameValidLabelList.isEmpty()) {
            srcNameValidLabelList.addAll(srcNameLabelList);
        }
        srcNameViewer.setContentProvider(new ArrayContentProvider());
        srcNameViewer.setInput(srcNameLabelList);
        srcNameViewer.setCheckedElements(srcNameValidLabelList.toArray());
        srcNameViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button srcNameBulkBtn = new Button(srcNameGrp, SWT.CHECK);
        srcNameBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcNameBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        srcNameBulkBtn.setSelection(true);
        srcNameBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (srcNameBulkBtn.getSelection()) {
                    srcNameValidLabelList.addAll(srcNameLabelList);
                    srcNameViewer.setCheckedElements(srcNameValidLabelList.toArray());
                    srcNameViewer.refresh();
                } else {
                    srcNameViewer.setCheckedElements(new ArrayList<String>().toArray());
                    srcNameViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### ソースIP #################### //
        Group srcIpGrp = new Group(composite, SWT.NONE);
        GridLayout srcIpGrpLt = new GridLayout(1, false);
        srcIpGrpLt.marginWidth = 10;
        srcIpGrpLt.marginHeight = 10;
        srcIpGrp.setLayout(srcIpGrpLt);
        GridData srcIpGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcIpGrpGrDt.minimumWidth = 200;
        srcIpGrp.setLayoutData(srcIpGrpGrDt);
        srcIpGrp.setText(Messages.getString("attackeventfilterdialog.filter.source.ip.group.title")); //$NON-NLS-1$

        final Table srcIpTable = new Table(srcIpGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData srcIpTableGrDt = new GridData(GridData.FILL_BOTH);
        srcIpTable.setLayoutData(srcIpTableGrDt);
        srcIpViewer = new CheckboxTableViewer(srcIpTable);
        srcIpViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> srcLabelList = new ArrayList<String>();
        List<String> srcValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.SOURCE_IP)) {
            srcLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                srcValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (srcValidLabelList.isEmpty()) {
            srcValidLabelList.addAll(srcLabelList);
        }
        srcIpViewer.setContentProvider(new ArrayContentProvider());
        srcIpViewer.setInput(srcLabelList);
        srcIpViewer.setCheckedElements(srcValidLabelList.toArray());
        srcIpViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button srcIpBulkBtn = new Button(srcIpGrp, SWT.CHECK);
        srcIpBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcIpBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        srcIpBulkBtn.setSelection(true);
        srcIpBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (srcIpBulkBtn.getSelection()) {
                    srcValidLabelList.addAll(srcLabelList);
                    srcIpViewer.setCheckedElements(srcValidLabelList.toArray());
                    srcIpViewer.refresh();
                } else {
                    srcIpViewer.setCheckedElements(new ArrayList<String>().toArray());
                    srcIpViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### アプリケーション #################### //
        Group appGrp = new Group(composite, SWT.NONE);
        GridLayout appGrpLt = new GridLayout(1, false);
        appGrpLt.marginWidth = 10;
        appGrpLt.marginHeight = 10;
        appGrp.setLayout(appGrpLt);
        GridData appGrpGrDt = new GridData(GridData.FILL_BOTH);
        appGrpGrDt.minimumWidth = 200;
        appGrp.setLayoutData(appGrpGrDt);
        appGrp.setText(Messages.getString("attackeventfilterdialog.filter.application.name.group.title")); //$NON-NLS-1$

        final Table appTable = new Table(appGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData appTableGrDt = new GridData(GridData.FILL_BOTH);
        appTable.setLayoutData(appTableGrDt);
        appViewer = new CheckboxTableViewer(appTable);
        appViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> appLabelList = new ArrayList<String>();
        List<String> appValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.APPLICATION)) {
            appLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                appValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (appValidLabelList.isEmpty()) {
            appValidLabelList.addAll(appLabelList);
        }
        appViewer.setContentProvider(new ArrayContentProvider());
        appViewer.setInput(appLabelList);
        appViewer.setCheckedElements(appValidLabelList.toArray());
        appViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button appBulkBtn = new Button(appGrp, SWT.CHECK);
        appBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        appBulkBtn.setSelection(true);
        appBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (appBulkBtn.getSelection()) {
                    appValidLabelList.addAll(appLabelList);
                    appViewer.setCheckedElements(appValidLabelList.toArray());
                    appViewer.refresh();
                } else {
                    appViewer.setCheckedElements(new ArrayList<String>().toArray());
                    appViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### ルール #################### //
        Group ruleGrp = new Group(composite, SWT.NONE);
        GridLayout ruleGrpLt = new GridLayout(1, false);
        ruleGrpLt.marginWidth = 10;
        ruleGrpLt.marginHeight = 10;
        ruleGrp.setLayout(ruleGrpLt);
        GridData ruleGrpGrDt = new GridData(GridData.FILL_BOTH);
        ruleGrpGrDt.minimumWidth = 200;
        ruleGrp.setLayoutData(ruleGrpGrDt);
        ruleGrp.setText(Messages.getString("attackeventfilterdialog.filter.rule.title.group.title")); //$NON-NLS-1$

        final Table ruleTable = new Table(ruleGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData ruleTableGrDt = new GridData(GridData.FILL_BOTH);
        ruleTable.setLayoutData(ruleTableGrDt);
        ruleViewer = new CheckboxTableViewer(ruleTable);
        ruleViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> ruleLabelList = new ArrayList<String>();
        List<String> ruleValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.RULE)) {
            ruleLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                ruleValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (ruleValidLabelList.isEmpty()) {
            ruleValidLabelList.addAll(ruleLabelList);
        }
        ruleViewer.setContentProvider(new ArrayContentProvider());
        ruleViewer.setInput(ruleLabelList);
        ruleViewer.setCheckedElements(ruleValidLabelList.toArray());
        ruleViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button ruleBulkBtn = new Button(ruleGrp, SWT.CHECK);
        ruleBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ruleBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        ruleBulkBtn.setSelection(true);
        ruleBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ruleBulkBtn.getSelection()) {
                    ruleValidLabelList.addAll(ruleLabelList);
                    ruleViewer.setCheckedElements(ruleValidLabelList.toArray());
                    ruleViewer.refresh();
                } else {
                    ruleViewer.setCheckedElements(new ArrayList<String>().toArray());
                    ruleViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### タグ #################### //
        Group tagGrp = new Group(composite, SWT.NONE);
        GridLayout tagGrpLt = new GridLayout(1, false);
        tagGrpLt.marginWidth = 10;
        tagGrpLt.marginHeight = 10;
        tagGrp.setLayout(tagGrpLt);
        GridData tagGrpGrDt = new GridData(GridData.FILL_BOTH);
        tagGrpGrDt.minimumWidth = 200;
        tagGrp.setLayoutData(tagGrpGrDt);
        tagGrp.setText(Messages.getString("attackeventfilterdialog.filter.tag.group.title")); //$NON-NLS-1$

        final Table tagTable = new Table(tagGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData tagTableGrDt = new GridData(GridData.FILL_BOTH);
        tagTable.setLayoutData(tagTableGrDt);
        tagViewer = new CheckboxTableViewer(tagTable);
        tagViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> tagLabelList = new ArrayList<String>();
        List<String> tagValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.TAG)) {
            tagLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                tagValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (tagValidLabelList.isEmpty()) {
            tagValidLabelList.addAll(tagLabelList);
        }
        tagViewer.setContentProvider(new ArrayContentProvider());
        tagViewer.setInput(tagLabelList);
        tagViewer.setCheckedElements(tagValidLabelList.toArray());
        tagViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button tagBulkBtn = new Button(tagGrp, SWT.CHECK);
        tagBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        tagBulkBtn.setSelection(true);
        tagBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (tagBulkBtn.getSelection()) {
                    tagValidLabelList.addAll(tagLabelList);
                    tagViewer.setCheckedElements(tagValidLabelList.toArray());
                    tagViewer.refresh();
                } else {
                    tagViewer.setCheckedElements(new ArrayList<String>().toArray());
                    tagViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### 時間帯 #################### //
        if (filterMap.containsKey(FilterEnum.BUSINESS_HOURS)) {
            Group termGrp = new Group(composite, SWT.NONE);
            GridLayout termGrpLt = new GridLayout(1, false);
            termGrpLt.marginWidth = 10;
            termGrpLt.marginHeight = 10;
            termGrp.setLayout(termGrpLt);
            GridData termGrpGrDt = new GridData(GridData.FILL_BOTH);
            termGrpGrDt.minimumWidth = 200;
            termGrp.setLayoutData(termGrpGrDt);
            termGrp.setText(Messages.getString("attackeventfilterdialog.filter.date.range.group.title")); //$NON-NLS-1$

            final Table termTable = new Table(termGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
            GridData termTableGrDt = new GridData(GridData.FILL_BOTH);
            termTable.setLayoutData(termTableGrDt);
            termViewer = new CheckboxTableViewer(termTable);
            termViewer.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return element.toString();
                }
            });
            List<String> termLabelList = new ArrayList<String>();
            List<String> termValidLabelList = new ArrayList<String>();
            for (Filter filter : filterMap.get(FilterEnum.BUSINESS_HOURS)) {
                termLabelList.add(filter.getLabel());
                if (filter.isValid()) {
                    termValidLabelList.add(filter.getLabel());
                } else {
                }
            }
            if (termValidLabelList.isEmpty()) {
                termValidLabelList.addAll(termLabelList);
            }
            termViewer.setContentProvider(new ArrayContentProvider());
            termViewer.setInput(termLabelList);
            termViewer.setCheckedElements(termValidLabelList.toArray());
            termViewer.addCheckStateListener(new ICheckStateListener() {
                @Override
                public void checkStateChanged(CheckStateChangedEvent event) {
                    checkStateUpdate();
                }
            });

            final Button termBulkBtn = new Button(termGrp, SWT.CHECK);
            termBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            termBulkBtn.setText(Messages.getString("attackeventfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
            termBulkBtn.setSelection(true);
            termBulkBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (termBulkBtn.getSelection()) {
                        termValidLabelList.addAll(termLabelList);
                        termViewer.setCheckedElements(termValidLabelList.toArray());
                        termViewer.refresh();
                    } else {
                        termViewer.setCheckedElements(new ArrayList<String>().toArray());
                        termViewer.refresh();
                    }
                    checkStateUpdate();
                }
            });
        }

        return composite;
    }

    private void checkStateUpdate() {
        // ソース名
        Object[] srcNameItems = srcNameViewer.getCheckedElements();
        List<String> strItems = new ArrayList<String>();
        for (Object item : srcNameItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.SOURCE_NAME)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // ソースIP
        Object[] srcIpItems = srcIpViewer.getCheckedElements();
        for (Object item : srcIpItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.SOURCE_IP)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // アプリケーション名
        Object[] appItems = appViewer.getCheckedElements();
        strItems.clear();
        for (Object item : appItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.APPLICATION)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // 脆弱性ルール
        Object[] ruleItems = ruleViewer.getCheckedElements();
        strItems.clear();
        for (Object item : ruleItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.RULE)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // タグ
        Object[] tagItems = tagViewer.getCheckedElements();
        strItems.clear();
        for (Object item : tagItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.TAG)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // 時間帯
        if (filterMap.containsKey(FilterEnum.BUSINESS_HOURS)) {
            Object[] termItems = termViewer.getCheckedElements();
            strItems.clear();
            for (Object item : termItems) {
                strItems.add((String) item);
            }
            for (Filter filter : filterMap.get(FilterEnum.BUSINESS_HOURS)) {
                if (strItems.contains(filter.getLabel())) {
                    filter.setValid(true);
                } else {
                    filter.setValid(false);
                }
            }
        }
        support.firePropertyChange("attackEventFilter", null, filterMap); //$NON-NLS-1$
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("attackeventfilterdialog.close.button.title"), true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("attackeventfilterdialog.title")); //$NON-NLS-1$
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
