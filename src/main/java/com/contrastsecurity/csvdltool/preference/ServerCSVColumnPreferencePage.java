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

import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.ServerCSVColmunEnum;
import com.contrastsecurity.csvdltool.model.ServerCSVColumn;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ServerCSVColumnPreferencePage extends PreferencePage {

    private Button outCsvHeaderFlg;
    private List<ServerCSVColumn> columnList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Text> separateTextList = new ArrayList<Text>();
    private List<Text> trueTextList = new ArrayList<Text>();
    private List<Text> falseTextList = new ArrayList<Text>();
    private Table table;

    public ServerCSVColumnPreferencePage() {
        super(Messages.getString("servercsvcolumnpreferencepage.title")); //$NON-NLS-1$
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
        csvColumnGrp.setText(Messages.getString("servercsvcolumnpreferencepage.output.settings.group.title")); //$NON-NLS-1$

        outCsvHeaderFlg = new Button(csvColumnGrp, SWT.CHECK);
        GridData outCsvHeaderFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        outCsvHeaderFlgGrDt.horizontalSpan = 3;
        outCsvHeaderFlg.setLayoutData(outCsvHeaderFlgGrDt);
        outCsvHeaderFlg.setText(Messages.getString("servercsvcolumnpreferencepage.column.header.print.checkbox.label")); //$NON-NLS-1$
        if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_SERVER)) {
            outCsvHeaderFlg.setSelection(true);
        }

        String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_SERVER);
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<ServerCSVColumn>>() {
                }.getType());
                List<ServerCSVColumn> defaultList = new ArrayList<ServerCSVColumn>();
                for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                    defaultList.add(new ServerCSVColumn(colEnum));
                }
                if (columnList.size() != defaultList.size()) {
                    defaultList.stream().filter(p -> {
                        return (!columnList.contains(p));
                    }).forEach(p -> {
                        columnList.add(p);
                    });
                }
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(getShell(), Messages.getString("servercsvcolumnpreferencepage.message.dialog.title"), String.format("%s\r\n%s", Messages.getString("servercsvcolumnpreferencepage.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                columnList = new ArrayList<ServerCSVColumn>();
            }
        } else {
            columnList = new ArrayList<ServerCSVColumn>();
            for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                columnList.add(new ServerCSVColumn(colEnum));
            }
        }
        // Clean up ここから
        List<Integer> irregularIndexes = new ArrayList<Integer>();
        for (int i = 0; i < columnList.size(); i++) {
            Object obj = columnList.get(i);
            if (!(obj instanceof ServerCSVColumn)) {
                irregularIndexes.add(i);
            }
        }
        int[] irregularArray = irregularIndexes.stream().mapToInt(i -> i).toArray();
        for (int i = irregularArray.length - 1; i >= 0; i--) {
            columnList.remove(i);
        }
        // Clean up ここまで
        if (columnList.isEmpty()) {
            for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                columnList.add(new ServerCSVColumn(colEnum));
            }
        }

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
        column1.setText(Messages.getString("servercsvcolumnpreferencepage.table.column0.title")); //$NON-NLS-1$
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(200);
        column2.setText(Messages.getString("servercsvcolumnpreferencepage.table.column1.title")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(75);
        column3.setText(Messages.getString("servercsvcolumnpreferencepage.table.column2.title")); //$NON-NLS-1$
        TableColumn column4 = new TableColumn(table, SWT.CENTER);
        column4.setWidth(75);
        column4.setText("true"); //$NON-NLS-1$
        TableColumn column5 = new TableColumn(table, SWT.CENTER);
        column5.setWidth(75);
        column5.setText("false"); //$NON-NLS-1$
        TableColumn column6 = new TableColumn(table, SWT.LEFT);
        column6.setWidth(350);
        column6.setText(Messages.getString("servercsvcolumnpreferencepage.table.column3.title")); //$NON-NLS-1$

        for (ServerCSVColumn col : columnList) {
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
            }

            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
            }

            public void drop(DropTargetEvent event) {
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    DropTarget target = (DropTarget) event.widget;
                    Table table = (Table) target.getControl();
                    String sourceIndexStr = (String) event.data;
                    int sourceIndex = Integer.valueOf(sourceIndexStr);
                    TableItem item = (TableItem) event.item;
                    int targetIndex = -1;
                    if (item != null) {
                        targetIndex = table.indexOf(item);
                    } else {
                        targetIndex = columnList.size();
                    }
                    if (sourceIndex == targetIndex) {
                        return;
                    }
                    if (sourceIndex < targetIndex) {
                        ServerCSVColumn targetColumn = columnList.get(sourceIndex);
                        columnList.add(targetIndex, targetColumn);
                        columnList.remove(sourceIndex);
                    } else if (sourceIndex > targetIndex) {
                        ServerCSVColumn targetColumn = columnList.remove(sourceIndex);
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
                    for (Text text : trueTextList) {
                        text.dispose();
                    }
                    trueTextList.clear();
                    for (Text text : falseTextList) {
                        text.dispose();
                    }
                    falseTextList.clear();
                    table.clearAll();
                    table.removeAll();
                    for (ServerCSVColumn col : columnList) {
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
        allOnBtn.setText(Messages.getString("servercsvcolumnpreferencepage.all.on.button.title")); //$NON-NLS-1$
        allOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ServerCSVColumn col : columnList) {
                    col.setValid(true);
                }
                for (Button button : checkBoxList) {
                    button.setSelection(true);
                }
            }
        });

        final Button allOffBtn = new Button(chkButtonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allOffBtn.setText(Messages.getString("servercsvcolumnpreferencepage.all.off.button.title")); //$NON-NLS-1$
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
                for (ServerCSVColumn col : columnList) {
                    col.setValid(false);
                }
            }
        });

        Label descLabel = new Label(csvColumnGrp, SWT.LEFT);
        List<String> descLabelList = new ArrayList<String>();
        descLabelList.add(Messages.getString("servercsvcolumnpreferencepage.table.desc.draganddrop")); //$NON-NLS-1$
        descLabelList.add(Messages.getString("servercsvcolumnpreferencepage.table.desc.delimiter")); //$NON-NLS-1$
        descLabelList.add(Messages.getString("servercsvcolumnpreferencepage.table.desc.truefalsestr")); //$NON-NLS-1$
        descLabel.setText(String.join("\r\n", descLabelList)); //$NON-NLS-1$
        GridData descLabelGrDt = new GridData(GridData.FILL_HORIZONTAL);
        descLabelGrDt.horizontalSpan = 3;
        descLabel.setLayoutData(descLabelGrDt);

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
        defaultBtnGrDt.widthHint = 100;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText(Messages.getString("servercsvcolumnpreferencepage.restoredefaults.button.title")); //$NON-NLS-1$
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                outCsvHeaderFlg.setSelection(ps.getDefaultBoolean(PreferenceConstants.CSV_OUT_HEADER_SERVER));
                columnList.clear();
                for (Button button : checkBoxList) {
                    button.dispose();
                }
                checkBoxList.clear();
                for (Text text : separateTextList) {
                    text.dispose();
                }
                separateTextList.clear();
                for (Text text : trueTextList) {
                    text.dispose();
                }
                trueTextList.clear();
                for (Text text : falseTextList) {
                    text.dispose();
                }
                falseTextList.clear();
                table.clearAll();
                table.removeAll();
                for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                    columnList.add(new ServerCSVColumn(colEnum));
                }
                for (ServerCSVColumn col : columnList) {
                    addColToTable(col, -1);
                }
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText(Messages.getString("servercsvcolumnpreferencepage.apply.button.title")); //$NON-NLS-1$
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
        ps.setValue(PreferenceConstants.CSV_OUT_HEADER_SERVER, this.outCsvHeaderFlg.getSelection());
        ps.setValue(PreferenceConstants.CSV_COLUMN_SERVER, new Gson().toJson(this.columnList));
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("servercsvcolumnpreferencepage.dialog.title"), String.join("\r\n", errors)); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return true;
    }

    private void addColToTable(ServerCSVColumn col, int index) {
        if (col == null) {
            return;
        }
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        if (col.isValid()) {
            button.setSelection(true);
        }
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button triggerBtn = (Button) e.getSource();
                int clickIndex = checkBoxList.indexOf(triggerBtn);
                boolean selected = triggerBtn.getSelection();
                ServerCSVColumn targetColumn = columnList.get(clickIndex);
                targetColumn.setValid(selected);
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
            text.setTextLimit(4);
            text.setText(col.getSeparateStr());
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text modifyText = (Text) e.getSource();
                    int modifyIndex = separateTextList.indexOf(modifyText);
                    String text = modifyText.getText();
                    ServerCSVColumn targetColumn = columnList.get(modifyIndex);
                    targetColumn.setSeparateStr(text);
                }
            });
            text.pack();
            editor2.grabHorizontal = true;
            editor2.horizontalAlignment = SWT.LEFT;
            editor2.setEditor(text, item, 3);
            separateTextList.add(text);
        } else {
            item.setText(3, ""); //$NON-NLS-1$
            separateTextList.add(new Text(table, SWT.NONE));
        }
        if (col.isBoolean()) {
            // TrueStr
            TableEditor editor3 = new TableEditor(table);
            Text trueText = new Text(table, SWT.NONE);
            trueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            trueText.setTextLimit(10);
            trueText.setText(col.getTrueStr());
            trueText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text modifyText = (Text) e.getSource();
                    int modifyIndex = trueTextList.indexOf(modifyText);
                    String text = modifyText.getText();
                    ServerCSVColumn targetColumn = columnList.get(modifyIndex);
                    targetColumn.setTrueStr(text);
                }
            });
            trueText.pack();
            editor3.grabHorizontal = true;
            editor3.horizontalAlignment = SWT.CENTER;
            editor3.setEditor(trueText, item, 4);
            trueTextList.add(trueText);
            // FalseStr
            TableEditor editor4 = new TableEditor(table);
            Text falseText = new Text(table, SWT.NONE);
            falseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            falseText.setTextLimit(10);
            falseText.setText(col.getFalseStr());
            falseText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    Text modifyText = (Text) e.getSource();
                    int modifyIndex = falseTextList.indexOf(modifyText);
                    String text = modifyText.getText();
                    ServerCSVColumn targetColumn = columnList.get(modifyIndex);
                    targetColumn.setFalseStr(text);
                }
            });
            falseText.pack();
            editor4.grabHorizontal = true;
            editor4.horizontalAlignment = SWT.CENTER;
            editor4.setEditor(falseText, item, 5);
            falseTextList.add(falseText);
        } else {
            item.setText(4, ""); //$NON-NLS-1$
            item.setText(5, ""); //$NON-NLS-1$
            trueTextList.add(new Text(table, SWT.NONE));
            falseTextList.add(new Text(table, SWT.NONE));
        }
        item.setText(6, col.getColumn().getRemarks());
    }
}
