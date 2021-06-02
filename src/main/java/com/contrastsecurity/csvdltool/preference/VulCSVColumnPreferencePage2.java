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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.VulCSVColmunEnum;
import com.contrastsecurity.csvdltool.model.VulCSVColumn;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class VulCSVColumnPreferencePage2 extends PreferencePage {

    private Button outCsvHeaderFlg;
    private List<VulCSVColumn> columnList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private Table table;

    public VulCSVColumnPreferencePage2() {
        super("脆弱性情報の出力設定");
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

        String columnJsonStr = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_VUL);
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<VulCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(getShell(), "脆弱性出力項目の読み込み", String.format("脆弱性出力項目の内容に問題があります。\r\n%s", columnJsonStr));
                columnList = new ArrayList<VulCSVColumn>();
            }
        } else {
            columnList = new ArrayList<VulCSVColumn>();
            for (VulCSVColmunEnum colEnum : VulCSVColmunEnum.sortedValues()) {
                columnList.add(new VulCSVColumn(colEnum));
            }
        }
        // Clean up ここから
        List<Integer> irregularIndexes = new ArrayList<Integer>();
        for (int i = 0; i < columnList.size(); i++) {
            Object obj = columnList.get(i);
            if (!(obj instanceof VulCSVColumn)) {
                irregularIndexes.add(i);
            }
        }
        int[] irregularArray = irregularIndexes.stream().mapToInt(i -> i).toArray();
        for (int i = irregularArray.length - 1; i >= 0; i--) {
            columnList.remove(i);
        }
        // Clean up ここまで

        table = new Table(csvColumnGrp, SWT.BORDER | SWT.FULL_SELECTION);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        // tableGrDt.horizontalSpan = 2;
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.CENTER);
        column1.setWidth(50);
        column1.setText("有効");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(200);
        column2.setText("項目名");
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(75);
        column3.setText("区切り文字");
        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(350);
        column4.setText("備考");

        for (VulCSVColumn col : columnList) {
            this.addColToTable(col, -1);
        }

        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        DragSource source = new DragSource(table, DND.DROP_MOVE);
        source.setTransfer(types);
        source.addDragListener(new DragSourceAdapter() {
            public void dragSetData(DragSourceEvent event) {
                DragSource ds = (DragSource) event.widget;
                Table table = (Table) ds.getControl();
                TableItem[] selection = table.getSelection();
                event.data = selection[0].getText(2);
                int sourceIndex = table.indexOf(selection[0]);
                event.data = String.valueOf(sourceIndex);
            }
        });

        DropTarget target = new DropTarget(table, DND.DROP_MOVE);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                System.out.println("dragEnter");
                // Allow dropping text only
                // for (int i = 0, n = event.dataTypes.length; i < n; i++) {
                // if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
                // event.currentDataType = event.dataTypes[i];
                // }
                // }
            }

            public void dragOver(DropTargetEvent event) {
                // System.out.println("dragOver");
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
            }

            public void drop(DropTargetEvent event) {
                System.out.println("drop");
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    // Get the dropped data
                    DropTarget target = (DropTarget) event.widget;
                    Table table = (Table) target.getControl();
                    TableItem item = (TableItem) event.item;
                    System.out.println(item.getText(2));
                    String sourceIndexStr = (String) event.data;
                    int sourceIndex = Integer.valueOf(sourceIndexStr);
                    int targetIndex = table.indexOf(item);
                    System.out.println(sourceIndex);
                    System.out.println(targetIndex);
                    if (sourceIndex == targetIndex) {
                        return;
                    }
                    if (sourceIndex < targetIndex) {
                        VulCSVColumn targetColumn = columnList.get(sourceIndex);
                        columnList.add(targetIndex, targetColumn);
                        columnList.remove(sourceIndex);
                    } else if (sourceIndex > targetIndex) {
                        VulCSVColumn targetColumn = columnList.remove(sourceIndex);
                        columnList.add(targetIndex, targetColumn);
                    }
                    checkBoxList.clear();
                    table.removeAll();
                    for (VulCSVColumn col : columnList) {
                        addColToTable(col, -1);
                    }
                }
            }
        });

        Composite chkButtonGrp = new Composite(csvColumnGrp, SWT.NONE);
        chkButtonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        chkButtonGrp.setLayout(new GridLayout(1, true));

        final Button allOnBtn = new Button(chkButtonGrp, SWT.NULL);
        allOnBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOnBtn.setText("すべてオン");
        allOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        final Button allOffBtn = new Button(chkButtonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOffBtn.setText("すべてオフ");
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        outCsvHeaderFlg = new Button(csvColumnGrp, SWT.CHECK);
        GridData outCsvHeaderFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        outCsvHeaderFlgGrDt.horizontalSpan = 2;
        outCsvHeaderFlg.setLayoutData(outCsvHeaderFlgGrDt);
        outCsvHeaderFlg.setText("カラムヘッダを出力");
        if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_VUL)) {
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
                outCsvHeaderFlg.setSelection(preferenceStore.getDefaultBoolean(PreferenceConstants.CSV_OUT_HEADER_VUL));
                columnList = new ArrayList<VulCSVColumn>();
                for (VulCSVColmunEnum colEnum : VulCSVColmunEnum.sortedValues()) {
                    columnList.add(new VulCSVColumn(colEnum));
                }
                checkBoxList.clear();
                table.removeAll();
                for (VulCSVColumn col : columnList) {
                    addColToTable(col, -1);
                }
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
        // Object[] elements = viewer.getCheckedElements();
        // List<String> checkedList = new ArrayList<String>();
        // for (Object element : elements) {
        // checkedList.add(element.toString());
        // }
        //
        // List<String> list = new ArrayList<String>();
        // for (String colName : checkedList) {
        // list.add(VulCSVColmunEnum.getByName(colName).name());
        // }
        // if (list.isEmpty()) {
        // errors.add("CSV出力項目を１つ以上選択してください。");
        // } else {
        // ps.setValue(PreferenceConstants.CSV_COLUMN_VUL, String.join(",", list));
        // }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "出力項目設定", String.join("\r\n", errors));
            return false;
        }
        ps.setValue(PreferenceConstants.CSV_COLUMN_VUL, new Gson().toJson(this.columnList));
        return true;
    }

    private void addColToTable(VulCSVColumn col, int index) {
        if (col == null) {
            return;
        }
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        if (col.isValid()) {
            button.setSelection(true);
        }
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button triggerBtn = (Button) e.getSource();
                int clickIndex = checkBoxList.indexOf(triggerBtn);
                boolean selected = triggerBtn.getSelection();
                VulCSVColumn targetColumn = columnList.get(clickIndex);
                targetColumn.setValid(selected);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        button.pack();
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(table, SWT.CENTER, index);
        } else {
            item = new TableItem(table, SWT.CENTER);
        }
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.CENTER;
        editor.setEditor(button, item, 1);
        checkBoxList.add(button);
        item.setText(2, col.getColumn().getCulumn());
        if (col.isSeparate()) {
            TableEditor editor2 = new TableEditor(table);
            Text text = new Text(table, SWT.NONE);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setTextLimit(5);
            text.setText(col.getSeparateStr());
            text.pack();
            editor2.grabHorizontal = true;
            editor2.horizontalAlignment = SWT.LEFT;
            editor2.setEditor(text, item, 3);
        } else {
            item.setText(3, "");
        }
        item.setText(4, col.getColumn().getRemarks());
    }
}
