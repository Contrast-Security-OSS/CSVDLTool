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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private PreferenceStore ps;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private boolean isOnlyHasCVE;
    private boolean isIncludeCVEDetail;

    Logger logger = LogManager.getLogger("csvdltool");

    public LibGetWithProgress(Shell shell, PreferenceStore ps, List<String> dstApps, Map<String, AppInfo> fullAppMap, boolean isOnlyHasCVE, boolean isIncludeCVEDetail) {
        this.shell = shell;
        this.ps = ps;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.isOnlyHasCVE = isOnlyHasCVE;
        this.isIncludeCVEDetail = isIncludeCVEDetail;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName("??????????????????????????????????????????????????????...");
        monitor.beginTask("??????????????????????????????????????????????????????...", 100);
        String filter = "ALL";
        if (isOnlyHasCVE) {
            filter = "VULNERABLE";
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
                MessageDialog.openError(this.shell, "????????????????????????????????????????????????", String.format("?????????????????????????????????????????????????????????????????????\r\n%s", columnJsonStr));
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
            // ?????????????????????????????????????????????????????????????????????????????????????????????
            if (this.isIncludeCVEDetail) {
                String dirPath = timestamp;
                if (OS.isFamilyMac()) {
                    if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                        dirPath = "../../../" + timestamp;
                    }
                }
                Path dir = Paths.get(dirPath);
                Files.createDirectory(dir);
            }
            // ????????????????????????????????????????????????
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 80);
            sub1Monitor.beginTask("", dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("[%s] %s (%d/%d)", org.getName(), appName, appIdx, dstApps.size()));
                List<Library> allLibraries = new ArrayList<Library>();
                Api librariesApi = new LibrariesApi(this.shell, this.ps, org, appId, filter, allLibraries.size());
                allLibraries.addAll((List<Library>) librariesApi.get());
                int totalCount = librariesApi.getTotalCount();
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > allLibraries.size();
                while (incompleteFlg) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("?????????????????????????????????");
                    }
                    librariesApi = new LibrariesApi(this.shell, this.ps, org, appId, filter, allLibraries.size());
                    allLibraries.addAll((List<Library>) librariesApi.get());
                    incompleteFlg = totalCount > allLibraries.size();
                    Thread.sleep(sleepTrace);
                }
                SubProgressMonitor sub1_1Monitor = new SubProgressMonitor(sub1Monitor, 1);
                sub1_1Monitor.beginTask("", allLibraries.size());
                for (Library library : allLibraries) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("?????????????????????????????????");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    monitor.subTask(library.getFile_name());
                    for (LibCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case LIB_01:
                                // ==================== 01. ?????????????????? ====================
                                csvLineList.add(library.getFile_name());
                                break;
                            case LIB_02:
                                // ==================== 02. ?????? ====================
                                csvLineList.add(library.getApp_language());
                                break;
                            case LIB_03:
                                // ==================== 03. ????????????????????? ====================
                                csvLineList.add(library.getVersion());
                                break;
                            case LIB_04:
                                // ==================== 04. ??????????????? ====================
                                csvLineList.add(library.getRelease_date());
                                break;
                            case LIB_05:
                                // ==================== 05. ????????????????????? ====================
                                csvLineList.add(library.getLatest_version());
                                break;
                            case LIB_06:
                                // ==================== 06. ??????????????? ====================
                                csvLineList.add(library.getLatest_release_date());
                                break;
                            case LIB_07:
                                // ==================== 07. ????????? ====================
                                csvLineList.add(library.getGrade());
                                break;
                            case LIB_08:
                                // ==================== 08. ?????????????????? ====================
                                csvLineList.add(String.valueOf(library.getClasses_used()));
                                break;
                            case LIB_09:
                                // ==================== 09. ?????????????????? ====================
                                csvLineList.add(String.valueOf(library.getClass_count()));
                                break;
                            case LIB_10: {
                                // ==================== 10. ??????????????? ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (String license : library.getLicenses()) {
                                    sj.add(license);
                                }
                                csvLineList.add(sj.toString());
                                break;

                            }
                            case LIB_11: {
                                // ==================== 11. ?????????????????????????????? ====================
                                StringJoiner sj = new StringJoiner(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"));
                                for (Application app : library.getApps()) {
                                    sj.add(app.getName());
                                }
                                csvLineList.add(sj.toString());
                                break;
                            }
                            case LIB_12: {
                                // ==================== 12. ??????????????? ====================
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
                                // ==================== 14. ????????????????????? ====================
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
                                // ==================== 15. ????????? ====================
                                csvLineList.add(org.getName());
                                break;
                            case LIB_16:
                                // ==================== 16. ??????ID ====================
                                csvLineList.add(org.getOrganization_uuid());
                                break;
                            case LIB_17: {
                                // ==================== 17. ?????????????????????????????? ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                            org.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(link);
                                } else {
                                    csvLineList.add("-");
                                }
                                break;
                            }
                            case LIB_18: {
                                // ==================== 18. ????????????????????????????????????????????????????????? ====================
                                String languageCode = library.getLanguageCode();
                                if (languageCode != null) {
                                    String link = String.format("%s/static/ng/index.html#/%s/libraries/%s/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                            org.getOrganization_uuid(), languageCode, library.getHash());
                                    csvLineList.add(String.format("=HYPERLINK(\"%s\",\"TeamServer???\")", link));
                                } else {
                                    csvLineList.add("-");
                                }
                                break;
                            }
                            case LIB_19: {
                                // ==================== 19. ?????????????????????????????? ====================
                                boolean restricted = library.isRestricted();
                                if (restricted) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            case LIB_20: {
                                // ==================== 20. ?????????????????????????????? ====================
                                boolean invalid_version = library.isInvalid_version();
                                if (invalid_version) {
                                    csvLineList.add(csvColumn.getTrueStr());
                                } else {
                                    csvLineList.add(csvColumn.getFalseStr());
                                }
                                break;
                            }
                            case LIB_21: {
                                // ==================== 21. ?????????????????????????????? ====================
                                boolean licenseViolation = library.isLicenseViolation();
                                if (licenseViolation) {
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
                        // ==================== 14. ?????? ====================
                        if (OS.isFamilyWindows()) {
                            csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", library.getHash(), library.getHash()));
                        } else {
                            csvLineList.add(String.format("=HYPERLINK(\"%s.txt\",\"%s\")", library.getHash(), library.getHash()));
                        }
                        String textFileName = String.format("%s\\%s.txt", timestamp, library.getHash());
                        if (OS.isFamilyMac()) {
                            textFileName = String.format("%s/%s.txt", timestamp, library.getHash());
                            if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                                textFileName = String.format("../../../%s/%s.txt", timestamp, library.getHash());
                            }
                        }
                        File file = new File(textFileName);
                        for (Vuln vuln : library.getVulns()) {
                            // ==================== 14-1. ???????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING,
                                    Arrays.asList(
                                            String.format("=============== %s(CVSS %s) %s ===============", vuln.getName(), vuln.getSeverity_value(), vuln.getSeverity_code())),
                                    true);
                            // ==================== 14-2. ?????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(vuln.getDescription()), true);
                            // ==================== 14-3. ????????????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("?????????????????????: %s", vuln.getConfidentiality_impact())), true);
                            // ==================== 14-4. ????????????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("?????????????????????: %s", vuln.getIntegrity_impact())), true);
                            // ==================== 14-5. ????????????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("?????????????????????: %s", vuln.getAvailability_impact())), true);
                            // ==================== 14-6. ???????????????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("????????????????????????: %s", vuln.getAuthentication())), true);
                            // ==================== 14-7. ??????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("???????????????: %s", vuln.getAccess_vector())), true);
                            // ==================== 14-8. ????????????????????? ====================
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(String.format("?????????????????????: %s", vuln.getAccess_complexity())), true);
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

        // ========== CSV?????? ==========
        monitor.setTaskName("CSV??????");
        Thread.sleep(500);
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 20);
        sub2Monitor.beginTask("", csvList.size());
        String filePath = timestamp + ".csv";
        if (OS.isFamilyMac()) {
            if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                filePath = "../../../" + timestamp + ".csv";
            }
        }
        if (isIncludeCVEDetail) {
            filePath = timestamp + "\\" + timestamp + ".csv";
            if (OS.isFamilyMac()) {
                filePath = timestamp + "/" + timestamp + ".csv";
                if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                    filePath = "../../../" + timestamp + "/" + timestamp + ".csv";
                }
            }
        }
        String csv_encoding = Main.CSV_WIN_ENCODING;
        if (OS.isFamilyMac()) {
            csv_encoding = Main.CSV_MAC_ENCODING;
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
                    csvHeaderList.add("??????");
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
