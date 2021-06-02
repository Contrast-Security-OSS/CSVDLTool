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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.LibCSVColmunEnum;
import com.contrastsecurity.csvdltool.model.LibCSVColumn;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class LibCSVColumnPreferencePage2 extends PreferencePage {

    private Button outCsvHeaderFlg;
    private List<LibCSVColumn> columnList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Text> separateTextList = new ArrayList<Text>();
    private Table table;
    private Text csvFileForamtTxt;
    private Text sleepTxt;

    public LibCSVColumnPreferencePage2() {
        super("ライブラリ情報の出力設定（新）");
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
        csvColumnGrp.setText("CSV出力内容の設定");

        outCsvHeaderFlg = new Button(csvColumnGrp, SWT.CHECK);
        GridData outCsvHeaderFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        outCsvHeaderFlgGrDt.horizontalSpan = 3;
        outCsvHeaderFlg.setLayoutData(outCsvHeaderFlgGrDt);
        outCsvHeaderFlg.setText("カラムヘッダ（項目名）を出力");
        if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB)) {
            outCsvHeaderFlg.setSelection(true);
        }

        String columnJsonStr = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_LIB);
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<LibCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(getShell(), "ライブラリ出力項目の読み込み", String.format("ライブラリ出力項目の内容に問題があります。\r\n%s", columnJsonStr));
                columnList = new ArrayList<LibCSVColumn>();
            }
        } else {
            columnList = new ArrayList<LibCSVColumn>();
            for (LibCSVColmunEnum colEnum : LibCSVColmunEnum.sortedValues()) {
                columnList.add(new LibCSVColumn(colEnum));
            }
        }
        // Clean up ここから
        List<Integer> irregularIndexes = new ArrayList<Integer>();
        for (int i = 0; i < columnList.size(); i++) {
            Object obj = columnList.get(i);
            if (!(obj instanceof LibCSVColumn)) {
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
        column1.setText("出力");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(200);
        column2.setText("項目名");
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(75);
        column3.setText("区切り文字");
        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(350);
        column4.setText("備考");

        for (LibCSVColumn col : columnList) {
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
                        LibCSVColumn targetColumn = columnList.get(sourceIndex);
                        columnList.add(targetIndex, targetColumn);
                        columnList.remove(sourceIndex);
                    } else if (sourceIndex > targetIndex) {
                        LibCSVColumn targetColumn = columnList.remove(sourceIndex);
                        columnList.add(targetIndex, targetColumn);
                    }
                    for (Button button : checkBoxList) {
                        button.dispose();
                    }
                    checkBoxList.clear();
                    for (Text text : separateTextList) {
                        text.dispose();
                    }
                    separateTextList.clear();
                    table.removeAll();
                    for (LibCSVColumn col : columnList) {
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
                for (LibCSVColumn col : columnList) {
                    col.setValid(true);
                }
                for (Button button : checkBoxList) {
                    button.setSelection(true);
                }
            }
        });

        final Button allOffBtn = new Button(chkButtonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOffBtn.setText("すべてオフ");
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
                for (LibCSVColumn col : columnList) {
                    col.setValid(false);
                }
            }
        });

        Label descLabel = new Label(csvColumnGrp, SWT.LEFT);
        descLabel.setText("・ ドラッグアンドドロップで項目の並び替えが可能です。\r\n・ 複数の値が出力される項目については、区切り文字が変更可能です。改行させる場合は\\r\\nをご指定してください。");
        GridData descLabelGrDt = new GridData(GridData.FILL_HORIZONTAL);
        descLabelGrDt.horizontalSpan = 3;
        descLabel.setLayoutData(descLabelGrDt);

        Group csvFileFormatGrp = new Group(composite, SWT.NONE);
        GridLayout csvFileFormatGrpLt = new GridLayout(1, false);
        csvFileFormatGrpLt.marginWidth = 10;
        csvFileFormatGrpLt.marginHeight = 10;
        csvFileFormatGrpLt.horizontalSpacing = 10;
        csvFileFormatGrp.setLayout(csvFileFormatGrpLt);
        GridData csvFileFormatGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFormatGrpGrDt.horizontalSpan = 2;
        csvFileFormatGrp.setLayoutData(csvFileFormatGrpGrDt);
        csvFileFormatGrp.setText("CSV出力ファイルフォーマット（またはフォルダ名）");

        csvFileForamtTxt = new Text(csvFileFormatGrp, SWT.BORDER);
        csvFileForamtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        csvFileForamtTxt.setText(preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
        csvFileForamtTxt.setMessage(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
        Label csvFileFormatHint = new Label(csvFileFormatGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText("※ java.text.SimpleDateFormatの書式としてください。\r\n例) 'vul_'yyyy-MM-dd_HHmmss");

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(4, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("制御");

        // ========== 脆弱性取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("脆弱性取得間隔スリープ（ミリ秒）：");
        sleepTxt = new Text(ctrlGrp, SWT.BORDER);
        sleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sleepTxt.setText(preferenceStore.getString(PreferenceConstants.SLEEP_LIB));

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
                columnList.clear();
                for (Button button : checkBoxList) {
                    button.dispose();
                }
                checkBoxList.clear();
                for (Text text : separateTextList) {
                    text.dispose();
                }
                separateTextList.clear();
                table.removeAll();
                for (LibCSVColmunEnum colEnum : LibCSVColmunEnum.sortedValues()) {
                    columnList.add(new LibCSVColumn(colEnum));
                }
                for (LibCSVColumn col : columnList) {
                    addColToTable(col, -1);
                }
                csvFileForamtTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB));
                sleepTxt.setText(preferenceStore.getDefaultString(PreferenceConstants.SLEEP_LIB));
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
        ps.setValue(PreferenceConstants.SLEEP_LIB, this.sleepTxt.getText());
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_LIB, this.csvFileForamtTxt.getText());
        ps.setValue(PreferenceConstants.CSV_COLUMN_LIB, new Gson().toJson(this.columnList));
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "脆弱性情報の出力設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }

    private void addColToTable(LibCSVColumn col, int index) {
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
                LibCSVColumn targetColumn = columnList.get(clickIndex);
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
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text modifyText = (Text) e.getSource();
                    int modifyIndex = separateTextList.indexOf(modifyText);
                    String text = modifyText.getText();
                    LibCSVColumn targetColumn = columnList.get(modifyIndex);
                    targetColumn.setSeparateStr(text);
                }
            });
            text.pack();
            editor2.grabHorizontal = true;
            editor2.horizontalAlignment = SWT.LEFT;
            editor2.setEditor(text, item, 3);
            separateTextList.add(text);
        } else {
            item.setText(3, "");
            separateTextList.add(new Text(table, SWT.NONE));
        }
        item.setText(4, col.getColumn().getRemarks());
    }
}
