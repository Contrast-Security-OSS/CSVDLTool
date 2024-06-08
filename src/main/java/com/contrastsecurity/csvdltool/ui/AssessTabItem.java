package com.contrastsecurity.csvdltool.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.AppGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.AppInfo;
import com.contrastsecurity.csvdltool.AppsGetWithProgress;
import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.FilterEnum;
import com.contrastsecurity.csvdltool.FilterLastDetectedDialog;
import com.contrastsecurity.csvdltool.FilterSeverityDialog;
import com.contrastsecurity.csvdltool.FilterVulnTypeDialog;
import com.contrastsecurity.csvdltool.LibGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.LibGetWithProgress;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.VulGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.VulGetWithProgress;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class AssessTabItem extends CTabItem implements PropertyChangeListener {

    private Button loadBtn;
    private Text srcListFilter;
    private Text srcListLanguagesFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private CTabFolder subTabFolder;

    private Button vulExecuteBtn;
    private Button vulOnlyParentAppChk;
    private Button vulOnlyCurVulExpChk;
    private Button includeDescChk;
    private Button includeStackTraceChk;

    private Button libExecuteBtn;
    private Button onlyHasCVEChk;
    private Button withCVSSInfoChk;
    private Button withEPSSInfoChk;
    private Button includeCVEDetailChk;

    private Text vulSeverityFilterTxt;
    private Text vulVulnTypeFilterTxt;
    private Text vulLastDetectedFilterTxt;

    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> assessFilterMap;
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;

    private PreferenceStore ps;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)"); //$NON-NLS-1$

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public AssessTabItem(CTabFolder mainTabFolder, CSVDLToolShell toolShell, PreferenceStore ps) {
        super(mainTabFolder, SWT.NONE);
        this.ps = ps;
        Font bigFont = new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL);
        setText(Messages.getString("main.tab.assess.title")); //$NON-NLS-1$
        setImage(new Image(toolShell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-assess-iast-02.png"))); //$NON-NLS-1$

        Composite shell = new Composite(mainTabFolder, SWT.NONE);
        shell.setLayout(new GridLayout(1, false));

        Group appListGrp = new Group(shell, SWT.NONE);
        GridLayout appListGrpLt = new GridLayout(3, false);
        appListGrpLt.marginHeight = 0;
        appListGrpLt.verticalSpacing = 0;
        appListGrp.setLayout(appListGrpLt);
        GridData appListGrpGrDt = new GridData(GridData.FILL_BOTH);
        appListGrpGrDt.minimumHeight = 200;
        appListGrp.setLayoutData(appListGrpGrDt);
        // appListGrp.setBackground(display.getSystemColor(SWT.COLOR_RED));

        loadBtn = new Button(appListGrp, SWT.PUSH);
        GridData loadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        loadBtnGrDt.horizontalSpan = 3;
        loadBtn.setLayoutData(loadBtnGrDt);
        loadBtn.setText(Messages.getString("main.application.load.button.title")); //$NON-NLS-1$
        loadBtn.setToolTipText(Messages.getString("main.application.load.button.tooltip")); //$NON-NLS-1$
        loadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                uiReset();
                AppsGetWithProgress progress = new AppsGetWithProgress(toolShell, ps, toolShell.getMain().getValidOrganizations());
                ProgressMonitorDialog progDialog = new AppGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(toolShell, Messages.getString("main.application.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.application.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullAppMap = progress.getFullAppMap();
                if (fullAppMap.isEmpty()) {
                    String userName = ps.getString(PreferenceConstants.USERNAME);
                    StringJoiner sj = new StringJoiner("\r\n"); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.1")); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.2")); //$NON-NLS-1$
                    sj.add(String.format("　%s", userName)); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.3")); //$NON-NLS-1$
                    sj.add(Messages.getString("main.application.load.empty.list.warning.message.4")); //$NON-NLS-1$
                    MessageDialog.openInformation(toolShell, Messages.getString("main.application.load.message.dialog.title"), sj.toString()); //$NON-NLS-1$
                }
                for (String appLabel : fullAppMap.keySet()) {
                    srcList.add(appLabel); // UI list
                    srcApps.add(appLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                assessFilterMap = progress.getFilterMap();
                vulSeverityFilterTxt.setText(Messages.getString("main.vul.filter.condition.severity.all")); //$NON-NLS-1$
                vulVulnTypeFilterTxt.setText(Messages.getString("main.vul.filter.condition.vulntype.all")); //$NON-NLS-1$
            }
        });

        Composite srcGrp = new Composite(appListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(2, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage(Messages.getString("main.src.apps.filter.name.message")); //$NON-NLS-1$
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });

        srcListLanguagesFilter = new Text(srcGrp, SWT.BORDER);
        GridData srcListLanguagesFilterGrDt = new GridData();
        srcListLanguagesFilterGrDt.minimumWidth = 40;
        srcListLanguagesFilter.setLayoutData(srcListLanguagesFilterGrDt);
        srcListLanguagesFilter.setMessage(Messages.getString("main.src.apps.filter.language.message")); //$NON-NLS-1$
        srcListLanguagesFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcListFilterUpdate();
            }
        });

        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData srcListGrDt = new GridData(GridData.FILL_BOTH);
        srcListGrDt.horizontalSpan = 2;
        this.srcList.setLayoutData(srcListGrDt);
        this.srcList.setToolTipText(Messages.getString("main.available.app.list.tooltip")); //$NON-NLS-1$
        this.srcList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = srcList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                dstList.add(srcApps.get(idx));
                dstApps.add(srcApps.get(idx));
                srcList.remove(idx);
                srcApps.remove(idx);
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
        srcListDescLbl.setText(Messages.getString("main.available.app.list.count.label")); //$NON-NLS-1$
        srcListDescLbl.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.srcCount = new Label(srcListLblComp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 12;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.srcCount.setText("0"); //$NON-NLS-1$
        this.srcCount.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        Composite btnGrp = new Composite(appListGrp, SWT.NONE);
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
                for (String appName : srcApps) {
                    dstList.add(appName);
                    dstApps.add(appName);
                }
                srcList.removeAll();
                srcApps.clear();
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
                    String appName = srcApps.get(idx);
                    String keyword = dstListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        dstList.add(appName);
                        dstApps.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(srcList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    srcList.remove(idx.intValue());
                    srcApps.remove(idx.intValue());
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
                    String appName = dstApps.get(idx);
                    String keyword = srcListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        srcList.add(appName);
                        srcApps.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(dstList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    dstList.remove(idx.intValue());
                    dstApps.remove(idx.intValue());
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
                for (String appName : dstApps) {
                    srcList.add(appName);
                    srcApps.add(appName);
                }
                dstList.removeAll();
                dstApps.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        Composite dstGrp = new Composite(appListGrp, SWT.NONE);
        dstGrp.setLayout(new GridLayout(1, false));
        GridData dstGrpGrDt = new GridData(GridData.FILL_BOTH);
        dstGrp.setLayoutData(dstGrpGrDt);

        dstListFilter = new Text(dstGrp, SWT.BORDER);
        dstListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dstListFilter.setMessage(Messages.getString("main.dst.apps.filter.name.message")); //$NON-NLS-1$
        dstListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                dstList.removeAll(); // UI List dst
                dstApps.clear(); // memory dst
                if (fullAppMap == null) {
                    dstCount.setText(String.valueOf(dstList.getItemCount()));
                    return;
                }
                String keyword = dstListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appName : fullAppMap.keySet()) {
                        if (srcApps.contains(appName)) {
                            continue; // 選択可能にあるアプリはスキップ
                        }
                        dstList.add(appName); // UI List dst
                        dstApps.add(appName); // memory dst
                    }
                } else {
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcApps.contains(appName)) {
                                continue; // 選択可能にあるアプリはスキップ
                            }
                            dstList.add(appName); // UI List dst
                            dstApps.add(appName); // memory dst
                        }
                    }
                }
                dstCount.setText(String.valueOf(dstList.getItemCount()));
            }
        });

        this.dstList = new org.eclipse.swt.widgets.List(dstGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.dstList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.dstList.setToolTipText(Messages.getString("main.selected.app.list.tooltip")); //$NON-NLS-1$
        this.dstList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = dstList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                srcList.add(dstApps.get(idx));
                srcApps.add(dstApps.get(idx));
                dstList.remove(idx);
                dstApps.remove(idx);
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
        dstListDescLbl.setText(Messages.getString("main.selected.app.list.count.label")); //$NON-NLS-1$
        dstListDescLbl.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        this.dstCount = new Label(dstListLblComp, SWT.RIGHT);
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.minimumHeight = 12;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setFont(new Font(toolShell.getDisplay(), "Arial", 8, SWT.NORMAL)); //$NON-NLS-1$
        this.dstCount.setText("0"); //$NON-NLS-1$
        this.dstCount.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        subTabFolder = new CTabFolder(shell, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_HORIZONTAL);
        subTabFolder.setLayoutData(tabFolderGrDt);
        subTabFolder.setSelectionBackground(
                new Color[] { toolShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), toolShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        // #################### 脆弱性 #################### //
        CTabItem vulTabItem = new CTabItem(subTabFolder, SWT.NONE);
        vulTabItem.setText(Messages.getString("main.vul.tab.title")); //$NON-NLS-1$

        // ========== グループ ==========
        Composite vulButtonGrp = new Composite(subTabFolder, SWT.NULL);
        GridLayout buttonGrpLt = new GridLayout(1, false);
        buttonGrpLt.marginWidth = 10;
        buttonGrpLt.marginHeight = 10;
        vulButtonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // buttonGrpGrDt.horizontalSpan = 3;
        // buttonGrpGrDt.widthHint = 100;
        vulButtonGrp.setLayoutData(buttonGrpGrDt);

        Group vulFilterGrp = new Group(vulButtonGrp, SWT.NONE);
        GridLayout vulFilterGrpLt = new GridLayout(2, false);
        vulFilterGrpLt.marginWidth = 10;
        vulFilterGrpLt.marginHeight = 10;
        // vulFilterGrpLt.horizontalSpacing = 10;
        // vulFilterGrpLt.verticalSpacing = 10;
        vulFilterGrp.setLayout(vulFilterGrpLt);
        GridData vulFilterGrpGrDt = new GridData(GridData.FILL_BOTH);
        vulFilterGrp.setLayoutData(vulFilterGrpGrDt);
        vulFilterGrp.setText(Messages.getString("main.vul.filter.condition.group.title")); //$NON-NLS-1$

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.severity.label")); //$NON-NLS-1$
        vulSeverityFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulSeverityFilterTxt.setText(Messages.getString("main.load.application.message")); //$NON-NLS-1$
        vulSeverityFilterTxt.setEditable(false);
        vulSeverityFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSeverityFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (assessFilterMap != null && assessFilterMap.containsKey(FilterEnum.SEVERITY)) {
                    FilterSeverityDialog filterDialog = new FilterSeverityDialog(toolShell, assessFilterMap.get(FilterEnum.SEVERITY));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        vulExecuteBtn.setFocus();
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : assessFilterMap.get(FilterEnum.SEVERITY)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulSeverityFilterTxt.setText(Messages.getString("main.vul.filter.condition.severity.all")); //$NON-NLS-1$
                    } else {
                        vulSeverityFilterTxt.setText(String.join(", ", labels)); //$NON-NLS-1$
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.vulntype.label")); //$NON-NLS-1$
        vulVulnTypeFilterTxt = new Text(vulFilterGrp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        vulVulnTypeFilterTxt.setText(Messages.getString("main.load.application.message")); //$NON-NLS-1$
        vulVulnTypeFilterTxt.setEditable(false);
        GridData vulVulnTypeFilterTxtGrDt = new GridData(GridData.FILL_HORIZONTAL);
        vulVulnTypeFilterTxtGrDt.minimumHeight = 2 * vulVulnTypeFilterTxt.getLineHeight();
        vulVulnTypeFilterTxt.setLayoutData(vulVulnTypeFilterTxtGrDt);
        vulVulnTypeFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (assessFilterMap != null && assessFilterMap.containsKey(FilterEnum.VULNTYPE)) {
                    FilterVulnTypeDialog filterDialog = new FilterVulnTypeDialog(toolShell, assessFilterMap.get(FilterEnum.VULNTYPE));
                    int result = filterDialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        vulExecuteBtn.setFocus();
                        return;
                    }
                    List<String> labels = filterDialog.getLabels();
                    for (Filter filter : assessFilterMap.get(FilterEnum.VULNTYPE)) {
                        if (labels.contains(filter.getLabel())) {
                            filter.setValid(true);
                        } else {
                            filter.setValid(false);
                        }
                    }
                    if (labels.isEmpty()) {
                        vulVulnTypeFilterTxt.setText(Messages.getString("main.vul.filter.condition.vulntype.all")); //$NON-NLS-1$
                    } else {
                        vulVulnTypeFilterTxt.setText(String.join(", ", labels)); //$NON-NLS-1$
                    }
                    vulExecuteBtn.setFocus();
                }
            }
        });

        new Label(vulFilterGrp, SWT.LEFT).setText(Messages.getString("main.vul.filter.condition.lastdetected.label")); //$NON-NLS-1$
        vulLastDetectedFilterTxt = new Text(vulFilterGrp, SWT.BORDER);
        vulLastDetectedFilterTxt.setText(Messages.getString("main.vul.filter.condition.lastdetected.all")); //$NON-NLS-1$
        vulLastDetectedFilterTxt.setEditable(false);
        vulLastDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulLastDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(toolShell, frLastDetectedDate, toLastDetectedDate);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    vulExecuteBtn.setFocus();
                    return;
                }
                frLastDetectedDate = filterDialog.getFrDate();
                toLastDetectedDate = filterDialog.getToDate();
                if (frLastDetectedDate != null && toLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("%s ～ %s", sdf.format(frLastDetectedDate), sdf.format(toLastDetectedDate))); //$NON-NLS-1$
                } else if (frLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("%s ～", sdf.format(frLastDetectedDate))); //$NON-NLS-1$
                } else if (toLastDetectedDate != null) {
                    vulLastDetectedFilterTxt.setText(String.format("～ %s", sdf.format(toLastDetectedDate))); //$NON-NLS-1$
                } else {
                    vulLastDetectedFilterTxt.setText(Messages.getString("main.vul.filter.condition.lastdetected.all")); //$NON-NLS-1$
                }
                vulExecuteBtn.setFocus();
            }
        });

        // ========== 取得ボタン ==========
        vulExecuteBtn = new Button(vulButtonGrp, SWT.PUSH);
        GC gc = new GC(vulExecuteBtn);
        gc.setFont(bigFont);
        Point bigBtnSize = gc.textExtent(Messages.getString("main.vul.export.button.title"));
        gc.dispose();
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.minimumHeight = 50;
        executeBtnGrDt.heightHint = bigBtnSize.y + 20;
        vulExecuteBtn.setLayoutData(executeBtnGrDt);
        vulExecuteBtn.setText(Messages.getString("main.vul.export.button.title")); //$NON-NLS-1$
        vulExecuteBtn.setToolTipText(Messages.getString("main.vul.export.button.tooltip")); //$NON-NLS-1$
        vulExecuteBtn.setFont(new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        vulExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(toolShell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.export.application.unselected.error.message")); //$NON-NLS-1$
                    return;
                }
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = toolShell.getMain().getOutDirPath();
                }
                if (outDirPath == null || outDirPath.isEmpty()) {
                    return;
                }
                VulGetWithProgress progress = new VulGetWithProgress(toolShell, ps, outDirPath, dstApps, fullAppMap, assessFilterMap, frLastDetectedDate, toLastDetectedDate,
                        vulOnlyParentAppChk.getSelection(), vulOnlyCurVulExpChk.getSelection(), includeDescChk.getSelection(), includeStackTraceChk.getSelection());
                ProgressMonitorDialog progDialog = new VulGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), exceptionMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(toolShell, trace, exceptionMsg);
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.vul.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.vul.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(toolShell, Messages.getString("main.application.load.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.vul.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        vulOnlyParentAppChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyParentAppChk.setText(Messages.getString("main.vul.export.option.only.parent.application")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.VUL_ONLY_PARENT_APP)) {
            vulOnlyParentAppChk.setSelection(true);
        }

        vulOnlyCurVulExpChk = new Button(vulButtonGrp, SWT.CHECK);
        vulOnlyCurVulExpChk.setText(Messages.getString("main.vul.export.option.only.latest")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.VUL_ONLY_CURVUL_EXP)) {
            vulOnlyCurVulExpChk.setSelection(true);
        }

        includeDescChk = new Button(vulButtonGrp, SWT.CHECK);
        includeDescChk.setText(Messages.getString("main.vul.export.option.include.detail")); //$NON-NLS-1$
        includeDescChk.setToolTipText(Messages.getString("main.vul.export.option.include.detail.hint")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_DESCRIPTION)) {
            includeDescChk.setSelection(true);
        }
        includeDescChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button chk = (Button) e.getSource();
                if (!chk.getSelection()) {
                    includeStackTraceChk.setSelection(false);
                }
            }
        });

        includeStackTraceChk = new Button(vulButtonGrp, SWT.CHECK);
        includeStackTraceChk.setText(Messages.getString("main.vul.export.option.include.stacktrace")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_STACKTRACE)) {
            includeStackTraceChk.setSelection(true);
        }
        includeStackTraceChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button chk = (Button) e.getSource();
                if (chk.getSelection()) {
                    includeDescChk.setSelection(true);
                }
            }
        });
        vulTabItem.setControl(vulButtonGrp);

        // #################### ライブラリ #################### //
        CTabItem libTabItem = new CTabItem(subTabFolder, SWT.NONE);
        libTabItem.setText(Messages.getString("main.lib.tab.title")); //$NON-NLS-1$

        // ========== グループ ==========
        Composite libButtonGrp = new Composite(subTabFolder, SWT.NULL);
        GridLayout libButtonGrpLt = new GridLayout(1, false);
        libButtonGrpLt.marginWidth = 10;
        libButtonGrpLt.marginHeight = 10;
        libButtonGrp.setLayout(libButtonGrpLt);
        GridData libButtonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // libButtonGrpGrDt.horizontalSpan = 3;
        // libButtonGrpGrDt.widthHint = 100;
        libButtonGrp.setLayoutData(libButtonGrpGrDt);

        // ========== 取得ボタン ==========
        libExecuteBtn = new Button(libButtonGrp, SWT.PUSH);
        GridData libExecuteBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        libExecuteBtnGrDt.minimumHeight = 50;
        libExecuteBtnGrDt.heightHint = bigBtnSize.y + 20;
        libExecuteBtn.setLayoutData(libExecuteBtnGrDt);
        libExecuteBtn.setText(Messages.getString("main.lib.export.button.title")); //$NON-NLS-1$
        libExecuteBtn.setToolTipText(Messages.getString("main.lib.export.button.tooltip")); //$NON-NLS-1$
        libExecuteBtn.setFont(new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        libExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.isEmpty()) {
                    MessageDialog.openInformation(toolShell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.export.application.unselected.error.message")); //$NON-NLS-1$
                    return;
                }
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = toolShell.getMain().getOutDirPath();
                }
                if (outDirPath == null || outDirPath.isEmpty()) {
                    return;
                }
                LibGetWithProgress progress = new LibGetWithProgress(toolShell, ps, outDirPath, dstApps, fullAppMap, onlyHasCVEChk.getSelection(), withCVSSInfoChk.getSelection(),
                        withEPSSInfoChk.getSelection(), includeCVEDetailChk.getSelection());
                ProgressMonitorDialog progDialog = new LibGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    // if (!(e.getTargetException() instanceof TsvException)) {
                    // logger.error(trace);
                    // }
                    String exceptionMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), exceptionMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof InterruptedException) {
                        MessageDialog.openInformation(toolShell, trace, exceptionMsg);
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(toolShell, Messages.getString("main.lib.export.message.dialog.title"), exceptionMsg); //$NON-NLS-1$
                        return;
                    } else {
                        logger.error(trace);
                        MessageDialog.openError(toolShell, Messages.getString("main.lib.export.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), exceptionMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        onlyHasCVEChk = new Button(libButtonGrp, SWT.CHECK);
        onlyHasCVEChk.setText(Messages.getString("main.lib.export.option.only.has.cve")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            onlyHasCVEChk.setSelection(true);
        }
        withCVSSInfoChk = new Button(libButtonGrp, SWT.CHECK);
        withCVSSInfoChk.setText(Messages.getString("main.lib.export.option.with.cvss")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.WITH_CVSS)) {
            withCVSSInfoChk.setSelection(true);
        }
        withEPSSInfoChk = new Button(libButtonGrp, SWT.CHECK);
        withEPSSInfoChk.setText(Messages.getString("main.lib.export.option.with.epss")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.WITH_EPSS)) {
            withEPSSInfoChk.setSelection(true);
        }
        includeCVEDetailChk = new Button(libButtonGrp, SWT.CHECK);
        includeCVEDetailChk.setText(Messages.getString("main.lib.export.option.include.detail")); //$NON-NLS-1$
        includeCVEDetailChk.setToolTipText(Messages.getString("main.lib.export.option.include.detail.tooltip")); //$NON-NLS-1$
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_CVE_DETAIL)) {
            includeCVEDetailChk.setSelection(true);
        }
        libTabItem.setControl(libButtonGrp);

        int sub_idx = this.ps.getInt(PreferenceConstants.OPENED_SUB_TAB_IDX);
        subTabFolder.setSelection(sub_idx);

        setControl(shell);
    }

    private void uiReset() {
        // src
        srcListFilter.setText(""); //$NON-NLS-1$
        srcList.removeAll();
        srcApps.clear();
        // dst
        dstListFilter.setText(""); //$NON-NLS-1$
        dstList.removeAll();
        dstApps.clear();
        // full
        if (fullAppMap != null) {
            fullAppMap.clear();
        }
    }

    private void srcListFilterUpdate() {
        srcList.removeAll(); // UI List src
        srcApps.clear(); // memory src
        if (fullAppMap == null) {
            srcCount.setText(String.valueOf(srcList.getItemCount()));
            return;
        }
        String keyword = srcListFilter.getText().trim();
        String language = srcListLanguagesFilter.getText().trim();
        if (keyword.isEmpty() && language.isEmpty()) {
            for (String appLabel : fullAppMap.keySet()) {
                if (dstApps.contains(appLabel)) {
                    continue; // 既に選択済みのアプリはスキップ
                }
                srcList.add(appLabel); // UI List src
                srcApps.add(appLabel); // memory src
            }
        } else {
            for (String appLabel : fullAppMap.keySet()) {
                boolean isKeywordValid = true;
                if (!keyword.isEmpty()) {
                    if (!appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                        if (dstApps.contains(appLabel)) {
                            continue; // 既に選択済みのアプリはスキップ
                        }
                        isKeywordValid = false;
                    }
                }
                boolean isLanguageValid = true;
                if (!language.isEmpty()) {
                    AppInfo appInfo = fullAppMap.get(appLabel);
                    if (!appInfo.getLanguageLabel().toLowerCase().contains(language)) {
                        isLanguageValid = false;
                    }
                }
                if (isKeywordValid && isLanguageValid) {
                    srcList.add(appLabel);
                    srcApps.add(appLabel);
                }
            }
        }
        srcCount.setText(String.valueOf(srcList.getItemCount()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("shellActivated".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("shellClosed".equals(event.getPropertyName())) { //$NON-NLS-1$
            int sub_idx = subTabFolder.getSelectionIndex();
            this.ps.setValue(PreferenceConstants.OPENED_SUB_TAB_IDX, sub_idx);
            this.ps.setValue(PreferenceConstants.VUL_ONLY_PARENT_APP, vulOnlyParentAppChk.getSelection());
            this.ps.setValue(PreferenceConstants.VUL_ONLY_CURVUL_EXP, vulOnlyCurVulExpChk.getSelection());
            this.ps.setValue(PreferenceConstants.INCLUDE_DESCRIPTION, includeDescChk.getSelection());
            this.ps.setValue(PreferenceConstants.INCLUDE_STACKTRACE, includeStackTraceChk.getSelection());
            this.ps.setValue(PreferenceConstants.ONLY_HAS_CVE, onlyHasCVEChk.getSelection());
            this.ps.setValue(PreferenceConstants.WITH_CVSS, withCVSSInfoChk.getSelection());
            this.ps.setValue(PreferenceConstants.WITH_EPSS, withEPSSInfoChk.getSelection());
            this.ps.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
        } else if ("tabSelected".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("buttonEnabled".equals(event.getPropertyName())) { //$NON-NLS-1$
            loadBtn.setEnabled((Boolean) event.getNewValue());
            vulExecuteBtn.setEnabled((Boolean) event.getNewValue());
        } else if ("validOrgChanged".equals(event.getPropertyName())) { //$NON-NLS-1$
            uiReset();
        }
    }

}
