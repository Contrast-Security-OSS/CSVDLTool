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
import org.eclipse.swt.graphics.Point;
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
    private CheckboxTableViewer srcIpViewer;
    private CheckboxTableViewer appViewer;
    private CheckboxTableViewer ruleViewer;
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

        // #################### ソースIP #################### //
        Group srcIpGrp = new Group(composite, SWT.NONE);
        GridLayout srcIpGrpLt = new GridLayout(1, false);
        srcIpGrpLt.marginWidth = 10;
        srcIpGrpLt.marginHeight = 10;
        srcIpGrp.setLayout(srcIpGrpLt);
        GridData srcIpGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcIpGrp.setLayoutData(srcIpGrpGrDt);
        srcIpGrp.setText("ソースIP");

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
        for (Filter filter : filterMap.get(FilterEnum.SOURCEIP)) {
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
        srcIpBulkBtn.setText("すべて");
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
        appGrp.setLayoutData(appGrpGrDt);
        appGrp.setText("アプリケーション");

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
        appBulkBtn.setText("すべて");
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
        ruleGrp.setLayoutData(ruleGrpGrDt);
        ruleGrp.setText("ルール");

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
        ruleBulkBtn.setText("すべて");
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

        return composite;
    }

    private void checkStateUpdate() {
        Object[] srcIpItems = srcIpViewer.getCheckedElements();
        List<String> strItems = new ArrayList<String>();
        for (Object item : srcIpItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.SOURCEIP)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
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
        support.firePropertyChange("attackEventFilter", null, filterMap);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(640, 480);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("攻撃イベントフィルター");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}