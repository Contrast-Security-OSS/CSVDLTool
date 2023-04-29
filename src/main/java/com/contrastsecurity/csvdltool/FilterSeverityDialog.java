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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.contrastsecurity.csvdltool.model.Filter;

public class FilterSeverityDialog extends Dialog {

    private Set<Filter> filters;
    private List<String> labels;
    private CheckboxTableViewer viewer;

    public FilterSeverityDialog(Shell parentShell, Set<Filter> filters) {
        super(parentShell);
        this.filters = filters;
        this.labels = new ArrayList<String>();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 5;
        compositeLt.horizontalSpacing = 5;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(compositeGrDt);

        final Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        viewer = new CheckboxTableViewer(table);
        viewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> labelList = new ArrayList<String>();
        List<String> validLabelList = new ArrayList<String>();
        for (Filter filter : this.filters) {
            labelList.add(filter.getLabel());
            if (filter.isValid()) {
                validLabelList.add(filter.getLabel());
            } else {
            }
        }
        if (validLabelList.isEmpty()) {
            validLabelList.addAll(labelList);
        }
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(labelList);
        viewer.setCheckedElements(validLabelList.toArray());

        final Button bulkBtn = new Button(composite, SWT.CHECK);
        bulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bulkBtn.setText(Messages.getString("filterseveritydialog.check.all")); //$NON-NLS-1$
        bulkBtn.setSelection(true);
        bulkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (bulkBtn.getSelection()) {
                    validLabelList.addAll(labelList);
                    viewer.setCheckedElements(validLabelList.toArray());
                    viewer.refresh();
                } else {
                    viewer.setCheckedElements(new ArrayList<String>().toArray());
                    viewer.refresh();
                }
            }
        });
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        Object[] items = viewer.getCheckedElements();
        for (Object item : items) {
            labels.add((String) item);
        }
        if (labels.size() == this.filters.size()) {
            labels.clear();
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(275, 240);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("filterseveritydialog.dialog.title")); //$NON-NLS-1$
    }

    public List<String> getLabels() {
        return labels;
    }

}
