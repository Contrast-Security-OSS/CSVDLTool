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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.contrastsecurity.csvdltool.CSVColmunEnum;

public class CSVColumnPreferencePage extends PreferencePage {

    private List<String> colmunList = new ArrayList<String>();
    private CheckboxTableViewer viewer;

    public CSVColumnPreferencePage() {
        super("出力項目設定");
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

        Group csvColumnGrp = new Group(composite, SWT.NONE);
        GridLayout csvGrpLt = new GridLayout(3, false);
        csvGrpLt.marginWidth = 10;
        csvGrpLt.marginHeight = 10;
        csvGrpLt.horizontalSpacing = 5;
        csvGrpLt.verticalSpacing = 10;
        csvColumnGrp.setLayout(csvGrpLt);
        GridData csvGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvGrpGrDt.horizontalSpan = 2;
        csvColumnGrp.setLayoutData(csvGrpGrDt);
        csvColumnGrp.setText("CSV出力");

        // ========== 出力項目テーブル ========== //
        for (CSVColmunEnum colEnum : CSVColmunEnum.values()) {
            colmunList.add(colEnum.getCulumn());
        }
        String targetColumns = preferenceStore.getString(PreferenceConstants.CSV_COLUMN);
        List<String> validColumnList = new ArrayList<String>();
        if (targetColumns.trim().length() > 0) {
            for (String targetColumn : targetColumns.split(",")) {
                validColumnList.add(CSVColmunEnum.valueOf(targetColumn.trim()).getCulumn());
            }
        }
        final Table table = new Table(csvColumnGrp, SWT.CHECK | SWT.BORDER);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 2;
        table.setLayoutData(tableGrDt);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer = new CheckboxTableViewer(table);
        viewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setInput(colmunList);
        viewer.setCheckedElements(validColumnList.toArray());

        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        ColumnLayoutData layoutData = new ColumnWeightData(100);
        TableColumn column = new TableColumn(table, SWT.NONE, 0);
        layout.addColumnData(layoutData);
        column.setResizable(layoutData.resizable);
        column.setText("項目名");

        Composite buttonGrp = new Composite(csvColumnGrp, SWT.NONE);
        buttonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttonGrp.setLayout(new GridLayout(1, true));

        final Button allOnBtn = new Button(buttonGrp, SWT.NULL);
        allOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOnBtn.setText("すべてオン");
        allOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });

        final Button allOffBtn = new Button(buttonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOffBtn.setText("すべてオフ");
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });

        final Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        defaultBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        defaultBtn.setText("デフォルトに戻す");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String defaultColumns = preferenceStore.getDefaultString(PreferenceConstants.CSV_COLUMN);
                List<String> validColumnList = new ArrayList<String>();
                if (defaultColumns.trim().length() > 0) {
                    for (String defaultColumn : defaultColumns.split(",")) {
                        validColumnList.add(CSVColmunEnum.valueOf(defaultColumn.trim()).getCulumn());
                    }
                }
                viewer.setCheckedElements(validColumnList.toArray());
                viewer.refresh();
            }
        });

        Button applyBtn = new Button(composite, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtnGrDt.horizontalSpan = 3;
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
        Object[] elements = viewer.getCheckedElements();
        List<String> checkedList = new ArrayList<String>();
        for (Object element : elements) {
            checkedList.add(element.toString());
        }

        List<String> list = new ArrayList<String>();
        for (String colName : checkedList) {
            list.add(CSVColmunEnum.getByName(colName).name());
        }
        if (list.isEmpty()) {
            errors.add("CSV出力項目を１つ以上選択してください。");
        } else {
            ps.setValue(PreferenceConstants.CSV_COLUMN, String.join(",", list));
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "出力項目設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
