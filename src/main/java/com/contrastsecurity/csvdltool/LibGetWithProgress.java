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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
    private PreferenceStore preferenceStore;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private boolean isOnlyHasCVE;
    private boolean isIncludeCVEDetail;

    Logger logger = Logger.getLogger("csvdltool");

    public LibGetWithProgress(Shell shell, PreferenceStore preferenceStore, List<String> dstApps, Map<String, AppInfo> fullAppMap, boolean isOnlyHasCVE,
            boolean isIncludeCVEDetail) {
        this.shell = shell;
        this.preferenceStore = preferenceStore;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.isOnlyHasCVE = isOnlyHasCVE;
        this.isIncludeCVEDetail = isIncludeCVEDetail;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName("ライブラリ情報の取得を開始しています...");
        monitor.beginTask("ライブラリ情報の取得を開始しています...", 100);
        String filter = "ALL";
        if (isOnlyHasCVE) {
            filter = "VULNERABLE";
        }
        String csvFileFormat = preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_LIB);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_LIB);
        }
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        int sleepTrace = preferenceStore.getInt(PreferenceConstants.SLEEP_LIB);
        String columnJsonStr = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_LIB);
        List<LibCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<LibCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(shell, "脆弱性出力項目の読み込み", String.format("脆弱性出力項目の内容に問題があります。\r\n%s", columnJsonStr));
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
                Path dir = Paths.get(timestamp);
                Files.createDirectory(dir);
            }
            // 選択済みアプリの脆弱性情報を取得
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 80);
            sub1Monitor.beginTask("", dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization organization = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("[%s] %s (%d/%d)", organization.getName(), appName, appIdx, dstApps.size()));
                List<Library> allLibraries = new ArrayList<Library>();
                Api librariesApi = new LibrariesApi(preferenceStore, organization, appId, filter, allLibraries.size());
                allLibraries.addAll((List<Library>) librariesApi.get());
                int totalCount = librariesApi.getTotalCount();
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > allLibraries.size();
                while (incompleteFlg) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    librariesApi = new LibrariesApi(preferenceStore, organization, appId, filter, allLibraries.size());
                    allLibraries.addAll((List<Library>) librariesApi.get());
                    incompleteFlg = totalCount > allLibraries.size();
                    Thread.sleep(sleepTrace);
                }
                SubProgressMonitor sub1_1Monitor = new SubProgressMonitor(sub1Monitor, 1);
                sub1_1Monitor.beginTask("", allLibraries.size());
                for (Library library : allLibraries) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(library.getFile_name());
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
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (String license : library.getLicenses()) {
                                    sj.add(license);
                                }
                                csvLineList.add(sj.toString());
                                break;

                            }
                            case LIB_11: {
                                // ==================== 11. 関連アプリケーション ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (Application app : library.getApps()) {
                                    sj.add(app.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_12: {
                                // ==================== 12. 関連サーバ ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (Server server : library.getServers()) {
                                    sj.add(server.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_13: {
                                // ==================== 13. CVE ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (Vuln vuln : library.getVulns()) {
                                    sj.add(vuln.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_14: {
                                // ==================== 14. ライブラリタグ ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
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
                                csvLineList.add(organization.getName());
                                break;
                            case LIB_16:
                                // ==================== 16. 組織ID ====================
                                csvLineList.add(organization.getOrganization_uuid());
                                break;
                            case LIB_17: {
                                // ==================== 17. ライブラリへのリンク ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", preferenceStore.getString(PreferenceConstants.CONTRAST_URL),
                                            organization.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(link);
                                } else {
                                    csvLineList.add("-");
                                }
                                break;
                            }
                            case LIB_18: {
                                // ==================== 18. ライブラリへのリンク（ハイパーリンク） ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", preferenceStore.getString(PreferenceConstants.CONTRAST_URL),
                                            organization.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(String.format("=HYPERLINK(\"%s\",\"TeamServerへ\")", link));
                                } else {
                                    csvLineList.add("-");
                                }
                                break;
                            }
                            default:
                                continue;
                        }
                    }
                    if (isIncludeCVEDetail && !library.getVulns().isEmpty()) {
                        // ==================== 14. 詳細 ====================
                        if (OS.isFamilyWindows()) {
                            csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", library.getHash(), library.getHash()));
                        } else {
                            csvLineList.add(String.format("=HYPERLINK(\"%s.txt\",\"%s\")", library.getHash(), library.getHash()));
                        }
                        String textFileName = String.format("%s\\%s.txt", timestamp, library.getHash());
                        if (OS.isFamilyMac()) {
                            textFileName = String.format("%s/%s.txt", timestamp, library.getHash());
                        }
                        File file = new File(textFileName);
                        for (Vuln vuln : library.getVulns()) {
                            // ==================== 14-1. タイトル ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(
                                            String.format("=============== %s(CVSS %s) %s ===============", vuln.getName(), vuln.getSeverity_value(), vuln.getSeverity_code())),
                                    true);
                            // ==================== 14-2. 説明 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(vuln.getDescription()), true);
                            // ==================== 14-3. 機密性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("機密性への影響: %s", vuln.getConfidentiality_impact())), true);
                            // ==================== 14-4. 完全性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("完全性への影響: %s", vuln.getIntegrity_impact())), true);
                            // ==================== 14-5. 可用性への影響 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("可用性への影響: %s", vuln.getAvailability_impact())), true);
                            // ==================== 14-6. 攻撃前の認証要否 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("攻撃前の認証要否: %s", vuln.getAuthentication())), true);
                            // ==================== 14-7. 攻撃元区分 ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("攻撃元区分: %s", vuln.getAccess_vector())), true);
                            // ==================== 14-8. 攻撃条件複雑さ ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("攻撃条件複雑さ: %s", vuln.getAccess_complexity())), true);
                        }
                    }

                    csvList.add(csvLineList);
                    sub1_1Monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                sub1_1Monitor.done();
                appIdx++;
            }
            monitor.subTask("");
            sub1Monitor.done();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }

        // ========== CSV出力 ==========
        monitor.setTaskName("CSV出力");
        Thread.sleep(500);
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 20);
        sub2Monitor.beginTask("", csvList.size());
        String filePath = timestamp + ".csv";
        if (isIncludeCVEDetail) {
            filePath = timestamp + "\\" + timestamp + ".csv";
            if (OS.isFamilyMac()) {
                filePath = timestamp + "/" + timestamp + ".csv";
            }
        }
        String csv_encoding = Main.CSV_WIN_ENCODING;
        if (OS.isFamilyMac()) {
            csv_encoding = Main.CSV_MAC_ENCODING;
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (LibCSVColumn csvColumn : columnList) {
                    if (csvColumn.isValid()) {
                        csvHeaderList.add(csvColumn.getColumn().getCulumn());
                    }
                }
                if (isIncludeCVEDetail) {
                    csvHeaderList.add("詳細");
                }
                printer.printRecord(csvHeaderList);
            }
            for (List<String> csvLine : csvList) {
                printer.printRecord(csvLine);
                sub2Monitor.worked(1);
                Thread.sleep(10);
            }
            sub2Monitor.done();
        } catch (IOException e) {
            e.printStackTrace();
        }
        monitor.done();
    }
}
