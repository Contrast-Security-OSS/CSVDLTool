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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.api.LibrariesApi;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.Library;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.Vuln;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class LibGetWithProgress implements IRunnableWithProgress {

    private static final String CSV_ENCODING = "Shift_JIS";
    private static final String FILE_ENCODING = "UTF-8";

    private static final String ROUTE = "==================== ルート ====================";
    private static final String HTTP_INFO = "==================== HTTP情報 ====================";
    private static final String WHAT_HAPPEN = "==================== 何が起こったか？ ====================";
    private static final String RISK = "==================== どんなリスクであるか？ ====================";
    private static final String HOWTOFIX = "==================== 修正方法 ====================";
    private static final String COMMENT = "==================== コメント ====================";

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
        String csvColumns = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_LIB);
        String csvSepLicense = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_LICENSE).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepApplication = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_RELATED_APPLICATION).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepServer = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_RELATED_SERVER).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepCVE = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_CVE).replace("\\r", "\r").replace("\\n", "\n");
        Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // 長文情報（何が起こったか？など）を出力する場合はフォルダに出力
            if (this.isIncludeCVEDetail) {
                Path dir = Paths.get(timestamp);
                Files.createDirectory(dir);
            }
            // アプリケーショングループの情報を取得
            Api groupsApi = new GroupsApi(preferenceStore);
            List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
            monitor.beginTask("アプリケーショングループの情報を取得", customGroups.size());
            for (CustomGroup customGroup : customGroups) {
                List<ApplicationInCustomGroup> apps = customGroup.getApplications();
                if (apps != null) {
                    for (ApplicationInCustomGroup app : apps) {
                        String appName = app.getApplication().getName();
                        if (appGroupMap.containsKey(appName)) {
                            appGroupMap.get(appName).add(customGroup.getName());
                        } else {
                            appGroupMap.put(appName, new ArrayList<String>(Arrays.asList(customGroup.getName())));
                        }
                    }
                }
                monitor.worked(1);
            }
            Thread.sleep(1000);
            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName(String.format("脆弱性情報の取得(0/%d)", dstApps.size()));
            int appIdx = 1;
            for (String appLabel : dstApps) {
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                Api librariesApi = new LibrariesApi(preferenceStore, appId, filter);
                List<Library> libraries = (List<Library>) librariesApi.get();
                monitor.beginTask(String.format("ライブラリ情報の取得(%d/%d)", appIdx, dstApps.size()), libraries.size());
                for (Library library : libraries) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(String.format("%s - %s", appName, library.getFile_name()));
                    // ==================== 01. ライブラリ名 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_01.name())) {
                        csvLineList.add(library.getFile_name());
                    }
                    // ==================== 02. 言語 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_02.name())) {
                        csvLineList.add(library.getApp_language());
                    }
                    // ==================== 03. 現在バージョン ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_03.name())) {
                        csvLineList.add(library.getVersion());
                    }
                    // ==================== 04. リリース日 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_04.name())) {
                        csvLineList.add(library.getRelease_date());
                    }
                    // ==================== 05. 最新バージョン ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_05.name())) {
                        csvLineList.add(library.getLatest_version());
                    }
                    // ==================== 06. リリース日 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_06.name())) {
                        csvLineList.add(library.getLatest_release_date());
                    }
                    // ==================== 07. スコア ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_07.name())) {
                        csvLineList.add(library.getGrade());
                    }
                    // ==================== 08. 使用クラス数 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_08.name())) {
                        csvLineList.add(String.valueOf(library.getClasses_used()));
                    }
                    // ==================== 09. 全体クラス数 ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_09.name())) {
                        csvLineList.add(String.valueOf(library.getClass_count()));
                    }
                    // ==================== 10. ライセンス ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_10.name())) {
                        StringJoiner sj = new StringJoiner(csvSepLicense);
                        for (String license : library.getLicenses()) {
                            sj.add(license);
                        }
                        csvLineList.add(sj.toString());
                    }
                    // ==================== 11. 関連アプリケーション ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_11.name())) {
                        StringJoiner sj = new StringJoiner(csvSepApplication);
                        for (Application app : library.getApps()) {
                            sj.add(app.getName());
                        }
                        csvLineList.add(sj.toString());
                    }
                    // ==================== 12. 関連サーバ ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_12.name())) {
                        StringJoiner sj = new StringJoiner(csvSepServer);
                        for (Server server : library.getServers()) {
                            sj.add(server.getName());
                        }
                        csvLineList.add(sj.toString());
                    }
                    // ==================== 13. CVE ====================
                    if (csvColumns.contains(LibCSVColmunEnum.LIB_13.name())) {
                        StringJoiner sj = new StringJoiner(csvSepCVE);
                        for (Vuln vuln : library.getVulns()) {
                            sj.add(vuln.getName());
                        }
                        csvLineList.add(sj.toString());
                    }
                    if (isIncludeCVEDetail) {
                        // ==================== 14. 詳細 ====================
                        csvLineList.add(library.getHash());
                        csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", library.getHash(), library.getHash()));
                        String textFileName = String.format("%s\\%s.txt", timestamp, library.getHash());
                        File file = new File(textFileName);

                        // ==================== 14-1. CVE ====================
                        FileUtils.writeLines(file, FILE_ENCODING, Arrays.asList(HTTP_INFO, library.getHash()), true);
                    }

                    csvList.add(csvLineList);
                    monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                appIdx++;
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }

        // ========== CSV出力 ==========
        monitor.beginTask("CSV出力", csvList.size());
        String filePath = timestamp + ".csv";
        if (isIncludeCVEDetail) {
            filePath = timestamp + "\\output.csv";
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), CSV_ENCODING))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_LIB)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (String csvColumn : csvColumns.split(",")) {
                    csvHeaderList.add(LibCSVColmunEnum.valueOf(csvColumn.trim()).getCulumn());
                }
                if (isIncludeCVEDetail) {
                    csvHeaderList.add("詳細");
                }
                printer.printRecord(csvHeaderList);
            }
            for (List<String> csvLine : csvList) {
                printer.printRecord(csvLine);
                monitor.worked(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        monitor.done();
    }
}
