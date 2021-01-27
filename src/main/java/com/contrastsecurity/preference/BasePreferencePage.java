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

package com.contrastsecurity.preference;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.comware.Main;

public class BasePreferencePage extends PreferencePage {

    private List<String> dirList = new ArrayList<String>();
    private CheckboxTableViewer viewer;

    public BasePreferencePage() {
        super("基本設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        // ========== 現在読み込んでいる設定ファイル(teratermstation.properties)のパス ========== //
        Composite propPathGrp = new Composite(composite, SWT.NONE);
        GridLayout propPathGrpLt = new GridLayout(3, false);
        propPathGrpLt.marginBottom = 15;
        propPathGrp.setLayout(propPathGrpLt);
        GridData propPathGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        propPathGrpGrDt.horizontalSpan = 3;
        propPathGrp.setLayoutData(propPathGrpGrDt);

        new Label(propPathGrp, SWT.LEFT).setText("現在の設定ファイル：");
        Text propPathTxt = new Text(propPathGrp, SWT.BORDER);
        propPathTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        propPathTxt.setText(preferenceStore.getString(PreferenceConstants.CURRENT_PROP_PATH));
        propPathTxt.setEditable(false);
        Button ttmacroBtn = new Button(propPathGrp, SWT.NULL);
        ttmacroBtn.setText("フォルダを開く");
        ttmacroBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            public void widgetSelected(SelectionEvent event) {
                try {
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(new String[] { "explorer.exe", "/select," + preferenceStore.getString(PreferenceConstants.CURRENT_PROP_PATH) });
                } catch (Exception e) {
                    MessageDialog.openError(composite.getShell(), "現在の設定ファイル", "ディレクトリが見つかりません。");
                }
            }
        });

        new Label(propPathGrp, SWT.LEFT).setText("");
        Label propPathDesc = new Label(propPathGrp, SWT.LEFT);
        GridData propPathDescGrDt = new GridData(GridData.FILL_HORIZONTAL);
        propPathDescGrDt.horizontalSpan = 2;
        propPathDesc.setLayoutData(propPathDescGrDt);
        propPathDesc.setText("※ TeratermStationの使用中に設定内容を変更しないでください。");

        // ========== 接続定義テーブル ========== //
        String dirStrs = preferenceStore.getString(PreferenceConstants.TARGET_DIRS);
        List<String> validDirList = new ArrayList<String>();
        Pattern ptn = Pattern.compile("^<([^<>]+)>$", Pattern.DOTALL);
        if (dirStrs.trim().length() > 0) {
            for (String dirStr : dirStrs.split(",")) {
                Matcher matcher = ptn.matcher(dirStr);
                if (matcher.find()) {
                    dirStr = matcher.group(1);
                    validDirList.add(dirStr);
                }
                dirList.add(dirStr);
            }
        }

        final Table table = new Table(composite, SWT.CHECK | SWT.BORDER);
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
        viewer.setInput(dirList);
        viewer.setCheckedElements(validDirList.toArray());

        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        ColumnLayoutData layoutData = new ColumnWeightData(100);
        TableColumn column = new TableColumn(table, SWT.NONE, 0);
        layout.addColumnData(layoutData);
        column.setResizable(layoutData.resizable);
        column.setText("定義基点ディレクトリ");

        Composite buttonGrp = new Composite(composite, SWT.NONE);
        buttonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttonGrp.setLayout(new GridLayout(1, true));

