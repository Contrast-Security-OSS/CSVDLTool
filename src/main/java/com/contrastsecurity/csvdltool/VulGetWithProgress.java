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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ApplicationTagsApi;
import com.contrastsecurity.csvdltool.api.EventDetailApi;
import com.contrastsecurity.csvdltool.api.EventSummaryApi;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.api.HowToFixApi;
import com.contrastsecurity.csvdltool.api.HttpRequestApi;
import com.contrastsecurity.csvdltool.api.RoutesApi;
import com.contrastsecurity.csvdltool.api.StoryApi;
import com.contrastsecurity.csvdltool.api.TraceApi;
import com.contrastsecurity.csvdltool.api.TraceTagsApi;
import com.contrastsecurity.csvdltool.api.TracesApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.json.HowToFixJson;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.Chapter;
import com.contrastsecurity.csvdltool.model.CollapsedEventSummary;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.EventDetail;
import com.contrastsecurity.csvdltool.model.EventSummary;
import com.contrastsecurity.csvdltool.model.HttpRequest;
import com.contrastsecurity.csvdltool.model.Note;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Property;
import com.contrastsecurity.csvdltool.model.Recommendation;
import com.contrastsecurity.csvdltool.model.Risk;
import com.contrastsecurity.csvdltool.model.Route;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.Story;
import com.contrastsecurity.csvdltool.model.Trace;
import com.contrastsecurity.csvdltool.model.VulCSVColumn;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class VulGetWithProgress implements IRunnableWithProgress {

    private static final String CSV_ENCODING = "Shift_JIS";
    private static final String FILE_ENCODING = "UTF-8";

    private static final String ROUTE = "==================== ルート ====================";
    private static final String HTTP_INFO = "==================== HTTP情報 ====================";
    private static final String WHAT_HAPPEN = "==================== 何が起こったか？ ====================";
    private static final String RISK = "==================== どんなリスクであるか？ ====================";
    private static final String HOWTOFIX = "==================== 修正方法 ====================";
    private static final String COMMENT = "==================== コメント ====================";
    private static final String STACK_TRACE = "==================== 詳細 ====================";

    private Shell shell;
    private PreferenceStore preferenceStore;
    private Organization organization;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private boolean isOnlyParentApp;
    private boolean isIncludeDesc;
    private boolean isIncludeStackTrace;

    Logger logger = Logger.getLogger("csvdltool");

    public VulGetWithProgress(Shell shell, PreferenceStore preferenceStore, Organization organization, List<String> dstApps, Map<String, AppInfo> fullAppMap,
            boolean isOnlyParentApp, boolean isIncludeDesc, boolean isIncludeStackTrace) {
        this.shell = shell;
        this.preferenceStore = preferenceStore;
        this.organization = organization;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.isOnlyParentApp = isOnlyParentApp;
        this.isIncludeDesc = isIncludeDesc;
        this.isIncludeStackTrace = isIncludeStackTrace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName("脆弱性情報の取得を開始しています...");
        String csvFileFormat = preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT_VUL);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL);
        }
        Pattern cwePtn = Pattern.compile("\\/(\\d+)\\.html$");
        Pattern stsPtn = Pattern.compile("^[A-Za-z\\s]+$");
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        int sleepTrace = preferenceStore.getInt(PreferenceConstants.SLEEP_VUL);
        String columnJsonStr = preferenceStore.getString(PreferenceConstants.CSV_COLUMN_VUL);
        List<VulCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<VulCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(shell, "脆弱性出力項目の読み込み", String.format("脆弱性出力項目の内容に問題があります。\r\n%s", columnJsonStr));
                columnList = new ArrayList<VulCSVColumn>();
            }
        } else {
            columnList = new ArrayList<VulCSVColumn>();
            for (VulCSVColmunEnum colEnum : VulCSVColmunEnum.sortedValues()) {
                columnList.add(new VulCSVColumn(colEnum));
            }
        }
        String csvSepTag = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_TAG).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepBuildNo = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_BUILDNO).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepGroup = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_GROUP).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepServer = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_SERVER).replace("\\r", "\r").replace("\\n", "\n");
        Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // 長文情報（何が起こったか？など）を出力する場合はフォルダに出力
            if (this.isIncludeDesc) {
                Path dir = Paths.get(timestamp);
                Files.createDirectory(dir);
            }
            // アプリケーショングループの情報を取得
            Api groupsApi = new GroupsApi(preferenceStore, organization);
            try {
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
            } catch (ApiException ae) {
            }
            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName(String.format("脆弱性情報の取得(0/%d)", dstApps.size()));
            int appIdx = 1;
            for (String appLabel : dstApps) {
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                Api tracesApi = new TracesApi(preferenceStore, organization, appId);
                List<String> traces = (List<String>) tracesApi.get();
                monitor.beginTask(String.format("脆弱性情報の取得(%d/%d)", appIdx, dstApps.size()), traces.size());
                for (String trace_id : traces) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    Api traceApi = new TraceApi(preferenceStore, organization, appId, trace_id);
                    Trace trace = (Trace) traceApi.get();
                    monitor.subTask(String.format("%s - %s", appName, trace.getTitle()));
                    Application realApp = trace.getApplication();
                    if (isOnlyParentApp) {
                        if (!appName.equals(realApp.getName())) {
                            monitor.worked(1);
                            continue;
                        }
                    }
                    HowToFixJson howToFixJson = null;
                    for (VulCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case VUL_01:
                                csvLineList.add(appName);
                                break;
                            case VUL_02:
                                csvLineList.add(realApp.getName());
                                break;
                            case VUL_03:
                                csvLineList.add(realApp.getApp_id());
                                break;
                            case VUL_04:
                                Api applicationTagsApi = new ApplicationTagsApi(preferenceStore, organization, appId);
                                List<String> applicationTags = (List<String>) applicationTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), applicationTags));
                                break;
                            case VUL_05:
                                csvLineList.add(trace.getCategory_label());
                                break;
                            case VUL_06:
                                csvLineList.add(trace.getRule_title());
                                break;
                            case VUL_07:
                                csvLineList.add(trace.getSeverity_label());
                                break;
                            case VUL_08:
                                Api howToFixApi = new HowToFixApi(preferenceStore, organization, trace_id);
                                try {
                                    howToFixJson = (HowToFixJson) howToFixApi.get();
                                    String cweUrl = howToFixJson.getCwe();
                                    Matcher m = cwePtn.matcher(cweUrl);
                                    if (m.find()) {
                                        csvLineList.add(m.group(1));
                                    } else {
                                        csvLineList.add("");
                                    }
                                } catch (Exception e) {
                                    shell.getDisplay().syncExec(new Runnable() {
                                        public void run() {
                                            if (!MessageDialog.openConfirm(shell, "脆弱性情報の取得", "修正方法、CWE、OWASPの情報を取得する際に例外が発生しました。\r\n例外についてはログでご確認ください。処理を続けますか？")) {
                                                monitor.setCanceled(true);
                                            }
                                        }
                                    });
                                    Recommendation recommendation = new Recommendation();
                                    recommendation.setText("***** 取得に失敗しました。 *****");
                                    howToFixJson = new HowToFixJson();
                                    howToFixJson.setRecommendation(recommendation);
                                    howToFixJson.setCwe("");
                                    howToFixJson.setOwasp("");
                                    csvLineList.add("");
                                }
                                break;
                            case VUL_09:
                                csvLineList.add(trace.getStatus());
                                break;
                            case VUL_10:
                                csvLineList.add(trace.getLanguage());
                                break;
                            case VUL_11:
                                if (appGroupMap.containsKey(appName)) {
                                    csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), appGroupMap.get(appName)));
                                } else {
                                    csvLineList.add("");
                                }
                                break;
                            case VUL_12:
                                csvLineList.add(trace.getTitle());
                                break;
                            case VUL_13:
                                csvLineList.add(trace.getFirst_time_seen());
                                break;
                            case VUL_14:
                                csvLineList.add(trace.getLast_time_seen());
                                break;
                            case VUL_15:
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), trace.getApp_version_tags()));
                                break;
                            case VUL_16:
                                List<String> serverNameList = trace.getServers().stream().map(Server::getName).collect(Collectors.toList());
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), serverNameList));
                                break;
                            case VUL_17:
                                Application app = trace.getApplication();
                                String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                                csvLineList.add(module);
                                break;
                            case VUL_18:
                                Api traceTagsApi = new TraceTagsApi(preferenceStore, organization, trace_id);
                                List<String> traceTags = (List<String>) traceTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), traceTags));
                                break;
                            case VUL_19:
                                csvLineList.add(trace.getPending_status());
                                break;
                            default:
                                continue;
                        }
                    }
                    // // ==================== 01. アプリケーション名 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_01.name())) {
                    // csvLineList.add(appName);
                    // }
                    // // ==================== 02. マージしたときの、各アプリ名称（可能であれば） ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_02.name())) {
                    // csvLineList.add(realApp.getName());
                    // }
                    // // ==================== 03. アプリケーションID ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_03.name())) {
                    // csvLineList.add(realApp.getApp_id());
                    // }
                    // // ==================== 04. アプリケーションタグ ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_04.name())) {
                    // Api applicationTagsApi = new ApplicationTagsApi(preferenceStore, organization, appId);
                    // List<String> applicationTags = (List<String>) applicationTagsApi.get();
                    // csvLineList.add(String.join(csvSepTag, applicationTags));
                    // }
                    // // ==================== 05. （脆弱性の）カテゴリ ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_05.name())) {
                    // csvLineList.add(trace.getCategory_label());
                    // }
                    // // ==================== 06. （脆弱性の）ルール ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_06.name())) {
                    // csvLineList.add(trace.getRule_title());
                    // }
                    // // ==================== 07. 深刻度 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_07.name())) {
                    // csvLineList.add(trace.getSeverity_label());
                    // }
                    // // ==================== 08. CWE ====================
                    // Api howToFixApi = new HowToFixApi(preferenceStore, organization, trace_id);
                    // HowToFixJson howToFixJson = null;
                    // try {
                    // howToFixJson = (HowToFixJson) howToFixApi.get();
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_08.name())) {
                    // String cweUrl = howToFixJson.getCwe();
                    // Matcher m = cwePtn.matcher(cweUrl);
                    // if (m.find()) {
                    // csvLineList.add(m.group(1));
                    // } else {
                    // csvLineList.add("");
                    // }
                    // }
                    // } catch (Exception e) {
                    // shell.getDisplay().syncExec(new Runnable() {
                    // public void run() {
                    // if (!MessageDialog.openConfirm(shell, "脆弱性情報の取得", "修正方法、CWE、OWASPの情報を取得する際に例外が発生しました。\r\n例外についてはログでご確認ください。処理を続けますか？")) {
                    // monitor.setCanceled(true);
                    // }
                    // }
                    // });
                    // Recommendation recommendation = new Recommendation();
                    // recommendation.setText("***** 取得に失敗しました。 *****");
                    // howToFixJson = new HowToFixJson();
                    // howToFixJson.setRecommendation(recommendation);
                    // howToFixJson.setCwe("");
                    // howToFixJson.setOwasp("");
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_08.name())) {
                    // csvLineList.add("");
                    // }
                    // }
                    // // ==================== 09. ステータス ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_09.name())) {
                    // csvLineList.add(trace.getStatus());
                    // }
                    // // ==================== 19. 保留中ステータス ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_19.name())) {
                    // csvLineList.add(trace.getPending_status());
                    // }
                    // // ==================== 10. 言語（Javaなど） ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_10.name())) {
                    // csvLineList.add(trace.getLanguage());
                    // }
                    // // ==================== 11. グループ（アプリケーションのグループ） ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_11.name())) {
                    // if (appGroupMap.containsKey(appName)) {
                    // csvLineList.add(String.join(csvSepGroup, appGroupMap.get(appName)));
                    // } else {
                    // csvLineList.add("");
                    // }
                    // }
                    // // ==================== 12. 脆弱性のタイトル（例：SQLインジェクション：「/api/v1/approvers/」ページのリクエストボディ ） ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_12.name())) {
                    // csvLineList.add(trace.getTitle());
                    // }
                    // // ==================== 13. 最初の検出 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_13.name())) {
                    // csvLineList.add(trace.getFirst_time_seen());
                    // }
                    // // ==================== 14. 最後の検出 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_14.name())) {
                    // csvLineList.add(trace.getLast_time_seen());
                    // }
                    // // ==================== 15. ビルド番号 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_15.name())) {
                    // csvLineList.add(String.join(csvSepBuildNo, trace.getApp_version_tags()));
                    // }
                    // // ==================== 16. 次のサーバにより報告 ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_16.name())) {
                    // List<String> serverNameList = trace.getServers().stream().map(Server::getName).collect(Collectors.toList());
                    // csvLineList.add(String.join(csvSepServer, serverNameList));
                    // }
                    // // ==================== 17. モジュール ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_17.name())) {
                    // Application app = trace.getApplication();
                    // String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                    // csvLineList.add(module);
                    // }
                    // // ==================== 18. 脆弱性タグ ====================
                    // if (csvColumns.contains(VulCSVColmunEnum.VUL_18.name())) {
                    // Api traceTagsApi = new TraceTagsApi(preferenceStore, organization, trace_id);
                    // List<String> traceTags = (List<String>) traceTagsApi.get();
                    // csvLineList.add(String.join(csvSepTag, traceTags));
                    // }
                    if (isIncludeDesc) {
                        // ==================== 19. 詳細（長文データ） ====================
                        csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", trace.getUuid(), trace.getUuid()));
                        String textFileName = String.format("%s\\%s.txt", timestamp, trace.getUuid());
                        File file = new File(textFileName);

                        // ==================== 19-1. ルート ====================
                        Api routesApi = new RoutesApi(preferenceStore, organization, appId, trace_id);
                        List<Route> routes = (List<Route>) routesApi.get();
                        List<String> signatureList = routes.stream().map(Route::getSignature).collect(Collectors.toList());
                        signatureList.add(0, ROUTE);
                        FileUtils.writeLines(file, FILE_ENCODING, signatureList, true);

                        // ==================== 19-2. HTTP情報 ====================
                        Api httpRequestApi = new HttpRequestApi(preferenceStore, organization, trace_id);
                        HttpRequest httpRequest = (HttpRequest) httpRequestApi.get();
                        if (httpRequest != null) {
                            FileUtils.writeLines(file, FILE_ENCODING, Arrays.asList(HTTP_INFO, httpRequest.getText()), true);
                        } else {
                            FileUtils.writeLines(file, FILE_ENCODING, Arrays.asList(HTTP_INFO, "なし"), true);
                        }

                        Api storyApi = new StoryApi(preferenceStore, organization, trace_id);
                        Story story = null;
                        try {
                            story = (Story) storyApi.get();
                        } catch (Exception e) {
                            shell.getDisplay().syncExec(new Runnable() {
                                public void run() {
                                    if (!MessageDialog.openConfirm(shell, "脆弱性情報の取得", "何が起こったか？、どんなリスクであるか？の情報を取得する際に例外が発生しました。\r\n例外についてはログでご確認ください。処理を続けますか？")) {
                                        monitor.setCanceled(true);
                                    }
                                }
                            });
                            Risk risk = new Risk();
                            risk.setText("***** 取得に失敗しました。 *****");
                            story = new Story();
                            story.setRisk(risk);
                            story.setChapters(new ArrayList<Chapter>());
                        }
                        // ==================== 19-3. 何が起こったか？ ====================
                        List<String> chapterLines = new ArrayList<String>();
                        chapterLines.add(WHAT_HAPPEN);
                        for (Chapter chapter : story.getChapters()) {
                            chapterLines.add(chapter.getIntroText());
                            chapterLines.add(chapter.getBody());
                        }
                        FileUtils.writeLines(file, FILE_ENCODING, chapterLines, true);
                        // ==================== 19-4. どんなリスクであるか？ ====================
                        FileUtils.writeLines(file, FILE_ENCODING, Arrays.asList(RISK, story.getRisk().getText()), true);
                        // ==================== 19-5. 修正方法 ====================
                        List<String> howToFixLines = new ArrayList<String>();
                        howToFixLines.add(HOWTOFIX);
                        if (howToFixJson == null) {
                            Api howToFixApi = new HowToFixApi(preferenceStore, organization, trace_id);
                            try {
                                howToFixJson = (HowToFixJson) howToFixApi.get();
                            } catch (Exception e) {
                                shell.getDisplay().syncExec(new Runnable() {
                                    public void run() {
                                        if (!MessageDialog.openConfirm(shell, "脆弱性情報の取得", "修正方法、CWE、OWASPの情報を取得する際に例外が発生しました。\r\n例外についてはログでご確認ください。処理を続けますか？")) {
                                            monitor.setCanceled(true);
                                        }
                                    }
                                });
                                Recommendation recommendation = new Recommendation();
                                recommendation.setText("***** 取得に失敗しました。 *****");
                                howToFixJson = new HowToFixJson();
                                howToFixJson.setRecommendation(recommendation);
                                howToFixJson.setCwe("");
                                howToFixJson.setOwasp("");
                            }
                        }
                        howToFixLines.add(howToFixJson.getRecommendation().getText());
                        howToFixLines.add(String.format("CWE: %s", howToFixJson.getCwe()));
                        howToFixLines.add(String.format("OWASP: %s", howToFixJson.getOwasp()));
                        FileUtils.writeLines(file, FILE_ENCODING, howToFixLines, true);
                        // ==================== 19-6. コメント ====================
                        List<String> noteLines = new ArrayList<String>();
                        noteLines.add(COMMENT);
                        for (Note note : trace.getNotes()) {
                            String statusVal = "";
                            String subStatusVal = "";
                            List<Property> noteProperties = note.getProperties();
                            if (noteProperties != null) {
                                for (Property prop : noteProperties) {
                                    if (prop.getName().equals("status.change.status")) {
                                        statusVal = prop.getValue();
                                    } else if (prop.getName().equals("status.change.substatus")) {
                                        subStatusVal = prop.getValue();
                                    }
                                }
                            }
                            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(note.getLast_modification())), ZoneId.systemDefault());
                            // 日時と投稿者
                            noteLines.add(String.format("[%s] %s", ldt.toString(), note.getLast_updater()));
                            // ステータス変更
                            StringBuilder statusBuffer = new StringBuilder();
                            if (!statusVal.isEmpty()) {
                                Matcher stsM = stsPtn.matcher(statusVal);
                                if (stsM.matches()) {
                                    String jpSts = StatusEnum.valueOf(statusVal.replaceAll(" ", "").toUpperCase()).getLabel();
                                    statusBuffer.append(String.format("次のステータスに変更: %s", jpSts));
                                } else {
                                    statusBuffer.append(String.format("次のステータスに変更: %s", statusVal));
                                }
                            }
                            if (!subStatusVal.isEmpty()) {
                                statusBuffer.append(String.format("(%s)", subStatusVal));
                            }
                            if (statusBuffer.length() > 0) {
                                noteLines.add(statusBuffer.toString());
                            }
                            // コメント本文
                            if (!note.getNote().isEmpty()) {
                                noteLines.add(note.getNote());
                            }
                        }
                        FileUtils.writeLines(file, FILE_ENCODING, noteLines, true);
                    }
                    if (isIncludeStackTrace) {
                        String textFileName = String.format("%s\\%s.txt", timestamp, trace.getUuid());
                        File file = new File(textFileName);
                        // ==================== 19-7. スタックトレース ====================
                        List<String> detailLines = new ArrayList<String>();
                        detailLines.add(STACK_TRACE);
                        Api eventSummaryApi = new EventSummaryApi(preferenceStore, organization, trace_id);
                        List<EventSummary> eventSummaries = (List<EventSummary>) eventSummaryApi.get();
                        for (EventSummary es : eventSummaries) {
                            if (es.getCollapsedEvents() != null && es.getCollapsedEvents().isEmpty()) {
                                detailLines.add(String.format("[%s]", es.getDescription()));
                                Api eventDetailApi = new EventDetailApi(preferenceStore, organization, trace_id, es.getId());
                                EventDetail ed = (EventDetail) eventDetailApi.get();
                                detailLines.addAll(ed.getDetailLines());
                            } else {
                                for (CollapsedEventSummary ce : es.getCollapsedEvents()) {
                                    detailLines.add(String.format("[%s]", es.getDescription()));
                                    Api eventDetailApi = new EventDetailApi(preferenceStore, organization, trace_id, ce.getId());
                                    EventDetail ed = (EventDetail) eventDetailApi.get();
                                    detailLines.addAll(ed.getDetailLines());
                                }
                            }
                        }
                        FileUtils.writeLines(file, FILE_ENCODING, detailLines, true);
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
        if (isIncludeDesc) {
            filePath = timestamp + "\\" + timestamp + ".csv";
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), CSV_ENCODING))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER_VUL)) {
                List<String> csvHeaderList = new ArrayList<String>();
                for (VulCSVColumn csvColumn : columnList) {
                    if (csvColumn.isValid()) {
                        csvHeaderList.add(csvColumn.getColumn().getCulumn());
                    }
                }
                if (isIncludeDesc) {
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
