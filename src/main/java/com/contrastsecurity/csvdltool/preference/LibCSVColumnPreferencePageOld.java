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

import com.contrastsecurity.csvdltool.LibCSVColmunEnum;

public class LibCSVColumnPreferencePageOld extends PreferencePage {

    private Button outCsvHeaderFlg;
    private List<String> colmunList = new ArrayList<String>();
    private CheckboxTableViewer viewer;

    public LibCSVColumnPreferencePageOld() {
        super("ライブラリ情報の出力設定");
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
        for (LibCSVColmunEnum colEnum : LibCSVColmunEnum.sortedValues()) {
            colmunList.add(colEnum.getCulumn());
        }
        String targetColumns = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_LIB);
        List<String> validColumnList = new ArrayList<String>();
        if (targetColumns.trim().length() > 0) {
            try {
                for (String targetColumn : targetColumns.split(",")) {
                    validColumnList.add(LibCSVColmunEnum.valueOf(targetColumn.trim()).getCulumn());
                }
            } catch (IllegalArgumentException iae) {
                MessageDialog.openError(parent.getShell(), "出力項目設定", "設定値に問題があるためデフォルト設定に戻されました。\r\n再度、設定を行い適用してください。");
                targetColumns = preferenceStore.getDefaultString(PreferenceConstants.CSV_COLUMN_LIB);
                for (String targetColumn : targetColumns.split(",")) {
                    validColumnList.add(LibCSVColmunEnum.valueOf(targetColumn.trim()).getCulumn());
                }
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

        Composite chkButtonGrp = new Composite(csvColumnGrp, SWT.NONE);
        chkButtonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        chkButtonGrp.setLayout(new GridLayout(1, true));

        final Button allOnBtn = new Button(chkButtonGrp, SWT.NULL);
        allOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOnBtn.setText("すべてオン");
        allOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });

        final Button allOffBtn = new Button(chkButtonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOffBtn.setText("すべてオフ");
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });

        outCsvHeaderFlg = new Button(csvColumnGrp, SWT.CHECK);
        GridData outCsvHeaderFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        outCsvHeaderFlgGrDt.horizontalSpan = 2;
        outCsvHeaderFlg.setLayoutData(outCsvHeaderFlgGrDt);
        outCsvHeaderFlg.setText("カラムヘッダを出力");
        if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB)) {
            outCsvHeaderFlg.setSelection(true);
        }

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
                outCsvHeaderFlg.setSelection(preferenceStore.getDefaultBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB));
                String defaultColumns = preferenceStore.getDefaultString(PreferenceConstants.CSV_COLUMN_LIB);
                List<String> validColumnList = new ArrayList<String>();
                if (defaultColumns.trim().length() > 0) {
                    for (String defaultColumn : defaultColumns.split(",")) {
                        validColumnList.add(LibCSVColmunEnum.valueOf(defaultColumn.trim()).getCulumn());
                    }
                }
                viewer.setCheckedElements(validColumnList.toArray());
                viewer.refresh();
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
        ps.setValue(PreferenceConstants.CSV_OUT_HEADER_LIB, this.outCsvHeaderFlg.getSelection());
        Object[] elements = viewer.getCheckedElements();
        List<String> checkedList = new ArrayList<String>();
        for (Object element : elements) {
            checkedList.add(element.toString());
        }

        List<String> list = new ArrayList<String>();
        for (String colName : checkedList) {
            list.add(LibCSVColmunEnum.getByName(colName).name());
        }
        if (list.isEmpty()) {
            errors.add("CSV出力項目を１つ以上選択してください。");
        } else {
            ps.setValue(PreferenceConstants.CSV_COLUMN_LIB, String.join(",", list));
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "出力項目設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
