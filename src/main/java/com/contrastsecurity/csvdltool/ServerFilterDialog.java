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

public class ServerFilterDialog extends Dialog {

    private Map<FilterEnum, Set<Filter>> filterMap;
    private CheckboxTableViewer languageViewer;
    private CheckboxTableViewer agentVersionViewer;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public ServerFilterDialog(Shell parentShell, Map<FilterEnum, Set<Filter>> filterMap) {
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

        // #################### 言語 #################### //
        Group srcIpGrp = new Group(composite, SWT.NONE);
        GridLayout srcIpGrpLt = new GridLayout(1, false);
        srcIpGrpLt.marginWidth = 10;
        srcIpGrpLt.marginHeight = 10;
        srcIpGrp.setLayout(srcIpGrpLt);
        GridData srcIpGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcIpGrp.setLayoutData(srcIpGrpGrDt);
        srcIpGrp.setText(Messages.getString("serverfilterdialog.filter.agent.language.group.title")); //$NON-NLS-1$

        final Table srcIpTable = new Table(srcIpGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData srcIpTableGrDt = new GridData(GridData.FILL_BOTH);
        srcIpTable.setLayoutData(srcIpTableGrDt);
        languageViewer = new CheckboxTableViewer(srcIpTable);
        languageViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> srcLabelList = new ArrayList<String>();
        List<String> srcValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.LANGUAGE)) {
            srcLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                srcValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (srcValidLabelList.isEmpty()) {
            srcValidLabelList.addAll(srcLabelList);
        }
        languageViewer.setContentProvider(new ArrayContentProvider());
        languageViewer.setInput(srcLabelList);
        languageViewer.setCheckedElements(srcValidLabelList.toArray());
        languageViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button srcIpBulkBtn = new Button(srcIpGrp, SWT.CHECK);
        srcIpBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcIpBulkBtn.setText(Messages.getString("serverfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        srcIpBulkBtn.setSelection(true);
        srcIpBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (srcIpBulkBtn.getSelection()) {
                    srcValidLabelList.addAll(srcLabelList);
                    languageViewer.setCheckedElements(srcValidLabelList.toArray());
                    languageViewer.refresh();
                } else {
                    languageViewer.setCheckedElements(new ArrayList<String>().toArray());
                    languageViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        // #################### エージェントバージョン #################### //
        Group appGrp = new Group(composite, SWT.NONE);
        GridLayout appGrpLt = new GridLayout(1, false);
        appGrpLt.marginWidth = 10;
        appGrpLt.marginHeight = 10;
        appGrp.setLayout(appGrpLt);
        GridData appGrpGrDt = new GridData(GridData.FILL_BOTH);
        appGrp.setLayoutData(appGrpGrDt);
        appGrp.setText(Messages.getString("serverfilterdialog.filter.agent.version.group.title")); //$NON-NLS-1$

        final Table appTable = new Table(appGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData appTableGrDt = new GridData(GridData.FILL_BOTH);
        appTable.setLayoutData(appTableGrDt);
        agentVersionViewer = new CheckboxTableViewer(appTable);
        agentVersionViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> appLabelList = new ArrayList<String>();
        List<String> appValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.AGENT_VERSION)) {
            appLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                appValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (appValidLabelList.isEmpty()) {
            appValidLabelList.addAll(appLabelList);
        }
        agentVersionViewer.setContentProvider(new ArrayContentProvider());
        agentVersionViewer.setInput(appLabelList);
        agentVersionViewer.setCheckedElements(appValidLabelList.toArray());
        agentVersionViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });

        final Button appBulkBtn = new Button(appGrp, SWT.CHECK);
        appBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appBulkBtn.setText(Messages.getString("serverfilterdialog.filter.checkbox.all.label")); //$NON-NLS-1$
        appBulkBtn.setSelection(true);
        appBulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (appBulkBtn.getSelection()) {
                    appValidLabelList.addAll(appLabelList);
                    agentVersionViewer.setCheckedElements(appValidLabelList.toArray());
                    agentVersionViewer.refresh();
                } else {
                    agentVersionViewer.setCheckedElements(new ArrayList<String>().toArray());
                    agentVersionViewer.refresh();
                }
                checkStateUpdate();
            }
        });

        return composite;
    }

    private void checkStateUpdate() {
        Object[] srcIpItems = languageViewer.getCheckedElements();
        List<String> strItems = new ArrayList<String>();
        for (Object item : srcIpItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.LANGUAGE)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        Object[] appItems = agentVersionViewer.getCheckedElements();
        strItems.clear();
        for (Object item : appItems) {
            strItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.AGENT_VERSION)) {
            if (strItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        support.firePropertyChange("serverFilter", null, filterMap); //$NON-NLS-1$
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("serverfilterdialog.close.button.title"), true); //$NON-NLS-1$
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
        newShell.setText(Messages.getString("serverfilterdialog.title")); //$NON-NLS-1$
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
