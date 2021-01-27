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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PathPreferencePage extends PreferencePage {

    private Text ttmacroTxt;
    private Text workDirTxt;
    private Text logDirTxt;
    private Text iniFileDirTxt;
    private Text ttlLogDirPathTxt;
    private Text ttlLogFileNameTxt;
    private Text ttlLogopenOptionTxt;
    private List<Text> textList;

    public PathPreferencePage() {
        super("パス設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        IPreferenceStore preferenceStore = getPreferenceStore();

        // ========== TeraTermマクロの場所 ========== //
        new Label(composite, SWT.LEFT).setText("ttpmacro.exeのパス：");
        ttmacroTxt = new Text(composite, SWT.BORDER);
        ttmacroTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ttmacroTxt.setText(preferenceStore.getString(PreferenceConstants.TTPMACRO_EXE));
        Button ttmacroBtn = new Button(composite, SWT.NULL);
        ttmacroBtn.setText("参照");
        ttmacroBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell());
                dialog.setText("TeraTermマクロ(ttpmacro.exe)を指定してください。");
                dialog.setFilterPath("C:\\Program Files (x86)");
                dialog.setFilterExtensions(new String[] { "*.exe" });
                String file = dialog.open();
                if (file != null) {
                    ttmacroTxt.setText(file);
                }
            }
        });
        new Label(composite, SWT.LEFT).setText("");
        Label ttmacroDesc = new Label(composite, SWT.LEFT);
        GridData ttmacroDescGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttmacroDescGrDt.horizontalSpan = 2;
        ttmacroDesc.setLayoutData(ttmacroDescGrDt);
        ttmacroDesc.setText("- Tera Term 4.58以上をサポートします。\r\n- ttermpro.exe　ではなく ttpmacro.exe を指定してください。");

        Group dirGrp = new Group(composite, SWT.NONE);
        dirGrp.setLayout(new GridLayout(3, false));
        GridData dirGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dirGrpGrDt.horizontalSpan = 3;
        dirGrp.setLayoutData(dirGrpGrDt);
        dirGrp.setText("基点ディレクトリ");

        this.textList = new ArrayList<Text>();

        // ========== ワークディレクトリの場所 ========== //
        new Label(dirGrp, SWT.LEFT).setText("ワークディレクトリ：");
        workDirTxt = new Text(dirGrp, SWT.BORDER);
        workDirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        workDirTxt.setText(preferenceStore.getString(PreferenceConstants.WORK_DIR));
        this.textList.add(workDirTxt);
        Button workDirBtn = new Button(dirGrp, SWT.NULL);
        workDirBtn.setText("参照");
        workDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                String dir = dirDialogOpen("ワークディレクトリを指定してください。", workDirTxt.getText());
                if (dir != null) {
                    workDirTxt.setText(dir);
                }
            }
        });

        // ========== ログディレクトリの場所 ========== //
        new Label(dirGrp, SWT.LEFT).setText("ログディレクトリ：");
        logDirTxt = new Text(dirGrp, SWT.BORDER);
        logDirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        logDirTxt.setText(preferenceStore.getString(PreferenceConstants.LOG_DIR));
        this.textList.add(logDirTxt);
        Button logDirBtn = new Button(dirGrp, SWT.NULL);
        logDirBtn.setText("参照");
        logDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                String dir = dirDialogOpen("ログディレクトリ(Local)を指定してください。", logDirTxt.getText());
                if (dir != null) {
                    logDirTxt.setText(dir);
                }
            }
        });

        // ========== INIファイルディレクトリの場所 ========== //
        new Label(dirGrp, SWT.LEFT).setText("INIファイルディレクトリ：");
        iniFileDirTxt = new Text(dirGrp, SWT.BORDER);
        iniFileDirTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        iniFileDirTxt.setText(preferenceStore.getString(PreferenceConstants.INIFILE_DIR));
        this.textList.add(iniFileDirTxt);
        Button iniDirBtn = new Button(dirGrp, SWT.NULL);
        iniDirBtn.setText("参照");
        iniDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                String dir = dirDialogOpen("INIファイルが置かれているディレクトリを指定してください。", iniFileDirTxt.getText());
                if (dir != null) {
                    iniFileDirTxt.setText(dir);
                }
            }
        });

        Button mkDirBtn = new Button(dirGrp, SWT.NULL);
        GridData mkDirBtnGrDt = new GridData();
        mkDirBtnGrDt.horizontalSpan = 3;
        mkDirBtnGrDt.horizontalAlignment = SWT.RIGHT;
        mkDirBtn.setLayoutData(mkDirBtnGrDt);
        mkDirBtn.setText("設定にあわせて基点ディレクトリを作成する");
        mkDirBtn.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // TODO 既にディレクトリが存在する場合は警告を出すなどの細かい対応も追加したい。
                // 今はとりあえず作るだけです。
                int mkNum = 0;
                for (Text text : textList) {
                    if (!text.getText().isEmpty()) {
                        File dir = new File(text.getText());
                        dir.mkdirs();
                        mkNum++;
                    }
                }
                if (mkNum > 0) {
                    MessageDialog.openInformation(composite.getShell(), "ディレクトリ作成", "ディレクトリを作成しました。");
                } else {
                    MessageDialog.openWarning(composite.getShell(), "ディレクトリ作成", "ディレクトリ作成対象が設定されていません。");
                }
            }
        });

        Group logOpenGrp = new Group(composite, SWT.NONE);
        logOpenGrp.setLayout(new GridLayout(3, false));
        GridData logOpenGrDt = new GridData(GridData.FILL_HORIZONTAL);
        logOpenGrDt.horizontalSpan = 3;
        logOpenGrp.setLayoutData(logOpenGrDt);
        logOpenGrp.setText("ログ出力設定");

        // パスに使用できる変数に関するToolTip
        StringBuilder varTipBuffer = new StringBuilder();
        varTipBuffer.append("以下の変数が使用できます。\r\n");
        varTipBuffer.append("環境変数： COMPUTERNAME, USERNAME, USERDOMAIN\r\n");
        varTipBuffer.append("日時：yyyy, MM, dd, yyyyMM, yyyyMMdd, HHmm, HHmmss, yyyyMMdd-HHmmss\r\n");
        varTipBuffer.append("その他：authuser, loginuser, tab, category, group, server");

        // ========== Teratermのログディレクトリ階層 ========== //
        new Label(logOpenGrp, SWT.LEFT).setText("ログディレクトリ階層：");
        new Label(logOpenGrp, SWT.LEFT).setText("C\\library\\log\\");
        ttlLogDirPathTxt = new Text(logOpenGrp, SWT.BORDER);
        ttlLogDirPathTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ttlLogDirPathTxt.setText(preferenceStore.getString(PreferenceConstants.LOGDIR_PATH));
        ttlLogDirPathTxt.setMessage("${yyyyMM}\\${yyyyMMdd}（省略時）");
        ttlLogDirPathTxt.setToolTipText(varTipBuffer.toString());

        // ========== Teratermのログファイル名 ========== //
        new Label(logOpenGrp, SWT.LEFT).setText("ログファイル名：");
        ttlLogFileNameTxt = new Text(logOpenGrp, SWT.BORDER);
        GridData ttlLogFileNameTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlLogFileNameTxtGrDt.horizontalSpan = 2;
        ttlLogFileNameTxt.setLayoutData(ttlLogFileNameTxtGrDt);
        ttlLogFileNameTxt.setText(preferenceStore.getString(PreferenceConstants.LOGFILE_NAME));
        ttlLogFileNameTxt.setMessage("${yyyyMMdd}-${HHmmss}_${group}_${server}_${COMPUTERNAME}.log（省略時）");
        ttlLogFileNameTxt.setToolTipText(varTipBuffer.toString());

        // ========== Teratermのlogopenオプション ========== //
        new Label(logOpenGrp, SWT.LEFT).setText("logopenのオプション：");
        ttlLogopenOptionTxt = new Text(logOpenGrp, SWT.BORDER);
        GridData ttlLogopenOptionTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlLogopenOptionTxtGrDt.horizontalSpan = 2;
        ttlLogopenOptionTxt.setLayoutData(ttlLogopenOptionTxtGrDt);
        ttlLogopenOptionTxt.setText(preferenceStore.getString(PreferenceConstants.LOGOPEN_OPTION));
        ttlLogopenOptionTxt.setMessage("省略時は \"0 0 0 0 1\" となります。");
        new Label(logOpenGrp, SWT.LEFT).setText("");
        Label ttlLogopenOptionDescLbl = new Label(logOpenGrp, SWT.LEFT);
        GridData ttlLogopenOptionDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        ttlLogopenOptionDescLblGrDt.horizontalSpan = 2;
        ttlLogopenOptionDescLbl.setLayoutData(ttlLogopenOptionDescLblGrDt);
        StringBuilder builder2 = new StringBuilder();
        builder2.append("logopen <filename> 0 0 0 0 1 のfilenameの後ろのオプションを指定できます。\r\n");
        builder2.append("例) \"0 0\", \"0 0 0 1 1\"など。省略した場合は0 0 0 0 1になります。\r\n");
        builder2.append("詳細はTera Termのマニュアルを見てください。");
        ttlLogopenOptionDescLbl.setText(builder2.toString());

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        if (this.ttmacroTxt != null) {
            ps.setValue(PreferenceConstants.TTPMACRO_EXE, this.ttmacroTxt.getText());
        }
        if (this.workDirTxt != null) {
            ps.setValue(PreferenceConstants.WORK_DIR, this.workDirTxt.getText());
        }
        if (this.logDirTxt != null) {
            ps.setValue(PreferenceConstants.LOG_DIR, this.logDirTxt.getText());
        }
        if (this.iniFileDirTxt != null) {
            ps.setValue(PreferenceConstants.INIFILE_DIR, this.iniFileDirTxt.getText());
        }
        if (this.ttlLogDirPathTxt != null) {
            ps.setValue(PreferenceConstants.LOGDIR_PATH, this.ttlLogDirPathTxt.getText());
        }
        if (this.ttlLogFileNameTxt != null) {
            ps.setValue(PreferenceConstants.LOGFILE_NAME, this.ttlLogFileNameTxt.getText());
        }
        if (this.ttlLogopenOptionTxt != null) {
            ps.setValue(PreferenceConstants.LOGOPEN_OPTION, this.ttlLogopenOptionTxt.getText());
        }
        return true;
    }

    private String dirDialogOpen(String msg, String currentPath) {
        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setText(msg);
        dialog.setFilterPath(currentPath.isEmpty() ? "C:\\" : currentPath);
        String dir = dialog.open();
        return dir;
    }
}
