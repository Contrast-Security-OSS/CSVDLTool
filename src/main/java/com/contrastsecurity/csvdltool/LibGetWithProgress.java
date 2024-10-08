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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
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
import com.contrastsecurity.csvdltool.api.LibrariesApi;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.LibCSVColumn;
import com.contrastsecurity.csvdltool.model.Library;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.Vuln;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class LibGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private String outDirPath;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private boolean isOnlyHasCVE;
    private boolean isWithCVSS;
    private boolean isWithEPSS;
    private boolean isIncludeCVEDetail;
    private Timer timer;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public LibGetWithProgress(Shell shell, PreferenceStore ps, String outDirPath, List<String> dstApps, Map<String, AppInfo> fullAppMap, boolean isOnlyHasCVE, boolean isWithCVSS,
            boolean isWithEPSS, boolean isIncludeCVEDetail) {
        this.shell = shell;
        this.ps = ps;
        this.outDirPath = outDirPath;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.isOnlyHasCVE = isOnlyHasCVE;
        this.isWithCVSS = isWithCVSS;
        this.isWithEPSS = isWithEPSS;
        this.isIncludeCVEDetail = isIncludeCVEDetail;
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
        String filter = "ALL"; //$NON-NLS-1$
        if (isOnlyHasCVE) {
            filter = "VULNERABLE"; //$NON-NLS-1$
        }
        String csvFileFormat = this.ps.getString(PreferenceConstants.CSV_FILE_FORMAT_LIB);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = this.ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB);
        }
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        int sleepTrace = this.ps.getInt(PreferenceConstants.SLEEP_LIB);
        String columnJsonStr = this.ps.getString(PreferenceConstants.CSV_COLUMN_LIB);
        List<LibCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<LibCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(this.shell, Messages.getString("libgetwithprogress.message.dialog.title"), //$NON-NLS-1$
                        String.format("%s\r\n%s", Messages.getString("libgetwithprogress.message.dialog.json.load.error.message"), columnJsonStr)); //$NON-NLS-1$ //$NON-NLS-2$
                columnList = new ArrayList<LibCSVColumn>();
            }
        } else {
            columnList = new ArrayList<LibCSVColumn>();
            for (LibCSVColmunEnum colEnum : LibCSVColmunEnum.sortedValues()) {
                columnList.add(new LibCSVColumn(colEnum));
            }
        }
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // 長文情報（何が起こったか？など）を出力する場合はフォルダに出力
            if (this.isIncludeCVEDetail) {
                String dirPath = this.outDirPath + System.getProperty("file.separator") + timestamp; //$NON-NLS-1$
                Path dir = Paths.get(dirPath);
                Files.createDirectory(dir);
            }
            // 選択済みアプリのライブラリ情報を取得
            monitor.setTaskName(Messages.getString("libgetwithprogress.progress.loading.libraries")); //$NON-NLS-1$
            SubMonitor sub1Monitor = subMonitor.split(80).setWorkRemaining(dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(
                        String.format("%s[%s] %s (%d/%d)", Messages.getString("libgetwithprogress.progress.loading.libraries"), org.getName(), appName, appIdx, dstApps.size())); //$NON-NLS-1$ //$NON-NLS-2$
                List<Library> allLibraries = new ArrayList<Library>();
                Api librariesApi = new LibrariesApi(this.shell, this.ps, org, appId, filter, allLibraries.size());
                allLibraries.addAll((List<Library>) librariesApi.get());
                int totalCount = librariesApi.getTotalCount();
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > allLibraries.size();
                while (incompleteFlg) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new OperationCanceledException();
                    }
                    librariesApi = new LibrariesApi(this.shell, this.ps, org, appId, filter, allLibraries.size());
                    allLibraries.addAll((List<Library>) librariesApi.get());
                    incompleteFlg = totalCount > allLibraries.size();
                    Thread.sleep(sleepTrace);
                }
                SubMonitor sub1_1Monitor = sub1Monitor.split(1).setWorkRemaining(allLibraries.size());
                int libIdx = 1;
                for (Library library : allLibraries) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new OperationCanceledException();
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(
                            String.format("%s %s (%d/%d)", Messages.getString("libgetwithprogress.progress.loading.library"), library.getFile_name(), libIdx, allLibraries.size())); //$NON-NLS-1$ //$NON-NLS-2$
                    for (LibCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case LIB_01:
                                // ==================== 01. ライブラリ名 ====================
                                csvLineList.add(library.getFile_name());
                                break;
                            case LIB_02:
                                // ==================== 02. 言語 ====================
                                csvLineList.add(library.getApp_language());
                                break;
                            case LIB_03:
                                // ==================== 03. 現在バージョン ====================
                                csvLineList.add(library.getVersion());
                                break;
                            case LIB_04:
                                // ==================== 04. リリース日 ====================
                                csvLineList.add(library.getRelease_date());
                                break;
                            case LIB_05:
                                // ==================== 05. 最新バージョン ====================
                                csvLineList.add(library.getLatest_version());
                                break;
                            case LIB_06:
                                // ==================== 06. リリース日 ====================
                                csvLineList.add(library.getLatest_release_date());
                                break;
                            case LIB_07:
                                // ==================== 07. スコア ====================
                                csvLineList.add(library.getGrade());
                                break;
                            case LIB_08:
                                // ==================== 08. 使用クラス数 ====================
                                csvLineList.add(String.valueOf(library.getClasses_used()));
                                break;
                            case LIB_09:
                                // ==================== 09. 全体クラス数 ====================
                                csvLineList.add(String.valueOf(library.getClass_count()));
                                break;
                            case LIB_10: {
                                // ==================== 10. ライセンス ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (String license : library.getLicenses()) {
                                    sj.add(license);
                                }
                                csvLineList.add(sj.toString());
                                break;

                            }
                            case LIB_11: {
                                // ==================== 11. 関連アプリケーション ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (Application app : library.getApps()) {
                                    sj.add(app.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_12: {
                                // ==================== 12. 関連サーバ ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (Server server : library.getServers()) {
                                    sj.add(server.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_13: {
                                // ==================== 13. CVE ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                for (Vuln vuln : library.getVulns()) {
                                    StringBuilder vulnBuffer = new StringBuilder();
                                    vulnBuffer.append(vuln.getName());
                                    if (isWithCVSS) {
                                        String withCVSS = "[CVSS -]";
                                        if (vuln.isHas_cvss3_score()) {
                                            withCVSS = String.format("[CVSS %.1f]", vuln.getCvss_3_severity_value());
                                        }
                                        vulnBuffer.append(withCVSS);
                                    }
                                    if (isWithEPSS) {
                                        String cisaStr = vuln.isCisa() ? Messages.getString("libgetwithprogress.cisa") : ""; //$NON-NLS-1$ //$NON-NLS-2$
                                        String withEPSS = String.format("[EPSS %.2f (%.2f%s) %s]", vuln.getEpss_score(), vuln.getEpss_percentile(), //$NON-NLS-1$
                                                Messages.getString("libgetwithprogress.percentile"), cisaStr); //$NON-NLS-1$
                                        vulnBuffer.append(withEPSS);
                                    }
                                    sj.add(vulnBuffer.toString());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_14: {
                                // ==================== 14. ライブラリタグ ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                if (library.getTags() != null) {
                                    for (String tag : library.getTags()) {
                                        sj.add(tag);
                                    }
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_15:
                                // ==================== 15. 組織名 ====================
                                csvLineList.add(org.getName());
                                break;
                            case LIB_16:
                                // ==================== 16. 組織ID ====================
                                csvLineList.add(org.getOrganization_uuid());
                                break;
                            case LIB_17: {
                                // ==================== 17. ライブラリへのリンク ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                            org.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(link);
                                } else {
                                    csvLineList.add("-"); //$NON-NLS-1$
                                }
                                break;
                            }
                            case LIB_18: {
                                // ==================== 18. ライブラリへのリンク（ハイパーリンク） ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL), //$NON-NLS-1$
                                            org.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(String.format("=HYPERLINK(\"%s\",\"%s\")", link, Messages.getString("libgetwithprogress.to.teamserver.hyperlink.text"))); //$NON-NLS-1$ //$NON-NLS-2$
                                } else {
                                    csvLineList.add("-"); //$NON-NLS-1$
                                }
                                break;
                            }
                            case LIB_19: {
                                // ==================== 19. ライブラリ制限に抵触 ====================
                                boolean restricted = library.isRestricted();
                                if (restricted) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            case LIB_20: {
                                // ==================== 20. バージョン要件に抵触 ====================
                                boolean invalid_version = library.isInvalid_version();
                                if (invalid_version) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            case LIB_21: {
                                // ==================== 21. ライセンス制限に抵触 ====================
                                boolean licenseViolation = library.isLicenseViolation();
                                if (licenseViolation) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            case LIB_22: {
                                // ==================== 22. 既知の悪用された脆弱性の有無 ====================
                                if (library.hasKEV()) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            default:
                                continue;
                        }
                    }
                    if (isIncludeCVEDetail && !library.getVulns().isEmpty()) {
                        // ==================== 23. 詳細 ====================
                        if (OS.isFamilyWindows()) {
                            csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", library.getHash(), library.getHash())); //$NON-NLS-1$
                        } else {
                            csvLineList.add(String.format("=HYPERLINK(\"%s.txt\",\"%s\")", library.getHash(), library.getHash())); //$NON-NLS-1$
                        }
                        String textFileName = String.format("%s%s%s.txt", timestamp, System.getProperty("file.separator"), library.getHash()); //$NON-NLS-1$ //$NON-NLS-2$
                        textFileName = this.outDirPath + System.getProperty("file.separator") + textFileName; //$NON-NLS-1$
                        File file = new File(textFileName);
                        for (Vuln vuln : library.getVulns()) {
                            // ==================== 23-1. タイトル ====================
                            if (vuln.isHas_cvss3_score()) {
                                String cvss3Ver = vuln.getCvss_3_vector().split(":")[1].replace("/AV", "");
                                FileUtils.writeLines(file, Main.FILE_ENCODING,
                                        Arrays.asList(String.format("=============== %s(CVSS%s %s) %s ===============", vuln.getName(), cvss3Ver, vuln.getCvss_3_severity_value(), //$NON-NLS-1$
                                                vuln.getCvss_3_severity_code())),
                                        true);
                            } else {
                                FileUtils.writeLines(file, Main.FILE_ENCODING,
                                        Arrays.asList(
                                                String.format("=============== %s(CVSS %s) %s ===============", vuln.getName(), vuln.getSeverity_value(), vuln.getSeverity_code())), //$NON-NLS-1$
                                        true);
                            }
                            // ==================== 23-2. 説明 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(vuln.getDescription()), true);
                            // ==================== 23-3. EPSS ====================
                            String cisaStr = vuln.isCisa() ? Messages.getString("libgetwithprogress.cisa") : ""; //$NON-NLS-1$ //$NON-NLS-2$
                            String epss = String.format("%.2f (%.2f%s) %s", vuln.getEpss_score(), vuln.getEpss_percentile(), Messages.getString("libgetwithprogress.percentile"), //$NON-NLS-1$ //$NON-NLS-2$
                                    cisaStr);
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.epss"), epss)), //$NON-NLS-1$ //$NON-NLS-2$
                                    true);
                            // ==================== 23-4. 機密性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(
                                    String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.confidentiality-impact"), vuln.getConfidentiality_impact())), true); //$NON-NLS-1$ //$NON-NLS-2$
                            // ==================== 23-5. 完全性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.integrity_impact"), vuln.getIntegrity_impact())), //$NON-NLS-1$ //$NON-NLS-2$
                                    true);
                            // ==================== 23-6. 可用性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(
                                            String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.availability_impact"), vuln.getAvailability_impact())), //$NON-NLS-1$ //$NON-NLS-2$
                                    true);
                            // ==================== 23-7. 攻撃前の認証要否 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.authentication"), vuln.getAuthentication())), true); //$NON-NLS-1$ //$NON-NLS-2$
                            // ==================== 23-8. 攻撃元区分 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.access_vector"), vuln.getAccess_vector())), true); //$NON-NLS-1$ //$NON-NLS-2$
                            // ==================== 23-9. 攻撃条件複雑さ ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(String.format("%s %s", Messages.getString("libgetwithprogress.detail.header.access_complexity"), vuln.getAccess_complexity())), //$NON-NLS-1$ //$NON-NLS-2$
                                    true);
                        }
                    }
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
        if (isIncludeCVEDetail) {
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
            if (this.ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (LibCSVColumn csvColumn : columnList) {
                    if (csvColumn.isValid()) {
                        csvHeaderList.add(csvColumn.getColumn().getCulumn());
                    }
                }
                if (isIncludeCVEDetail) {
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
