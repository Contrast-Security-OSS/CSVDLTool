package com.contrastsecurity.csvdltool.ui;

import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.csvdltool.AttackEventCSVColmunEnum;
import com.contrastsecurity.csvdltool.AttackEventDetectedDateFilterEnum;
import com.contrastsecurity.csvdltool.AttackEventFilterDialog;
import com.contrastsecurity.csvdltool.AttackEventsGetWithProgress;
import com.contrastsecurity.csvdltool.AttackGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.FilterEnum;
import com.contrastsecurity.csvdltool.FilterLastDetectedDialog;
import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.TagEditDialog;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackEventTagsApi;
import com.contrastsecurity.csvdltool.api.PutTagsToAttackEventsApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.AttackEventCSVColumn;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ProtectTabItem extends CTabItem implements PropertyChangeListener {

    private Button attackLoadBtn;
    private Label attackEventCount;
    private List<Button> attackTermRadios = new ArrayList<Button>();
    private Button attackTerm30days;
    private Button attackTermYesterday;
    private Button attackTermToday;
    private Button attackTermLastWeek;
    private Button attackTermThisWeek;
    private Button attackTermPeriod;
    private Text attackDetectedFilterTxt;
    private Date frDetectedDate;
    private Date toDetectedDate;
    private Table attackTable;
    private List<AttackEvent> attackEvents;
    private List<AttackEvent> filteredAttackEvents = new ArrayList<AttackEvent>();
    private Map<AttackEventDetectedDateFilterEnum, LocalDate> attackEventDetectedFilterMap;

    private Map<FilterEnum, Set<Filter>> protectFilterMap;

    private CSVDLToolShell toolShell;
    private PreferenceStore ps;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)"); //$NON-NLS-1$

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ProtectTabItem(CTabFolder mainTabFolder, CSVDLToolShell toolShell, PreferenceStore ps, Point bigBtnSize) {
        super(mainTabFolder, SWT.NONE);
        this.toolShell = toolShell;
        this.ps = ps;
        setText(Messages.getString("main.tab.protect.title")); //$NON-NLS-1$
        setImage(new Image(toolShell.getDisplay(), getClass().getClassLoader().getResourceAsStream("contrast-protect-rasp-02.png"))); //$NON-NLS-1$

        Composite protectShell = new Composite(mainTabFolder, SWT.NONE);
        protectShell.setLayout(new GridLayout(1, false));

        Group attackListGrp = new Group(protectShell, SWT.NONE);
        attackListGrp.setLayout(new GridLayout(3, false));
        GridData attackListGrpGrDt = new GridData(GridData.FILL_BOTH);
        attackListGrpGrDt.minimumHeight = 200;
        attackListGrp.setLayoutData(attackListGrpGrDt);

        Composite attackTermGrp = new Composite(attackListGrp, SWT.NONE);
        attackTermGrp.setLayout(new GridLayout(7, false));
        GridData attackTermGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackTermGrp.setLayoutData(attackTermGrpGrDt);
        attackTerm30days = new Button(attackTermGrp, SWT.RADIO);
        attackTerm30days.setText(Messages.getString("main.attackevent.data.range.radio.all")); //$NON-NLS-1$
        attackTermRadios.add(attackTerm30days);
        attackTermYesterday = new Button(attackTermGrp, SWT.RADIO);
        attackTermYesterday.setText(Messages.getString("main.attackevent.data.range.radio.yesterday")); //$NON-NLS-1$
        attackTermRadios.add(attackTermYesterday);
        attackTermToday = new Button(attackTermGrp, SWT.RADIO);
        attackTermToday.setText(Messages.getString("main.attackevent.data.range.radio.today")); //$NON-NLS-1$
        attackTermRadios.add(attackTermToday);
        attackTermLastWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermLastWeek.setText(Messages.getString("main.attackevent.data.range.radio.lastweek")); //$NON-NLS-1$
        attackTermRadios.add(attackTermLastWeek);
        attackTermThisWeek = new Button(attackTermGrp, SWT.RADIO);
        attackTermThisWeek.setText(Messages.getString("main.attackevent.data.range.radio.thisweek")); //$NON-NLS-1$
        attackTermRadios.add(attackTermThisWeek);
        attackTermPeriod = new Button(attackTermGrp, SWT.RADIO);
        attackTermPeriod.setText(Messages.getString("main.attackevent.data.range.radio.custom")); //$NON-NLS-1$
        attackTermPeriod.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (attackTermPeriod.getSelection()) {
                    long frLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR);
                    long toLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO);
                    Date fr = frLong > 0 ? new Date(frLong) : null;
                    Date to = frLong > 0 ? new Date(toLong) : null;
                    if (fr == null && to == null) {
                        attackDetectedTermUpdate();
                        attackLoadBtn.setFocus();
                    }
                    attackDetectedFilterTxt.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                } else {
                    attackDetectedFilterTxt.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
                }
            }
        });
        attackTermRadios.add(attackTermPeriod);
        attackDetectedFilterTxt = new Text(attackTermGrp, SWT.BORDER);
        long frLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR);
        long toLong = ps.getLong(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO);
        this.frDetectedDate = frLong > 0 ? new Date(frLong) : null;
        this.toDetectedDate = frLong > 0 ? new Date(toLong) : null;
        attackDetectedTermTextSet(this.frDetectedDate, this.toDetectedDate);
        attackDetectedFilterTxt.setEditable(false);
        attackDetectedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        attackDetectedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                attackDetectedTermUpdate();
                if (!attackDetectedFilterTxt.getText().isEmpty()) {
                    for (Button rdo : attackTermRadios) {
                        rdo.setSelection(false);
                    }
                    attackTermPeriod.setSelection(true);
                }
                attackLoadBtn.setFocus();
            }
        });
        for (Button termBtn : this.attackTermRadios) {
            termBtn.setSelection(false);
            if (this.attackTermRadios.indexOf(termBtn) == this.ps.getInt(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER)) {
                termBtn.setSelection(true);
            }
        }
        if (attackTermPeriod.getSelection()) {
            attackDetectedFilterTxt.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        } else {
            attackDetectedFilterTxt.setForeground(toolShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        }

        attackLoadBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackLoadBtnGrDt.horizontalSpan = 3;
        attackLoadBtnGrDt.minimumHeight = 50;
        attackLoadBtnGrDt.heightHint = bigBtnSize.y + 20;
        attackLoadBtn.setLayoutData(attackLoadBtnGrDt);
        attackLoadBtn.setText(Messages.getString("main.attackevent.load.button.title")); //$NON-NLS-1$
        attackLoadBtn.setToolTipText(Messages.getString("main.attackevent.load.button.tooltip")); //$NON-NLS-1$
        attackLoadBtn.setFont(new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        attackLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filteredAttackEvents.clear();
                attackTable.clearAll();
                attackTable.removeAll();
                Date[] frToDate = getFrToDetectedDate();
                if (frToDate.length != 2) {
                    MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.load.message.dialog.undefined.term.error.message")); //$NON-NLS-1$
                    return;
                }
                AttackEventsGetWithProgress progress = new AttackEventsGetWithProgress(toolShell, ps, toolShell.getMain().getValidOrganizations(), frToDate[0], frToDate[1]);
                ProgressMonitorDialog progDialog = new AttackGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                    attackEvents = progress.getAllAttackEvents();
                    Collections.reverse(attackEvents);
                    filteredAttackEvents.addAll(attackEvents);
                    for (AttackEvent attackEvent : attackEvents) {
                        addColToAttackTable(attackEvent, -1);
                    }
                    protectFilterMap = progress.getFilterMap();
                    attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size())); //$NON-NLS-1$
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        this.attackEventCount = new Label(attackListGrp, SWT.RIGHT);
        GridData attackEventCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackEventCountGrDt.minimumHeight = 12;
        attackEventCountGrDt.minimumWidth = 30;
        this.attackEventCount.setLayoutData(attackEventCountGrDt);
        this.attackEventCount.setFont(new Font(toolShell.getDisplay(), "Arial", 10, SWT.NORMAL)); //$NON-NLS-1$
        this.attackEventCount.setText("0/0"); //$NON-NLS-1$

        attackTable = new Table(attackListGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        attackTable.setLayoutData(tableGrDt);
        attackTable.setLinesVisible(true);
        attackTable.setHeaderVisible(true);
        Menu menuTable = new Menu(attackTable);
        attackTable.setMenu(menuTable);

        MenuItem miTag = new MenuItem(menuTable, SWT.NONE);
        miTag.setText(Messages.getString("main.attackevent.menu.item.edit.tag")); //$NON-NLS-1$
        miTag.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                // TagInputDialog tagInputDialog = new TagInputDialog(shell);
                Set<String> existTagSet = new TreeSet<String>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    for (String existTag : attackEvent.getTags()) {
                        existTagSet.add(existTag);
                    }
                }
                TagEditDialog tagEditDialog = new TagEditDialog(toolShell, new ArrayList<>(existTagSet));
                int result = tagEditDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                String tag = tagEditDialog.getTag();
                List<String> removeTags = tagEditDialog.getRemoveTags();
                if (tag == null && removeTags.isEmpty()) {
                    return;
                }
                Map<Organization, List<AttackEvent>> orgMap = new HashMap<Organization, List<AttackEvent>>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    if (orgMap.containsKey(attackEvent.getOrganization())) {
                        orgMap.get(attackEvent.getOrganization()).add(attackEvent);
                    } else {
                        orgMap.put(attackEvent.getOrganization(), new ArrayList<AttackEvent>(Arrays.asList(attackEvent)));
                    }
                }
                try {
                    for (Organization org : orgMap.keySet()) {
                        List<AttackEvent> attackEvents = orgMap.get(org);
                        Api putApi = new PutTagsToAttackEventsApi(toolShell, ps, org, attackEvents, tag, removeTags);
                        String msg = (String) putApi.put();
                        if (Boolean.valueOf(msg)) {
                            for (AttackEvent attackEvent : attackEvents) {
                                Api attackEventTagsApi = new AttackEventTagsApi(toolShell, ps, org, attackEvent.getEvent_uuid());
                                List<String> tags = (List<String>) attackEventTagsApi.get();
                                attackEvent.setTags(tags);
                            }
                            attackTable.clearAll();
                            attackTable.removeAll();
                            for (AttackEvent attackEvent : filteredAttackEvents) {
                                addColToAttackTable(attackEvent, -1);
                            }
                            MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.message.dialog.edit.tag.title"), //$NON-NLS-1$
                                    Messages.getString("main.attackevent.message.dialog.edit.tag.message")); //$NON-NLS-1$
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });

        MenuItem miExp = new MenuItem(menuTable, SWT.NONE);
        miExp.setText(Messages.getString("main.attackevent.menu.item.export.csv")); //$NON-NLS-1$
        miExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = toolShell.getMain().getOutDirPath();
                }
                int[] selectIndexes = attackTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ATTACKEVENT);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String filePath = timestamp + ".csv"; //$NON-NLS-1$
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                filePath = outDirPath + System.getProperty("file.separator") + filePath;
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_ATTACKEVENT);
                List<AttackEventCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<AttackEventCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(toolShell, Messages.getString("main.attackevent.message.dialog.json.load.error.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.attackevent.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
                        columnList = new ArrayList<AttackEventCSVColumn>();
                    }
                } else {
                    columnList = new ArrayList<AttackEventCSVColumn>();
                    for (AttackEventCSVColmunEnum colEnum : AttackEventCSVColmunEnum.sortedValues()) {
                        columnList.add(new AttackEventCSVColumn(colEnum));
                    }
                }
                for (int idx : selectIndexes) {
                    List<String> csvLineList = new ArrayList<String>();
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    for (AttackEventCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case ATTACK_EVENT_01:
                                // ==================== 01. ソース名 ====================
                                csvLineList.add(attackEvent.getSource_name());
                                break;
                            case ATTACK_EVENT_02:
                                // ==================== 02. ソースIP ====================
                                csvLineList.add(attackEvent.getSource());
                                break;
                            case ATTACK_EVENT_03:
                                // ==================== 03. 結果 ====================
                                csvLineList.add(attackEvent.getResult());
                                break;
                            case ATTACK_EVENT_04:
                                // ==================== 04. アプリケーション ====================
                                csvLineList.add(attackEvent.getApplication().getName());
                                break;
                            case ATTACK_EVENT_05:
                                // ==================== 05. サーバ ====================
                                csvLineList.add(attackEvent.getServer().getName());
                                break;
                            case ATTACK_EVENT_06:
                                // ==================== 06. ルール ====================
                                csvLineList.add(attackEvent.getRule());
                                break;
                            case ATTACK_EVENT_07:
                                // ==================== 07. 時間 ====================
                                csvLineList.add(attackEvent.getFormatReceived());
                                break;
                            case ATTACK_EVENT_08:
                                // ==================== 08. URL ====================
                                csvLineList.add(attackEvent.getUrl());
                                break;
                            case ATTACK_EVENT_09:
                                // ==================== 09. 攻撃値 ====================
                                csvLineList.add(attackEvent.getUser_input().getValue());
                                break;
                            case ATTACK_EVENT_10:
                                // ==================== 10. タグ ====================
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), attackEvent.getTags())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                break;
                            case ATTACK_EVENT_11:
                                // ==================== 11. 組織名 ====================
                                csvLineList.add(attackEvent.getOrganization().getName());
                                break;
                            case ATTACK_EVENT_12:
                                // ==================== 12. 組織ID ====================
                                csvLineList.add(attackEvent.getOrganization().getOrganization_uuid());
                                break;
                            case ATTACK_EVENT_13: {
                                // ==================== 13. 攻撃イベントへのリンク ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(link);
                                break;
                            }
                            case ATTACK_EVENT_14: {
                                // ==================== 14. 攻撃イベントへのリンク（ハイパーリンク） ====================
                                String link = String.format("%s/static/ng/index.html#/%s/attacks/events/%s", ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                        attackEvent.getOrganization().getOrganization_uuid().trim(), attackEvent.getEvent_uuid());
                                csvLineList.add(String.format("=HYPERLINK(\"%s\",\"%s\")", link, Messages.getString("main.to.teamserver.hyperlink.text"))); //$NON-NLS-1$//$NON-NLS-2$
                                break;
                            }
                        }
                    }
                    csvList.add(csvLineList);
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_ATTACKEVENT)) {
                        List<String> csvHeaderList = new ArrayList<String>();
                        for (AttackEventCSVColumn csvColumn : columnList) {
                            if (csvColumn.isValid()) {
                                csvHeaderList.add(csvColumn.getColumn().getCulumn());
                            }
                        }
                        printer.printRecord(csvHeaderList);
                    }
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                    MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.message.dialog.export.csv.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.message.dialog.export.csv.message")); //$NON-NLS-1$
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miReport = new MenuItem(menuTable, SWT.NONE);
        miReport.setText(Messages.getString("main.attackevent.menu.item.output.report")); //$NON-NLS-1$
        miReport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = toolShell.getMain().getOutDirPath();
                }
                int[] selectIndexes = attackTable.getSelectionIndices();
                Set<String> srcIpSet = new HashSet<String>();
                Set<String> ruleSet = new HashSet<String>();
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    srcIpSet.add(attackEvent.getSource());
                    ruleSet.add(attackEvent.getRule());
                }
                Map<String, Map<String, Integer>> srcIpMap = new HashMap<String, Map<String, Integer>>();
                for (String srcIp : srcIpSet) {
                    Map<String, Integer> ruleMap = new HashMap<String, Integer>();
                    for (String rule : ruleSet) {
                        ruleMap.put(rule, 0);
                    }
                    srcIpMap.put(srcIp, ruleMap);
                }
                for (int idx : selectIndexes) {
                    AttackEvent attackEvent = filteredAttackEvents.get(idx);
                    String srcIp = attackEvent.getSource();
                    String rule = attackEvent.getRule();
                    int cnt = srcIpMap.get(srcIp).get(rule).intValue();
                    srcIpMap.get(srcIp).put(rule, ++cnt);
                }
                String timestamp = new SimpleDateFormat("'protect_report'_yyyy-MM-dd_HHmmss").format(new Date()); //$NON-NLS-1$
                String filePath = timestamp + ".txt"; //$NON-NLS-1$
                String txt_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    txt_encoding = Main.CSV_MAC_ENCODING;
                }
                filePath = outDirPath + System.getProperty("file.separator") + filePath;
                File dir = new File(new File(filePath).getParent());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File f = new File(filePath);
                try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), txt_encoding)))) {
                    for (String srcIp : srcIpMap.keySet()) {
                        printWriter.println(String.format("- %s", srcIp)); //$NON-NLS-1$
                        Map<String, Integer> ruleMap = srcIpMap.get(srcIp);
                        for (String rule : ruleMap.keySet()) {
                            int cnt = ruleMap.get(rule).intValue();
                            if (cnt > 0) {
                                printWriter.println(String.format("  - %s: %d", rule, cnt)); //$NON-NLS-1$
                            }
                        }
                    }
                    MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.message.dialog.export.txt.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.message.dialog.export.txt.message")); //$NON-NLS-1$
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miJump = new MenuItem(menuTable, SWT.NONE);
        miJump.setText(Messages.getString("main.attackevent.menu.item.browser.open")); //$NON-NLS-1$
        miJump.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = attackTable.getSelectionIndices();
                if (selectIndexes.length > 10) {
                    MessageBox messageBox = new MessageBox(toolShell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    messageBox.setText(Messages.getString("main.attackevent.message.dialog.browser.open.title")); //$NON-NLS-1$
                    messageBox.setMessage(String.format(Messages.getString("main.attackevent.message.dialog.browser.open.confirm.message"), selectIndexes.length)); //$NON-NLS-1$
                    int response = messageBox.open();
                    if (response == SWT.NO) {
                        return;
                    }
                }
                try {
                    Desktop desktop = Desktop.getDesktop();
                    for (int idx : selectIndexes) {
                        AttackEvent attackEvent = filteredAttackEvents.get(idx);
                        String contrastUrl = ps.getString(PreferenceConstants.CONTRAST_URL);
                        String orgUuid = attackEvent.getOrganization().getOrganization_uuid();
                        String eventUuid = attackEvent.getEvent_uuid();
                        desktop.browse(new URI(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid))); //$NON-NLS-1$
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (URISyntaxException urise) {
                    urise.printStackTrace();
                }
            }
        });

        MenuItem miUrlCopy = new MenuItem(menuTable, SWT.NONE);
        miUrlCopy.setText(Messages.getString("main.attackevent.menu.item.copy.teamserver.url")); //$NON-NLS-1$
        miUrlCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectIndex = attackTable.getSelectionIndex();
                AttackEvent attackEvent = filteredAttackEvents.get(selectIndex);
                String contrastUrl = ps.getString(PreferenceConstants.CONTRAST_URL);
                String orgUuid = attackEvent.getOrganization().getOrganization_uuid();
                String eventUuid = attackEvent.getEvent_uuid();
                Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(String.format("%s/static/ng/index.html#/%s/attacks/events/%s", contrastUrl, orgUuid.trim(), eventUuid)); //$NON-NLS-1$
                clipboard.setContents(selection, null);
            }
        });

        MenuItem miSelectAll = new MenuItem(menuTable, SWT.NONE);
        miSelectAll.setText(Messages.getString("main.attackevent.menu.item.select.all")); //$NON-NLS-1$
        miSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                attackTable.selectAll();
            }
        });

        attackTable.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                miUrlCopy.setEnabled(true);
                if (attackTable.getSelectionCount() <= 0) {
                    event.doit = false;
                } else if (attackTable.getSelectionCount() != 1) {
                    miUrlCopy.setEnabled(false);
                }
            }
        });
        attackTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && e.keyCode == 'a') {
                    attackTable.selectAll();
                    e.doit = false;
                }
            }
        });

        TableColumn column0 = new TableColumn(attackTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(attackTable, SWT.LEFT);
        column1.setWidth(120);
        column1.setText(Messages.getString("main.attackevent.table.column0.title")); //$NON-NLS-1$
        TableColumn column2 = new TableColumn(attackTable, SWT.LEFT);
        column2.setWidth(120);
        column2.setText(Messages.getString("main.attackevent.table.column1.title")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(attackTable, SWT.CENTER);
        column3.setWidth(100);
        column3.setText(Messages.getString("main.attackevent.table.column2.title")); //$NON-NLS-1$
        TableColumn column4 = new TableColumn(attackTable, SWT.LEFT);
        column4.setWidth(250);
        column4.setText(Messages.getString("main.attackevent.table.column3.title")); //$NON-NLS-1$
        TableColumn column5 = new TableColumn(attackTable, SWT.LEFT);
        column5.setWidth(200);
        column5.setText(Messages.getString("main.attackevent.table.column4.title")); //$NON-NLS-1$
        TableColumn column6 = new TableColumn(attackTable, SWT.LEFT);
        column6.setWidth(200);
        column6.setText(Messages.getString("main.attackevent.table.column5.title")); //$NON-NLS-1$
        TableColumn column7 = new TableColumn(attackTable, SWT.LEFT);
        column7.setWidth(150);
        column7.setText(Messages.getString("main.attackevent.table.column6.title")); //$NON-NLS-1$
        TableColumn column8 = new TableColumn(attackTable, SWT.LEFT);
        column8.setWidth(150);
        column8.setText(Messages.getString("main.attackevent.table.column7.title")); //$NON-NLS-1$
        TableColumn column9 = new TableColumn(attackTable, SWT.LEFT);
        column9.setWidth(250);
        column9.setText(Messages.getString("main.attackevent.table.column8.title")); //$NON-NLS-1$
        TableColumn column10 = new TableColumn(attackTable, SWT.LEFT);
        column10.setWidth(250);
        column10.setText(Messages.getString("main.attackevent.table.column9.title")); //$NON-NLS-1$
        TableColumn column11 = new TableColumn(attackTable, SWT.LEFT);
        column11.setWidth(150);
        column11.setText(Messages.getString("main.attackevent.table.column10.title")); //$NON-NLS-1$

        Button attackEventFilterBtn = new Button(attackListGrp, SWT.PUSH);
        GridData attackEventFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        attackEventFilterBtnGrDt.horizontalSpan = 3;
        attackEventFilterBtn.setLayoutData(attackEventFilterBtnGrDt);
        attackEventFilterBtn.setText(Messages.getString("main.attackevent.filter.button.title")); //$NON-NLS-1$
        attackEventFilterBtn.setToolTipText(Messages.getString("main.attackevent.filter.button.tooltip")); //$NON-NLS-1$
        attackEventFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (protectFilterMap == null) {
                    MessageDialog.openInformation(toolShell, Messages.getString("main.attackevent.filter.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.attackevent.filter.not.loaded.error.message")); //$NON-NLS-1$
                    return;
                }
                String dayTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                String nightTimeHours = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                if (!dayTimeHours.isEmpty() || !nightTimeHours.isEmpty()) {
                    Set<Filter> businessHoursFilterSet = new LinkedHashSet<Filter>();
                    if (!dayTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.daytime"))); //$NON-NLS-1$
                    }
                    if (!nightTimeHours.isEmpty()) {
                        businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.nighttime"))); //$NON-NLS-1$
                    }
                    businessHoursFilterSet.add(new Filter(Messages.getString("main.attackevent.filter.time.slot.othertime"))); //$NON-NLS-1$
                    protectFilterMap.put(FilterEnum.BUSINESS_HOURS, businessHoursFilterSet);
                }
                AttackEventFilterDialog filterDialog = new AttackEventFilterDialog(toolShell, protectFilterMap);
                filterDialog.addPropertyChangeListener(toolShell.getMain());
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
            }
        });
        setControl(protectShell);
    }

    private void addColToAttackTable(AttackEvent attackEvent, int index) {
        if (attackEvent == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(attackTable, SWT.CENTER, index);
        } else {
            item = new TableItem(attackTable, SWT.CENTER);
        }
        item.setText(1, attackEvent.getSource_name());
        item.setText(2, attackEvent.getSource());
        item.setText(3, attackEvent.getResult());
        item.setText(4, attackEvent.getApplication().getName());
        item.setText(5, attackEvent.getServer().getName());
        item.setText(6, attackEvent.getRule());
        item.setText(7, attackEvent.getFormatReceived());
        item.setText(8, attackEvent.getUrl());
        item.setText(9, attackEvent.getUser_input().getValue());
        String tags = String.join(",", attackEvent.getTags()); //$NON-NLS-1$
        item.setText(10, tags);
        item.setText(11, attackEvent.getOrganization().getName());
    }

    private void attackDetectedTermUpdate() {
        FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(toolShell, frDetectedDate, toDetectedDate);
        int result = filterDialog.open();
        if (IDialogConstants.OK_ID != result) {
            attackLoadBtn.setFocus();
            return;
        }
        frDetectedDate = filterDialog.getFrDate();
        toDetectedDate = filterDialog.getToDate();
        attackDetectedTermTextSet(frDetectedDate, toDetectedDate);
        ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_FR, frDetectedDate != null ? frDetectedDate.getTime() : 0);
        ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_TERM_TO, toDetectedDate != null ? toDetectedDate.getTime() : 0);
    }

    private void attackDetectedTermTextSet(Date fr, Date to) {
        if (fr != null && to != null) {
            attackDetectedFilterTxt.setText(String.format("%s - %s", sdf.format(fr), sdf.format(to))); //$NON-NLS-1$
        } else if (frDetectedDate != null) {
            attackDetectedFilterTxt.setText(String.format("%s -", sdf.format(fr))); //$NON-NLS-1$
        } else if (toDetectedDate != null) {
            attackDetectedFilterTxt.setText(String.format("- %s", sdf.format(to))); //$NON-NLS-1$
        } else {
            attackDetectedFilterTxt.setText(""); //$NON-NLS-1$
        }
    }

    public void updateProtectOption() {
        this.attackEventDetectedFilterMap = getAttackEventDetectedDateMap();
        attackTermToday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))); //$NON-NLS-1$
        attackTermYesterday.setToolTipText(this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)"))); //$NON-NLS-1$
        attackTerm30days.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
        attackTermLastWeek.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
        attackTermThisWeek.setToolTipText(String.format("%s ～ %s", //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_START).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")), //$NON-NLS-1$
                this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_END).format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)")))); //$NON-NLS-1$
    }

    private Date[] getFrToDetectedDate() {
        int idx = -1;
        for (Button termBtn : this.attackTermRadios) {
            if (termBtn.getSelection()) {
                idx = attackTermRadios.indexOf(termBtn);
                break;
            }
        }
        if (idx < 0) {
            idx = 0;
        }
        LocalDate frLocalDate = null;
        LocalDate toLocalDate = null;
        switch (idx) {
            case 0: // 30days
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                break;
            case 1: // Yesterday
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.YESTERDAY);
                break;
            case 2: // Today
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
                break;
            case 3: // LastWeek
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_START);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.LAST_WEEK_END);
                break;
            case 4: // ThisWeek
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_START);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.THIS_WEEK_END);
                break;
            case 5: // Specify
                if (frDetectedDate == null || toDetectedDate == null) {
                    return new Date[] {};
                }
                return new Date[] { frDetectedDate, toDetectedDate };
            default:
                frLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS);
                toLocalDate = this.attackEventDetectedFilterMap.get(AttackEventDetectedDateFilterEnum.TODAY);
        }
        Date frDate = Date.from(frLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.set(toLocalDate.getYear(), toLocalDate.getMonthValue() - 1, toLocalDate.getDayOfMonth(), 23, 59, 59);
        Date toDate = cal.getTime();
        return new Date[] { frDate, toDate };
    }

    public Map<AttackEventDetectedDateFilterEnum, LocalDate> getAttackEventDetectedDateMap() {
        Map<AttackEventDetectedDateFilterEnum, LocalDate> map = new HashMap<AttackEventDetectedDateFilterEnum, LocalDate>();
        LocalDate today = LocalDate.now();
        map.put(AttackEventDetectedDateFilterEnum.TODAY, today);
        map.put(AttackEventDetectedDateFilterEnum.YESTERDAY, today.minusDays(1));
        map.put(AttackEventDetectedDateFilterEnum.BEFORE_30_DAYS, today.minusDays(30));
        LocalDate lastWeekStart = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        lastWeekStart = lastWeekStart.minusDays(7 - ps.getInt(PreferenceConstants.ATTACK_START_WEEKDAY));
        if (lastWeekStart.plusDays(7).isAfter(today)) {
            lastWeekStart = lastWeekStart.minusDays(7);
        }
        map.put(AttackEventDetectedDateFilterEnum.LAST_WEEK_START, lastWeekStart);
        map.put(AttackEventDetectedDateFilterEnum.LAST_WEEK_END, lastWeekStart.plusDays(6));
        map.put(AttackEventDetectedDateFilterEnum.THIS_WEEK_START, lastWeekStart.plusDays(7));
        map.put(AttackEventDetectedDateFilterEnum.THIS_WEEK_END, lastWeekStart.plusDays(13));
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("shellActivated".equals(event.getPropertyName())) { //$NON-NLS-1$
            updateProtectOption();
        } else if ("shellClosed".equals(event.getPropertyName())) { //$NON-NLS-1$
            for (Button termBtn : attackTermRadios) {
                if (termBtn.getSelection()) {
                    ps.setValue(PreferenceConstants.ATTACK_DETECTED_DATE_FILTER, attackTermRadios.indexOf(termBtn));
                }
            }
        } else if ("tabSelected".equals(event.getPropertyName())) { //$NON-NLS-1$
            if (event.getNewValue() instanceof ProtectTabItem) {
                System.out.println("protect tab selected.");
                updateProtectOption();
            }
        } else if ("buttonEnabled".equals(event.getPropertyName())) { //$NON-NLS-1$
            attackLoadBtn.setEnabled((Boolean) event.getNewValue());
        } else if ("attackEventFilter".equals(event.getPropertyName())) { //$NON-NLS-1$
            Map<FilterEnum, Set<Filter>> filterMap = (Map<FilterEnum, Set<Filter>>) event.getNewValue();
            attackTable.clearAll();
            attackTable.removeAll();
            filteredAttackEvents.clear();
            for (AttackEvent attackEvent : attackEvents) {
                boolean lostFlg = false;
                for (Filter filter : filterMap.get(FilterEnum.SOURCE_NAME)) {
                    if (attackEvent.getSource_name().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.SOURCE_IP)) {
                    if (attackEvent.getSource().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.APPLICATION)) {
                    if (attackEvent.getApplication().getName().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.RULE)) {
                    if (attackEvent.getRule().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.TAG)) {
                    if (filter.getLabel().equals("")) { //$NON-NLS-1$
                        if (attackEvent.getTags().isEmpty()) {
                            if (!filter.isValid()) {
                                lostFlg |= true;
                            }
                        }
                    }
                    if (attackEvent.getTags() != null && attackEvent.getTags().contains(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }

                // 時間帯フィルタ
                if (filterMap.containsKey(FilterEnum.BUSINESS_HOURS)) {
                    int target = Integer.parseInt(attackEvent.getTimeStrReceived());
                    String termDayTime = ps.getString(PreferenceConstants.ATTACK_RANGE_DAYTIME);
                    String termNightTime = ps.getString(PreferenceConstants.ATTACK_RANGE_NIGHTTIME);
                    for (Filter filter : filterMap.get(FilterEnum.BUSINESS_HOURS)) {
                        if (filter.isValid()) {
                            continue;
                        }
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.daytime"))) { //$NON-NLS-1$
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0]; //$NON-NLS-1$
                                String toDtStr = termDayTime.split("-")[1]; //$NON-NLS-1$
                                int frDt = Integer.parseInt(frDtStr);
                                int toDt = Integer.parseInt(toDtStr);
                                if (toDt < frDt) {
                                    if ((frDt <= target && target < 2400) || 0 <= target && target < toDt) {
                                        lostFlg |= true;
                                    }
                                } else {
                                    if (frDt <= target && target < toDt) {
                                        lostFlg |= true;
                                    }
                                }
                            }
                        }
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.nighttime"))) { //$NON-NLS-1$
                            if (!termNightTime.isEmpty()) {
                                String frNtStr = termNightTime.split("-")[0]; //$NON-NLS-1$
                                String toNtStr = termNightTime.split("-")[1]; //$NON-NLS-1$
                                int frNt = Integer.parseInt(frNtStr);
                                int toNt = Integer.parseInt(toNtStr);
                                if (toNt < frNt) {
                                    if ((frNt <= target && target < 2400) || 0 <= target && target < toNt) {
                                        lostFlg |= true;
                                    }
                                } else {
                                    if (frNt <= target && target < toNt) {
                                        lostFlg |= true;
                                    }
                                }
                            }
                        }
                        if (filter.getLabel().equals(Messages.getString("main.attackevent.filter.time.slot.othertime"))) { //$NON-NLS-1$
                            boolean hitFlg = false;
                            if (!termDayTime.isEmpty()) {
                                String frDtStr = termDayTime.split("-")[0]; //$NON-NLS-1$
                                String toDtStr = termDayTime.split("-")[1]; //$NON-NLS-1$
                                int frDt = Integer.parseInt(frDtStr);
                                int toDt = Integer.parseInt(toDtStr);
                                if (toDt < frDt) {
                                    if ((frDt <= target && target < 2400) || 0 <= target && target < toDt) {
                                        hitFlg |= true;
                                    }
                                } else {
                                    if (frDt <= target && target < toDt) {
                                        hitFlg |= true;
                                    }
                                }
                            }
                            if (!termNightTime.isEmpty()) {
                                String frNtStr = termNightTime.split("-")[0]; //$NON-NLS-1$
                                String toNtStr = termNightTime.split("-")[1]; //$NON-NLS-1$
                                int frNt = Integer.parseInt(frNtStr);
                                int toNt = Integer.parseInt(toNtStr);
                                if (toNt < frNt) {
                                    if ((frNt <= target && target < 2400) || 0 <= target && target < toNt) {
                                        hitFlg |= true;
                                    }
                                } else {
                                    if (frNt <= target && target < toNt) {
                                        hitFlg |= true;
                                    }
                                }
                            }
                            if (!hitFlg) {
                                lostFlg |= true;
                            }
                        }
                    }
                }
                if (!lostFlg) {
                    addColToAttackTable(attackEvent, -1);
                    filteredAttackEvents.add(attackEvent);
                }
            }
            attackEventCount.setText(String.format("%d/%d", filteredAttackEvents.size(), attackEvents.size())); //$NON-NLS-1$
        }
    }

}
