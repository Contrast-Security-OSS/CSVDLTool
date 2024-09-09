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

package com.contrastsecurity.csvdltool.ui.monitordialog.routecoverage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.AppInfo;
import com.contrastsecurity.csvdltool.CSVDLToolShell;
import com.contrastsecurity.csvdltool.Main;
import com.contrastsecurity.csvdltool.Messages;
import com.contrastsecurity.csvdltool.RouteCoverageCSVColmunEnum;
import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.routecoverage.RouteCoveragesApi;
import com.contrastsecurity.csvdltool.model.Observation;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Route;
import com.contrastsecurity.csvdltool.model.RouteCoverageCSVColumn;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class RouteCoverageGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private String outDirPath;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private Timer timer;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public RouteCoverageGetWithProgress(Shell shell, PreferenceStore ps, String outDirPath, List<String> dstApps, Map<String, AppInfo> fullAppMap) {
        this.shell = shell;
        this.ps = ps;
        this.outDirPath = outDirPath;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        int auto_login_interval = this.ps.getInt(PreferenceConstants.AUTO_RELOGIN_INTERVAL);
        if (auto_login_interval > 0) {
            TimerTask task = new TimerTask() {
                public void run() {
                    shell.getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            ((CSVDLToolShell) shell).getMain().loggedOut();
                        }
                    });
                }
            };
            this.timer = new Timer();
            int time = 1000 * 60 * auto_login_interval;
            this.timer.schedule(task, time, time);
        }
        monitor.setTaskName(Messages.getString("routecoveragegetwithprogress.progress.loading.starting.routecoverage")); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        String csvFileFormat = this.ps.getString(PreferenceConstants.CSV_FILE_FORMAT_ROUTECOVERAGE);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = this.ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_ROUTECOVERAGE);
        }
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        String columnJsonStr = this.ps.getString(PreferenceConstants.CSV_COLUMN_ROUTECOVERAGE);
        int sleepTrace = this.ps.getInt(PreferenceConstants.SLEEP_ROUTECOVERAGE);
        List<RouteCoverageCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<RouteCoverageCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(this.shell, Messages.getString("routecoveragegetwithprogress.message.dialog.title"), String.format("%s\r\n%s", Messages.getString("routecoveragegetwithprogress.message.dialog.json.load.error.message"), columnJsonStr));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                columnList = new ArrayList<RouteCoverageCSVColumn>();
            }
        } else {
            columnList = new ArrayList<RouteCoverageCSVColumn>();
            for (RouteCoverageCSVColmunEnum colEnum : RouteCoverageCSVColmunEnum.sortedValues()) {
                columnList.add(new RouteCoverageCSVColumn(colEnum));
            }
        }
        SubMonitor sub1Monitor = subMonitor.split(90);
        sub1Monitor.setWorkRemaining(dstApps.size() * 2);
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            Set<Organization> orgs = new HashSet<Organization>();
            for (String appLabel : dstApps) {
                orgs.add(fullAppMap.get(appLabel).getOrganization());
            }

            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName(Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.label")); //$NON-NLS-1$
            //SubMonitor child1Monitor = sub1Monitor.split(100).setWorkRemaining(dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("%s[%s] %s (%d/%d)", Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.label"), org.getName(), appName, appIdx, //$NON-NLS-1$ //$NON-NLS-2$
                        dstApps.size()));
                List<Route> allRoutes = new ArrayList<Route>();
                Api reoutesApi = new RouteCoveragesApi(this.shell, this.ps, org, appId, 0);
                List<Route> tmpRoutes = (List<Route>) reoutesApi.get();
                int totalRouteCount = reoutesApi.getTotalCount();
                int attackProcessCount = 0;
                monitor.subTask(String.format("%s(%d/%d)", Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.list.label"), attackProcessCount, totalRouteCount)); //$NON-NLS-1$ //$NON-NLS-2$
                SubMonitor child1_1Monitor = sub1Monitor.split(1).setWorkRemaining(totalRouteCount);
                allRoutes.addAll(tmpRoutes);
                for (Route route : tmpRoutes) {
                    attackProcessCount++;
                    monitor.subTask(String.format("%s(%d/%d)", Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.list.label"), attackProcessCount, totalRouteCount)); //$NON-NLS-1$ //$NON-NLS-2$
                    child1_1Monitor.worked(1);
                    Thread.sleep(50);
                }
                boolean routeIncompleteFlg = false;
                routeIncompleteFlg = totalRouteCount > allRoutes.size();
                while (routeIncompleteFlg) {
                    Thread.sleep(1000);
                    reoutesApi = new RouteCoveragesApi(this.shell, this.ps, org, appId, allRoutes.size());
                    tmpRoutes = (List<Route>) reoutesApi.get();
                    allRoutes.addAll(tmpRoutes);
                    for (Route route : tmpRoutes) {
                        attackProcessCount++;
                        monitor.subTask(String.format("%s(%d/%d)", Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.list.label"), attackProcessCount, totalRouteCount)); //$NON-NLS-1$ //$NON-NLS-2$
                        child1_1Monitor.worked(1);
                        Thread.sleep(50);
                    }
                    routeIncompleteFlg = totalRouteCount > allRoutes.size();
                }
                SubMonitor child1_2Monitor = sub1Monitor.split(1).setWorkRemaining(allRoutes.size());
                int routeIdx = 1;
                for (Route route : allRoutes) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new OperationCanceledException();
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(String.format("%s %s (%d/%d)", Messages.getString("routecoveragegetwithprogress.progress.loading.routecoverage.count.title"), route.getSignature(), routeIdx, allRoutes.size())); //$NON-NLS-1$ //$NON-NLS-2$
                    for (RouteCoverageCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case RC_01:
                                // ==================== 01. ルート ====================
                                csvLineList.add(route.getSignature());
                                break;
                            case RC_02: {
                                // ==================== 02. ルートのURL ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (Observation observation : route.getObservations()) {
                                    sj.add(observation.toString());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case RC_03: {
                                // ==================== 03. 環境 ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (String environment : route.getEnvironments()) {
                                    sj.add(environment);
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case RC_04: {
                                // ==================== 04. サーバ ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (Server server : route.getServers()) {
                                    sj.add(server.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case RC_05: {
                                // ==================== 05. 脆弱性 ====================
                                csvLineList.add(String.valueOf(route.getVulnerabilities()));
                                break;
                            }
                            case RC_06: {
                                // ==================== 06. アプリケーション ====================
                                csvLineList.add(route.getApp().getName());
                                break;
                            }
                            case RC_07: {
                                // ==================== 07. 最後のアクティビティ ====================
                                csvLineList.add(route.getFormatExercised());
                                break;
                            }
                            case RC_08: {
                                // ==================== 08. ステータス ====================
                                csvLineList.add(route.getStatus());
                                break;
                            }
                            default:
                                continue;
                        }
                    }
                    csvList.add(csvLineList);
                    routeIdx++;
                    child1_2Monitor.worked(1);
                    Thread.sleep(50);
                }
                monitor.subTask(""); //$NON-NLS-1$
                //child1Monitor.worked(1);
                appIdx++;
                Thread.sleep(sleepTrace);
            }
        } catch (OperationCanceledException oce) {
            throw new InvocationTargetException(new OperationCanceledException(Messages.getString("routecoveragegetwithprogress.progress.canceled"))); //$NON-NLS-1$
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (this.timer != null) {
                this.timer.cancel();
            }
        }
        //sub1Monitor.done();

        // ========== CSV出力 ==========
        monitor.setTaskName(Messages.getString("routecoveragegetwithprogress.progress.output.csv")); //$NON-NLS-1$
        SubMonitor sub2Monitor = subMonitor.split(10);
        sub2Monitor.setWorkRemaining(csvList.size());
        //SubMonitor sub2Monitor = subMonitor.split(10).setWorkRemaining(csvList.size());
        Thread.sleep(500);
        String filePath = timestamp + ".csv"; //$NON-NLS-1$
        String csv_encoding = Main.CSV_WIN_ENCODING;
        if (OS.isFamilyMac()) {
            csv_encoding = Main.CSV_MAC_ENCODING;
        }
        filePath = this.outDirPath + System.getProperty("file.separator") + filePath; //$NON-NLS-1$
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (this.ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_ROUTECOVERAGE)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (RouteCoverageCSVColumn csvColumn : columnList) {
                    if (csvColumn.isValid()) {
                        csvHeaderList.add(csvColumn.getColumn().getCulumn());
                    }
                }
                printer.printRecord(csvHeaderList);
            }
            for (List<String> csvLine : csvList) {
                printer.printRecord(csvLine);
                sub2Monitor.worked(1);
                Thread.sleep(15);
            }
        } catch (OperationCanceledException oce) {
            throw new InvocationTargetException(new OperationCanceledException(Messages.getString("routecoveragegetwithprogress.progress.canceled"))); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
        }
        //sub2Monitor.done();
        monitor.done();
    }
}
