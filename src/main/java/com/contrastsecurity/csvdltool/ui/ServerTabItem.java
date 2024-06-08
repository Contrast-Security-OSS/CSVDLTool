package com.contrastsecurity.csvdltool.ui;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.FilterEnum;
import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.ServerCSVColmunEnum;
import com.contrastsecurity.csvdltool.ServerFilterDialog;
import com.contrastsecurity.csvdltool.ServerGetProgressMonitorDialog;
import com.contrastsecurity.csvdltool.ServerWithProgress;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.exception.BasicAuthException;
import com.contrastsecurity.csvdltool.exception.NonApiException;
import com.contrastsecurity.csvdltool.exception.TsvException;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.ServerCSVColumn;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ServerTabItem extends CTabItem implements PropertyChangeListener {

    private Button loadBtn;
    private Table table;
    private List<Server> servers;
    private List<Server> filteredServers = new ArrayList<Server>();
    private Map<FilterEnum, Set<Filter>> filterMap;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ServerTabItem(CTabFolder mainTabFolder, CSVDLToolShell toolShell, PreferenceStore ps, Point bigBtnSize) {
        super(mainTabFolder, SWT.NONE);
        setText(Messages.getString("main.tab.server.title")); //$NON-NLS-1$
        // serverTabItem.setImage(new Image(toolShell.getDisplay(), getClass().getClassLoader().getResourceAsStream("server16.png"))); //$NON-NLS-1$

        Composite shell = new Composite(mainTabFolder, SWT.NONE);
        shell.setLayout(new GridLayout(1, false));

        Group listGrp = new Group(shell, SWT.NONE);
        listGrp.setLayout(new GridLayout(3, false));
        GridData listGrpGrDt = new GridData(GridData.FILL_BOTH);
        listGrpGrDt.minimumHeight = 200;
        listGrp.setLayoutData(listGrpGrDt);

        loadBtn = new Button(listGrp, SWT.PUSH);
        GridData loadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        loadBtnGrDt.horizontalSpan = 3;
        loadBtnGrDt.minimumHeight = 50;
        loadBtnGrDt.heightHint = bigBtnSize.y + 20;
        loadBtn.setLayoutData(loadBtnGrDt);
        loadBtn.setText(Messages.getString("main.server.load.button.title")); //$NON-NLS-1$
        loadBtn.setToolTipText(Messages.getString("main.server.load.button.tooltip")); //$NON-NLS-1$
        loadBtn.setFont(new Font(toolShell.getDisplay(), "Arial", 20, SWT.NORMAL)); //$NON-NLS-1$
        loadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                table.clearAll();
                table.removeAll();
                ServerWithProgress progress = new ServerWithProgress(toolShell, ps, toolShell.getMain().getValidOrganizations());
                ProgressMonitorDialog progDialog = new ServerGetProgressMonitorDialog(toolShell);
                try {
                    progDialog.run(true, true, progress);
                    servers = progress.getAllServers();
                    filteredServers.addAll(servers);
                    for (Server server : servers) {
                        addColToTable(server, -1);
                    }
                    filterMap = progress.getFilterMap();
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
                        MessageDialog.openError(toolShell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.teamserver.return.error"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s %s\r\n%s", Messages.getString("main.unexpected.status.code.error"), errorMsg, //$NON-NLS-1$ //$NON-NLS-2$
                                        Messages.getString("main.message.dialog.make.sure.logfile.message"))); //$NON-NLS-1$
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof BasicAuthException) {
                        MessageDialog.openError(toolShell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else if (e.getTargetException() instanceof OperationCanceledException) {
                        MessageDialog.openInformation(toolShell, Messages.getString("main.server.load.message.dialog.title"), errorMsg); //$NON-NLS-1$
                        return;
                    } else {
                        MessageDialog.openError(toolShell, Messages.getString("main.server.load.message.dialog.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.message.dialog.unknown.error.message"), errorMsg)); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        table = new Table(listGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        Menu tableMenu = new Menu(table);
        table.setMenu(tableMenu);

        MenuItem miExport = new MenuItem(tableMenu, SWT.NONE);
        miExport.setText(Messages.getString("main.server.menu.item.export.csv")); //$NON-NLS-1$
        miExport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSaveOutDirPath = ps.getString(PreferenceConstants.FILE_OUT_MODE).equals("save");
                String outDirPath = ps.getString(PreferenceConstants.FILE_OUT_DIR);
                if (!isSaveOutDirPath || outDirPath.isEmpty()) {
                    outDirPath = toolShell.getMain().getOutDirPath();
                }

                int[] selectIndexes = table.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SERVER);
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
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_SERVER);
                List<ServerCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<ServerCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(toolShell, Messages.getString("main.server.message.dialog.json.load.error.title"), //$NON-NLS-1$
                                String.format("%s\r\n%s", Messages.getString("main.server.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
                        columnList = new ArrayList<ServerCSVColumn>();
                    }
                } else {
                    columnList = new ArrayList<ServerCSVColumn>();
                    for (ServerCSVColmunEnum colEnum : ServerCSVColmunEnum.sortedValues()) {
                        columnList.add(new ServerCSVColumn(colEnum));
                    }
                }
                for (int idx : selectIndexes) {
                    List<String> csvLineList = new ArrayList<String>();
                    Server server = filteredServers.get(idx);
                    for (ServerCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case SERVER_01:
                                // ==================== 01. サーバ名 ====================
                                csvLineList.add(server.getName());
                                break;
                            case SERVER_02:
                                // ==================== 02. パス ====================
                                csvLineList.add(server.getPath());
                                break;
                            case SERVER_03:
                                // ==================== 03. 言語 ====================
                                csvLineList.add(server.getLanguage());
                                break;
                            case SERVER_04:
                                // ==================== 04. エージェントバージョン ====================
                                csvLineList.add(server.getAgent_version());
                                break;
                        }
                    }
                    csvList.add(csvLineList);
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_SERVER)) {
                        List<String> csvHeaderList = new ArrayList<String>();
                        for (ServerCSVColumn csvColumn : columnList) {
                            if (csvColumn.isValid()) {
                                csvHeaderList.add(csvColumn.getColumn().getCulumn());
                            }
                        }
                        printer.printRecord(csvHeaderList);
                    }
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                    MessageDialog.openInformation(toolShell, Messages.getString("main.server.message.dialog.export.csv.title"), //$NON-NLS-1$
                            Messages.getString("main.server.message.dialog.export.csv.message")); //$NON-NLS-1$
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        table.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (table.getSelectionCount() <= 0) {
                    event.doit = false;
                }
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && e.keyCode == 'a') {
                    table.selectAll();
                    e.doit = false;
                }
            }
        });

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setWidth(0);
        column1.setResizable(false);
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(150);
        column2.setText(Messages.getString("main.server.table.column0.title")); //$NON-NLS-1$
        TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setWidth(360);
        column3.setText(Messages.getString("main.server.table.column1.title")); //$NON-NLS-1$
        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setWidth(100);
        column4.setText(Messages.getString("main.server.table.column2.title")); //$NON-NLS-1$
        TableColumn column5 = new TableColumn(table, SWT.LEFT);
        column5.setWidth(200);
        column5.setText(Messages.getString("main.server.table.column3.title")); //$NON-NLS-1$

        Button filterBtn = new Button(listGrp, SWT.PUSH);
        GridData filterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        filterBtnGrDt.horizontalSpan = 3;
        filterBtn.setLayoutData(filterBtnGrDt);
        filterBtn.setText(Messages.getString("main.server.filter.button.title")); //$NON-NLS-1$
        filterBtn.setToolTipText(Messages.getString("main.server.filter.button.tooltip")); //$NON-NLS-1$
        filterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (filterMap == null) {
                    MessageDialog.openInformation(toolShell, Messages.getString("main.server.filter.message.dialog.title"), //$NON-NLS-1$
                            Messages.getString("main.server.filter.not.loaded.error.message")); //$NON-NLS-1$
                    return;
                }
                ServerFilterDialog filterDialog = new ServerFilterDialog(toolShell, filterMap);
                filterDialog.addPropertyChangeListener(ServerTabItem.this);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
            }
        });
        setControl(shell);
    }

    private void addColToTable(Server server, int index) {
        if (server == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(table, SWT.CENTER, index);
        } else {
            item = new TableItem(table, SWT.CENTER);
        }
        item.setText(1, server.getName());
        item.setText(2, server.getPath());
        item.setText(3, server.getLanguage());
        item.setText(4, server.getAgent_version());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("shellActivated".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("shellClosed".equals(event.getPropertyName())) { //$NON-NLS-1$
        } else if ("tabSelected".equals(event.getPropertyName())) { //$NON-NLS-1$
            if (event.getNewValue() instanceof ServerTabItem) {
                System.out.println("server tab selected.");
            }
        } else if ("buttonEnabled".equals(event.getPropertyName())) { //$NON-NLS-1$
            loadBtn.setEnabled((Boolean) event.getNewValue());
        } else if ("serverFilter".equals(event.getPropertyName())) { //$NON-NLS-1$
            Map<FilterEnum, Set<Filter>> filterMap = (Map<FilterEnum, Set<Filter>>) event.getNewValue();
            table.clearAll();
            table.removeAll();
            filteredServers.clear();
            for (Server server : servers) {
                boolean lostFlg = false;
                for (Filter filter : filterMap.get(FilterEnum.LANGUAGE)) {
                    if (server.getLanguage().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                for (Filter filter : filterMap.get(FilterEnum.AGENT_VERSION)) {
                    if (server.getAgent_version().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                if (!lostFlg) {
                    addColToTable(server, -1);
                    filteredServers.add(server);
                }
            }
        }
    }
}