        final Button addBtn = new Button(buttonGrp, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addBtn.setText("新規...");
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PathDialog pathDialog = new PathDialog(getShell(), "");
                int result = pathDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                String path = pathDialog.getDirPath();
                if (dirList.contains(path)) {
                    MessageDialog.openError(composite.getShell(), "定義基点ディレクトリ", "すでに設定されているパスです。");
                    return;
                }
                dirList.add(path);
                viewer.refresh();
                // チェックしなおし処理
                Object[] elements = viewer.getCheckedElements();
                List<String> elementList = new ArrayList<String>();
                for (Object element : elements) {
                    elementList.add(element.toString());
                }
                elementList.add(path);
                viewer.setCheckedElements(elementList.toArray());
            }
        });

        final Button chgBtn = new Button(buttonGrp, SWT.NULL);
        chgBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chgBtn.setText("編集...");
        chgBtn.setEnabled(false);
        chgBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                TableItem[] items = table.getSelection();
                boolean chked = viewer.getChecked(items[0].getText());
                PathDialog pathDialog = new PathDialog(getShell(), items[0].getText());
                int result = pathDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                String path = pathDialog.getDirPath();
                if (dirList.contains(path)) {
                    MessageDialog.openError(composite.getShell(), "定義基点ディレクトリ", "すでに設定されているパスです。");
                    return;
                }
                dirList.remove(index);
                dirList.add(index, path);
                viewer.refresh();
                viewer.setChecked(path, chked);
            }
        });

        final Button rmvBtn = new Button(buttonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText("削除");
        rmvBtn.setEnabled(false);
        rmvBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                dirList.remove(index);
                viewer.refresh();
            }
        });

        final Button upBtn = new Button(buttonGrp, SWT.NULL);
        upBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        upBtn.setText("上へ移動");
        upBtn.setEnabled(false);
        upBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index > 0) {
                    TableItem[] items = table.getSelection();
                    dirList.remove(index);
                    index--;
                    dirList.add(index, items[0].getText());
                    viewer.refresh();
                }
            }
        });

        final Button downBtn = new Button(buttonGrp, SWT.NULL);
        downBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        downBtn.setText("下へ移動");
        downBtn.setEnabled(false);
        downBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index < dirList.size()) {
                    TableItem[] items = table.getSelection();
                    dirList.remove(index);
                    index++;
                    dirList.add(index, items[0].getText());
                    viewer.refresh();
                }
            }
        });

        final Button explorerBtn = new Button(buttonGrp, SWT.NULL);
        explorerBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        explorerBtn.setText("開く");
        explorerBtn.setEnabled(false);
        explorerBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                Desktop desktop = Desktop.getDesktop();
                File dirToOpen = null;
                try {
                    dirToOpen = new File(items[0].getText());
                    desktop.open(dirToOpen);
                } catch (Exception e) {
                    MessageDialog.openError(composite.getShell(), "定義基点ディレクトリ", "ディレクトリが見つかりません。");
                }
            }
        });

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index < 0) {
                    chgBtn.setEnabled(false);
                    rmvBtn.setEnabled(false);
                    upBtn.setEnabled(false);
                    downBtn.setEnabled(false);
                    explorerBtn.setEnabled(false);
                } else {
                    chgBtn.setEnabled(true);
                    rmvBtn.setEnabled(true);
                    upBtn.setEnabled(true);
                    downBtn.setEnabled(true);
                    explorerBtn.setEnabled(true);
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                int index = table.getSelectionIndex();
                TableItem[] items = table.getSelection();
                boolean chked = viewer.getChecked(items[0].getText());
                PathDialog pathDialog = new PathDialog(getShell(), items[0].getText());
                int result = pathDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                String path = pathDialog.getDirPath();
                if (dirList.contains(path)) {
                    MessageDialog.openError(composite.getShell(), "定義基点ディレクトリ", "すでに設定されているパスです。");
                    return;
                }
                dirList.remove(index);
                dirList.add(index, path);
                viewer.refresh();
                viewer.setChecked(path, chked);
            }
        });

        Button restartBtn = new Button(composite, SWT.NULL);
        GridData restartBtnGrDt = new GridData();
        restartBtnGrDt.horizontalSpan = 3;
        restartBtnGrDt.horizontalAlignment = GridData.BEGINNING;
        restartBtn.setLayoutData(restartBtnGrDt);
        restartBtn.setText("再起動");
        restartBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent event) {
                if (!performOk()) {
                    return;
                }
                Logger logger = Logger.getLogger("conntool");
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                File currentExecuteFile;
                try {
                    currentExecuteFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    logger.info(String.format("Restart: %s", currentExecuteFile));
                    if (currentExecuteFile.getName().endsWith(".jar")) {
                        ArrayList<String> command = new ArrayList<String>();
                        command.add(javaBin);
                        command.add("-jar");
                        command.add(currentExecuteFile.getPath());
                        ProcessBuilder builder = new ProcessBuilder(command);
                        builder.start();
                        getShell().getParent().getShell().close();
                    } else if (currentExecuteFile.getName().endsWith(".exe")) {
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec(new String[] { currentExecuteFile.toString() });
                        getShell().getParent().getShell().close();
                    } else {
                        MessageDialog.openWarning(composite.getShell(), "再起動", "jarまたはexe形式から起動された時だけ、このボタンからの再起動が可能です。");
                    }
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    logger.error(trace);
                    MessageDialog.openError(composite.getShell(), "再起動", "再起動に失敗しました。手で再起動してください。");
                }
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
        Object[] elements = viewer.getCheckedElements();
        List<String> checkedList = new ArrayList<String>();
        for (Object element : elements) {
            checkedList.add(element.toString());
        }

        StringBuilder builder = new StringBuilder();
        for (String dir : this.dirList) {
            if (checkedList.contains(dir)) {
                dir = "<" + dir + ">";
            }
            builder.append(dir + ",");
        }
        if (builder.length() > 0) {
            ps.setValue(PreferenceConstants.TARGET_DIRS, builder.toString());
        }
        return true;
    }
}
