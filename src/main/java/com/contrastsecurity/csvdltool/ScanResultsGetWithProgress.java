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

package com.contrastsecurity.csvdltool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.scan.ScanResultsApi;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.ScanResultCSVColumn;
import com.contrastsecurity.csvdltool.model.scan.ScanResult;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ScanResultsGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private String outDirPath;
    private List<String> dstProjects;
    private Map<String, ScanProjectInfo> fullMap;
    private boolean includeStackTraceChk;
    private Timer timer;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ScanResultsGetWithProgress(Shell shell, PreferenceStore ps, String outDirPath, List<String> dstProjects, Map<String, ScanProjectInfo> fullMap,
            boolean includeStackTraceChk) {
        this.shell = shell;
        this.ps = ps;
        this.outDirPath = outDirPath;
        this.dstProjects = dstProjects;
        this.fullMap = fullMap;
        this.includeStackTraceChk = includeStackTraceChk;
    }

    @SuppressWarnings("unchecked")
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
        monitor.setTaskName(Messages.getString("libgetwithprogress.progress.loading.starting.libraries")); //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        String csvFileFormat = this.ps.getString(PreferenceConstants.CSV_FILE_FORMAT_SCANRESULT);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = this.ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_SCANRESULT);
        }
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        int sleepTrace = this.ps.getInt(PreferenceConstants.SLEEP_SCANRESULT);
        String columnJsonStr = this.ps.getString(PreferenceConstants.CSV_COLUMN_SCANRESULT);
        List<ScanResultCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<ScanResultCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(this.shell, Messages.getString("libgetwithprogress.message.dialog.title"), //$NON-NLS-1$
                        String.format("%s\r\n%s", Messages.getString("libgetwithprogress.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
                columnList = new ArrayList<ScanResultCSVColumn>();
            }
        } else {
            columnList = new ArrayList<ScanResultCSVColumn>();
            for (ScanResultCSVColmunEnum colEnum : ScanResultCSVColmunEnum.sortedValues()) {
                columnList.add(new ScanResultCSVColumn(colEnum));
            }
        }
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // 長文情報（何が起こったか？など）を出力する場合はフォルダに出力
            if (this.includeStackTraceChk) {
                String dirPath = this.outDirPath + System.getProperty("file.separator") + timestamp; //$NON-NLS-1$
                Path dir = Paths.get(dirPath);
                Files.createDirectory(dir);
            }
            // 選択済みアプリのライブラリ情報を取得
            monitor.setTaskName(Messages.getString("libgetwithprogress.progress.loading.libraries")); //$NON-NLS-1$
            SubMonitor sub1Monitor = subMonitor.split(80).setWorkRemaining(dstProjects.size());
            int appIdx = 1;
            for (String appLabel : dstProjects) {
                Organization org = fullMap.get(appLabel).getOrganization();
                String projName = fullMap.get(appLabel).getName();
                String projId = fullMap.get(appLabel).getId();
                monitor.setTaskName(String.format("%s[%s] %s (%d/%d)", Messages.getString("libgetwithprogress.progress.loading.libraries"), org.getName(), projName, appIdx, //$NON-NLS-1$ //$NON-NLS-2$
                        dstProjects.size()));
                List<ScanResult> allScanResults = new ArrayList<ScanResult>();
                Api scanResultsApi = new ScanResultsApi(this.shell, this.ps, org, projId, allScanResults.size());
                allScanResults.addAll((List<ScanResult>) scanResultsApi.get());
                int totalCount = scanResultsApi.getTotalCount();
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > allScanResults.size();
                while (incompleteFlg) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new OperationCanceledException();
                    }
                    scanResultsApi = new ScanResultsApi(this.shell, this.ps, org, projId, allScanResults.size());
                    allScanResults.addAll((List<ScanResult>) scanResultsApi.get());
                    incompleteFlg = totalCount > allScanResults.size();
                    Thread.sleep(sleepTrace);
                }
                SubMonitor sub1_1Monitor = sub1Monitor.split(1).setWorkRemaining(allScanResults.size());
                int libIdx = 1;
                for (ScanResult result : allScanResults) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new OperationCanceledException();
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(String.format("%s %s (%d/%d)", "脆弱性", result.getName(), libIdx, allScanResults.size())); //$NON-NLS-1$
                    for (ScanResultCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case SCAN_RESULT_01:
                                // ==================== 01. 脆弱性 ====================
                                csvLineList.add(result.getMessage().get("text"));
                                break;
                            case SCAN_RESULT_02:
                                // ==================== 02. 深刻度 ====================
                                csvLineList.add(result.getSeverity());
                                break;
                            case SCAN_RESULT_03:
                                // ==================== 03. 言語 ====================
                                csvLineList.add(result.getLanguage());
                                break;
                            case SCAN_RESULT_04:
                                // ==================== 04. 最後の検出 ====================
                                csvLineList.add(result.getLastSeenTime());
                                break;
                            case SCAN_RESULT_05:
                                // ==================== 05. ステータス ====================
                                csvLineList.add(result.getStatus());
                                break;
                            default:
                                continue;
                        }
                    }
                    // if (includeStackTraceChk && !library.getVulns().isEmpty()) {
                    // // ==================== 23. 詳細 ====================
                    // if (OS.isFamilyWindows()) {
                    // csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", library.getHash(), library.getHash())); //$NON-NLS-1$
                    // } else {
                    // csvLineList.add(String.format("=HYPERLINK(\"%s.txt\",\"%s\")", library.getHash(), library.getHash())); //$NON-NLS-1$
                    // }
                    // String textFileName = String.format("%s%s%s.txt", timestamp, System.getProperty("file.separator"), library.getHash()); //$NON-NLS-1$ //$NON-NLS-2$
                    // textFileName = this.outDirPath + System.getProperty("file.separator") + textFileName; //$NON-NLS-1$
                    // File file = new File(textFileName);
                    // for (Vuln vuln : library.getVulns()) {
                    // // ==================== 23-1. タイトル ====================
                    // if (vuln.isHas_cvss3_score()) {
                    // String cvss3Ver = vuln.getCvss_3_vector().split(":")[1].replace("/AV", "");
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(String.format("=============== %s(CVSS%s %s) %s ===============", vuln.getName(), cvss3Ver, vuln.getCvss_3_severity_value(), //$NON-NLS-1$
                    // vuln.getCvss_3_severity_code())),
                    // true);
                    // } else {
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(
                    // String.format("=============== %s(CVSS %s) %s ===============", vuln.getName(), vuln.getSeverity_value(), vuln.getSeverity_code())), //$NON-NLS-1$
                    // true);
                    // }
                    // // ==================== 23-2. 説明 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(vuln.getDescription()), true);
                    // // ==================== 23-3. EPSS ====================
                    // String cisaStr = vuln.isCisa() ? Messages.getString("libgetwithprogress.cisa") : ""; //$NON-NLS-1$ //$NON-NLS-2$
                    // String epss = String.format("%.2f (%.2f%s) %s", vuln.getEpss_score(), vuln.getEpss_percentile(), Messages.getString("libgetwithprogress.percentile"),
                    // //$NON-NLS-1$ //$NON-NLS-2$
                    // cisaStr);
                    // FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.epss"), epss)),
                    // //$NON-NLS-1$ //$NON-NLS-2$
                    // true);
                    // // ==================== 23-4. 機密性への影響 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(
                    // String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.confidentiality-impact"), vuln.getConfidentiality_impact())), true);
                    // //$NON-NLS-1$ //$NON-NLS-2$
                    // // ==================== 23-5. 完全性への影響 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.integrity_impact"), vuln.getIntegrity_impact())), //$NON-NLS-1$
                    // //$NON-NLS-2$
                    // true);
                    // // ==================== 23-6. 可用性への影響 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(
                    // String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.availability_impact"), vuln.getAvailability_impact())), //$NON-NLS-1$
                    // //$NON-NLS-2$
                    // true);
                    // // ==================== 23-7. 攻撃前の認証要否 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.authentication"), vuln.getAuthentication())), true); //$NON-NLS-1$
                    // //$NON-NLS-2$
                    // // ==================== 23-8. 攻撃元区分 ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.access_vector"), vuln.getAccess_vector())), true); //$NON-NLS-1$
                    // //$NON-NLS-2$
                    // // ==================== 23-9. 攻撃条件複雑さ ====================
                    // FileUtils.writeLines(file, Main.FILE_ENCODING,
                    // Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.access_complexity"), vuln.getAccess_complexity())), //$NON-NLS-1$
                    // //$NON-NLS-2$
                    // true);
                    // }
                    // }

                    csvList.add(csvLineList);
                    libIdx++;
                    sub1_1Monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                sub1_1Monitor.done();
                appIdx++;
            }
            monitor.subTask(""); //$NON-NLS-1$
            sub1Monitor.done();
        } catch (OperationCanceledException oce) {
            throw new InvocationTargetException(new OperationCanceledException(Messages.getString("libgetwithprogress.progress.canceled"))); //$NON-NLS-1$
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (this.timer != null) {
                this.timer.cancel();
            }
        }

        // ========== CSV出力 ==========
        monitor.setTaskName(Messages.getString("libgetwithprogress.progress.output.csv")); //$NON-NLS-1$
        Thread.sleep(500);
        SubMonitor sub2Monitor = subMonitor.split(20).setWorkRemaining(csvList.size());
        String filePath = timestamp + ".csv"; //$NON-NLS-1$
        if (includeStackTraceChk) {
            filePath = timestamp + System.getProperty("file.separator") + timestamp + ".csv"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        String csv_encoding = Main.CSV_WIN_ENCODING;
        if (OS.isFamilyMac()) {
            csv_encoding = Main.CSV_MAC_ENCODING;
        }
        filePath = this.outDirPath + System.getProperty("file.separator") + filePath; //$NON-NLS-1$
        File dir = new File(new File(filePath).getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (this.ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_SCANRESULT)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (ScanResultCSVColumn csvColumn : columnList) {
                    if (csvColumn.isValid()) {
                        csvHeaderList.add(csvColumn.getColumn().getCulumn());
                    }
                }
                if (includeStackTraceChk) {
                    csvHeaderList.add(Messages.getString("libgetwithprogress.detail.column.title")); //$NON-NLS-1$
                }
                printer.printRecord(csvHeaderList);
            }
            for (List<String> csvLine : csvList) {
                printer.printRecord(csvLine);
                sub2Monitor.worked(1);
                Thread.sleep(10);
            }
            sub2Monitor.done();
        } catch (OperationCanceledException oce) {
            throw new InvocationTargetException(new OperationCanceledException(Messages.getString("libgetwithprogress.progress.canceled"))); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
        }
        monitor.done();
    }
}
