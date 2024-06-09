package com.contrastsecurity.csvdltool.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.ScanProjectGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.ScanProjectInfo;
import com.contrastsecurity.csvdltool.ScanProjectsGetWithProgress;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class ScanTabItem extends CTabItem implements PropertyChangeListener {

    private Button loadBtn;
    private Button includeArchivedProjectChk;
    private Text srcListFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private Map<String, ScanProjectInfo> fullMap;
    private List<String> srcProjects = new ArrayList<String>();
    private List<String> dstProjects = new ArrayList<String>();

    private PreferenceStore ps;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ScanTabItem(CTabFolder mainTabFolder, CSVDLToolShell toolShell, PreferenceStore ps) {
        super(mainTabFolder, SWT.NONE);
        this.ps = ps;
        Font bigFont = new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL);
        setText("SCAN");
        setImage(new Image(toolShell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-scan-sast-02.png"))); //$NON-NLS-1$

        Composite shell = new Composite(mainTabFolder, SWT.NONE);
        shell.setLayout(new GridLayout(1, false));

        Group projectListGrp = new Group(shell, SWT.NONE);
        GridLayout projectListGrpLt = new GridLayout(3, false);
        projectListGrpLt.marginHeight = 0;
        projectListGrpLt.verticalSpacing = 0;
        projectListGrp.setLayout(projectListGrpLt);
        GridData projectListGrpGrDt = new GridData(GridData.FILL_BOTH);
        projectListGrpGrDt.minimumHeight = 200;
        projectListGrp.setLayoutData(projectListGrpGrDt);
        // appListGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        Composite attackTermGrp = new Composite(projectListGrp, SWT.NONE);
        attackTermGrp.setLayout(new GridLayout(1, false));
        GridData attackTermGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackTermGrpGrDt.horizontalSpan = 3;
        attackTermGrp.setLayoutData(attackTermGrpGrDt);

        this.includeArchivedProjectChk = new Button(attackTermGrp, SWT.CHECK);
        this.includeArchivedProjectChk.setText("アーカイブ済プロジェクトも対象とする。");
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_ARCHIVED_PROJ)) {
            this.includeArchivedProjectChk.setSelection(true);
        }

        loadBtn = new Button(projectListGrp, SWT.PUSH);
        GridData projectLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        projectLoadBtnGrDt.horizontalSpan = 3;
        loadBtn.setLayoutData(projectLoadBtnGrDt);
        loadBtn.setText("プロジェクト一覧の読み込み");
        loadBtn.setToolTipText("TeamServerにオンボードされているSCANプロジェクトを読み込みます。");
        loadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                uiReset();
                ScanProjectsGetWithProgress progress = new ScanProjectsGetWithProgress(toolShell, ps, toolShell.getMain().getValidOrganizations(),
                        includeArchivedProjectChk.getSelection());
                ProgressMonitorDialog progDialog = new ScanProjectGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullMap = progress.getFullProjectMap();
                if (fullMap.isEmpty()) {
                }
                for (String projLabel : fullMap.keySet()) {
                    srcList.add(projLabel); // UI list
                    srcProjects.add(projLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
            }
        });

        Composite srcGrp = new Composite(projectListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(1, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage("Filter...");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });

        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.srcList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.srcList.setToolTipText("選択可能なプロジェクト一覧");
        this.srcList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = srcList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                dstList.add(srcProjects.get(idx));
                dstProjects.add(srcProjects.get(idx));
                srcList.remove(idx);
                srcProjects.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Composite srcListLblComp = new Composite(srcGrp, SWT.NONE);
        GridLayout srcListLblLt = new GridLayout(2, false);
        srcListLblLt.marginHeight = 0;
        srcListLblLt.marginWidth = 0;
        srcListLblLt.marginLeft = 5;
        srcListLblLt.marginBottom = 0;
        srcListLblComp.setLayout(srcListLblLt);
        GridData srcListLblCompGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcListLblCompGrDt.horizontalSpan = 2;
        srcListLblComp.setLayoutData(srcListLblCompGrDt);
        Label srcListDescLbl = new Label(srcListLblComp, SWT.LEFT);
        GridData srcListDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcListDescLblGrDt.minimumHeight = 12;
        srcListDescLbl.setLayoutData(srcListDescLblGrDt);
        srcListDescLbl.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        srcListDescLbl.setText("選択可能なプロジェクト一覧");
        srcListDescLbl.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.srcCount = new Label(srcListLblComp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 12;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.srcCount.setText("0"); //$NON-NLS-1$
        this.srcCount.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        Composite btnGrp = new Composite(projectListGrp, SWT.NONE);
        btnGrp.setLayout(new GridLayout(1, false));
        GridData btnGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        btnGrpGrDt.verticalAlignment = SWT.CENTER;
        btnGrp.setLayoutData(btnGrpGrDt);

        Button allRightBtn = new Button(btnGrp, SWT.PUSH);
        allRightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allRightBtn.setText(">>"); //$NON-NLS-1$
        allRightBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : srcProjects) {
                    dstList.add(appName);
                    dstProjects.add(appName);
                }
                srcList.removeAll();
                srcProjects.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Button rightBtn = new Button(btnGrp, SWT.PUSH);
        rightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightBtn.setText(">"); //$NON-NLS-1$
        rightBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : srcList.getSelectionIndices()) {
                    String appName = srcProjects.get(idx);
                    String keyword = dstListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        dstList.add(appName);
                        dstProjects.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(srcList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    srcList.remove(idx.intValue());
                    srcProjects.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Button leftBtn = new Button(btnGrp, SWT.PUSH);
        leftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftBtn.setText("<"); //$NON-NLS-1$
        leftBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : dstList.getSelectionIndices()) {
                    String appName = dstProjects.get(idx);
                    String keyword = srcListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        srcList.add(appName);
                        srcProjects.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(dstList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    dstList.remove(idx.intValue());
                    dstProjects.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Button allLeftBtn = new Button(btnGrp, SWT.PUSH);
        allLeftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allLeftBtn.setText("<<"); //$NON-NLS-1$
        allLeftBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : dstProjects) {
                    srcList.add(appName);
                    srcProjects.add(appName);
                }
                dstList.removeAll();
                dstProjects.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Composite dstGrp = new Composite(projectListGrp, SWT.NONE);
        dstGrp.setLayout(new GridLayout(1, false));
        GridData dstGrpGrDt = new GridData(GridData.FILL_BOTH);
        dstGrp.setLayoutData(dstGrpGrDt);

        dstListFilter = new Text(dstGrp, SWT.BORDER);
        dstListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dstListFilter.setMessage("Filter...");
        dstListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                dstList.removeAll(); // UI List dst
                dstProjects.clear(); // memory dst
                if (fullMap == null) {
                    dstCount.setText(String.valueOf(dstList.getItemCount()));
                    return;
                }
                String keyword = dstListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appName : fullMap.keySet()) {
                        if (srcProjects.contains(appName)) {
                            continue; // 選択可能にあるアプリはスキップ
                        }
                        dstList.add(appName); // UI List dst
                        dstProjects.add(appName); // memory dst
                    }
                } else {
                    for (String appName : fullMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcProjects.contains(appName)) {
                                continue; // 選択可能にあるアプリはスキップ
                            }
                            dstList.add(appName); // UI List dst
                            dstProjects.add(appName); // memory dst
                        }
                    }
                }
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.dstList = new org.eclipse.swt.widgets.List(dstGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.dstList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.dstList.setToolTipText("選択済みのプロジェクト一覧");
        this.dstList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = dstList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                srcList.add(dstProjects.get(idx));
                srcProjects.add(dstProjects.get(idx));
                dstList.remove(idx);
                dstProjects.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Composite dstListLblComp = new Composite(dstGrp, SWT.NONE);
        GridLayout dstListLblLt = new GridLayout(2, false);
        dstListLblLt.marginHeight = 0;
        dstListLblLt.marginWidth = 0;
        dstListLblLt.marginLeft = 5;
        dstListLblLt.marginBottom = 0;
        dstListLblComp.setLayout(dstListLblLt);
        dstListLblComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label dstListDescLbl = new Label(dstListLblComp, SWT.LEFT);
        GridData dstListDescLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstListDescLblGrDt.minimumHeight = 12;
        dstListDescLbl.setLayoutData(dstListDescLblGrDt);
        dstListDescLbl.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        dstListDescLbl.setText("選択済みのプロジェクト一覧");
        dstListDescLbl.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.dstCount = new Label(dstListLblComp, SWT.RIGHT);
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.minimumHeight = 12;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.dstCount.setText("0"); //$NON-NLS-1$
        this.dstCount.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        setControl(shell);
    }

    private void uiReset() {
        // src
        srcListFilter.setText(""); //$NON-NLS-1$
        srcList.removeAll();
        srcProjects.clear();
        // dst
        dstListFilter.setText(""); //$NON-NLS-1$
        dstList.removeAll();
        dstProjects.clear();
        // full
        if (fullMap != null) {
            fullMap.clear();
        }
    }

    private void srcListFilterUpdate() {
        srcList.removeAll(); // UI List src
        srcProjects.clear(); // memory src
        if (fullMap == null) {
            srcCount.setText(String.valueOf(srcList.getItemCount()));
            return;
        }
        String keyword = srcListFilter.getText().trim();
        if (keyword.isEmpty()) {
            for (String appLabel : fullMap.keySet()) {
                if (dstProjects.contains(appLabel)) {
                    continue; // 既に選択済みのアプリはスキップ
                }
                srcList.add(appLabel); // UI List src
                srcProjects.add(appLabel); // memory src
            }
        } else {
            for (String appLabel : fullMap.keySet()) {
                boolean isKeywordValid = true;
                if (!keyword.isEmpty()) {
                    if (!appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                        if (dstProjects.contains(appLabel)) {
                            continue; // 既に選択済みのアプリはスキップ
                        }
                        isKeywordValid = false;
                    }
                }
                if (isKeywordValid) {
                    srcList.add(appLabel);
                    srcProjects.add(appLabel);
                }
            }
        }
        srcCount.setText(String.valueOf(srcList.getItemCount()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("shellActivated".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("shellClosed".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("tabSelected".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("buttonEnabled".equals(event.getPropertyName())) { //$NON-NLS-1$
            loadBtn.setEnabled((Boolean) event.getNewValue());
        } else if ("validOrgChanged".equals(event.getPropertyName())) { //$NON-NLS-1$
            uiReset();
        }
    }

}
