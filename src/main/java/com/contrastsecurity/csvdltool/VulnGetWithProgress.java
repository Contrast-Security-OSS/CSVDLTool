package com.contrastsecurity.csvdltool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.api.HowToFixApi;
import com.contrastsecurity.csvdltool.api.HttpRequestApi;
import com.contrastsecurity.csvdltool.api.RoutesApi;
import com.contrastsecurity.csvdltool.api.StoryApi;
import com.contrastsecurity.csvdltool.api.TraceApi;
import com.contrastsecurity.csvdltool.api.TracesApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.json.HowToFixJson;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.Chapter;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.HttpRequest;
import com.contrastsecurity.csvdltool.model.Note;
import com.contrastsecurity.csvdltool.model.Property;
import com.contrastsecurity.csvdltool.model.Route;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.Story;
import com.contrastsecurity.csvdltool.model.Trace;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;

public class VulnGetWithProgress implements IRunnableWithProgress {

    private static final int CELL_TEXT_MAX = 32767;
    private static final List<String> CSV_HEADER = new ArrayList<String>(Arrays.asList("アプリケーション名", "マージしたときの各アプリ名称", "カテゴリ", "ルール", "深刻度", "ステータス", "言語", "アプリケーションのグループ",
            "脆弱性のタイトル", "最初の検出", "最後の検出", "ビルド番号", "次のサーバにより報告", "ルート", "モジュール", "HTTP情報", "コメント"));
    private static final List<String> CSV_HEADER_FULL = new ArrayList<String>(Arrays.asList("アプリケーション名", "マージしたときの各アプリ名称", "カテゴリ", "ルール", "深刻度", "ステータス", "言語", "アプリケーションのグループ",
            "脆弱性のタイトル", "最初の検出", "最後の検出", "ビルド番号", "次のサーバにより報告", "ルート", "モジュール", "HTTP情報", "何が起こったか？", "どんなリスクであるか？", "修正方法", "コメント"));

    private Shell shell;
    private PreferenceStore preferenceStore;
    private List<String> dstApps;
    private Map<String, String> fullAppMap;
    private boolean isIncludeDesc;

    Logger logger = Logger.getLogger("csvdltool");

    public VulnGetWithProgress(Shell shell, PreferenceStore preferenceStore, List<String> dstApps, Map<String, String> fullAppMap, boolean isIncludeDesc) {
        this.shell = shell;
        this.preferenceStore = preferenceStore;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.isIncludeDesc = isIncludeDesc;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName("脆弱性情報の取得を開始しています...");
        int sleepTrace = preferenceStore.getInt(PreferenceConstants.SLEEP_TRACE);
        String csvSepBuildNo = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_BUILDNO).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepServer = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_SERVER).replace("\\r", "\r").replace("\\n", "\n");
        String csvSepRoute = preferenceStore.getString(PreferenceConstants.CSV_SEPARATOR_ROUTE).replace("\\r", "\r").replace("\\n", "\n");
        Map<String, String> appGroupMap = new HashMap<String, String>();
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // アプリケーショングループの情報を取得
            Api groupsApi = new GroupsApi(preferenceStore);
            List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
            monitor.beginTask("アプリケーショングループの情報を取得", customGroups.size());
            for (CustomGroup customGroup : customGroups) {
                List<Application> apps = customGroup.getApplications();
                if (apps != null) {
                    for (Application app : apps) {
                        appGroupMap.put(app.getName(), customGroup.getName());
                    }
                }
                monitor.worked(1);
            }
            Thread.sleep(1000);
            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName(String.format("脆弱性情報の取得(0/%d)", dstApps.size()));
            int appIdx = 1;
            for (String appName : dstApps) {
                String appId = fullAppMap.get(appName);
                Api tracesApi = new TracesApi(preferenceStore, appId);
                List<String> traces = (List<String>) tracesApi.get();
                monitor.beginTask(String.format("脆弱性情報の取得(%d/%d)", appIdx, dstApps.size()), traces.size());
                for (String trace_id : traces) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    Api traceApi = new TraceApi(preferenceStore, appId, trace_id);
                    Trace trace = (Trace) traceApi.get();
                    monitor.subTask(String.format("%s - %s", appName, trace.getTitle()));
                    Application realApp = trace.getApplication();
                    // ==================== 01. アプリケーション名 ====================
                    csvLineList.add(appName);
                    // ==================== 02. マージしたときの、各アプリ名称（可能であれば） ====================
                    csvLineList.add(realApp.getName());
                    // ==================== 03. （脆弱性の）カテゴリ ====================
                    csvLineList.add(trace.getCategory_label());
                    // ==================== 04. （脆弱性の）ルール ====================
                    csvLineList.add(trace.getRule_title());
                    // ==================== 05. 深刻度 ====================
                    csvLineList.add(trace.getSeverity_label());
                    // ==================== 06. ステータス ====================
                    csvLineList.add(trace.getStatus());
                    // ==================== 07. 言語（Javaなど） ====================
                    csvLineList.add(trace.getLanguage());
                    // ==================== 08. グループ（アプリケーションのグループ） ====================
                    if (appGroupMap.containsKey(appName)) {
                        csvLineList.add(appGroupMap.get(appName));
                    } else {
                        csvLineList.add("");
                    }
                    // ==================== 09. 脆弱性のタイトル（例：SQLインジェクション：「/api/v1/approvers/」ページのリクエストボディ ） ====================
                    csvLineList.add(trace.getTitle());
                    // ==================== 10. 最初の検出 ====================
                    csvLineList.add(trace.getFirst_time_seen());
                    // ==================== 11. 最後の検出 ====================
                    csvLineList.add(trace.getLast_time_seen());
                    // ==================== 12. ビルド番号 ====================
                    csvLineList.add(String.join(csvSepBuildNo, trace.getApp_version_tags()));
                    // ==================== 13. 次のサーバにより報告 ====================
                    List<String> serverNameList = trace.getServers().stream().map(Server::getName).collect(Collectors.toList());
                    csvLineList.add(String.join(csvSepServer, serverNameList));
                    // ==================== 14. ルート ====================
                    Api routesApi = new RoutesApi(preferenceStore, appId, trace_id);
                    List<Route> routes = (List<Route>) routesApi.get();
                    List<String> signatureList = routes.stream().map(Route::getSignature).collect(Collectors.toList());
                    csvLineList.add(String.join(csvSepRoute, signatureList));
                    // ==================== 15. モジュール ====================
                    Application app = trace.getApplication();
                    String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                    csvLineList.add(module);
                    // ==================== 16. HTTP情報 ====================
                    Api httpRequestApi = new HttpRequestApi(preferenceStore, trace_id);
                    HttpRequest httpRequest = (HttpRequest) httpRequestApi.get();
                    if (httpRequest != null) {
                        csvLineList.add(httpRequest.getText());
                    } else {
                        csvLineList.add(""); // HTTP情報がない場合もあります。
                    }
                    if (isIncludeDesc) {
                        Api storyApi = new StoryApi(preferenceStore, trace_id);
                        Story story = (Story) storyApi.get();
                        // ==================== 17. 何が起こったか？ ====================
                        List<String> chapterLines = new ArrayList<String>();
                        for (Chapter chapter : story.getChapters()) {
                            chapterLines.add(chapter.getIntroText());
                            chapterLines.add(chapter.getBody());
                        }
                        String chapterStr = String.join("\r\n", chapterLines);
                        csvLineList.add(StringUtils.abbreviate(chapterStr, CELL_TEXT_MAX));
                        // ==================== 18. どんなリスクであるか？ ====================
                        csvLineList.add(StringUtils.abbreviate(story.getRisk().getText(), CELL_TEXT_MAX));
                        // ==================== 19. 修正方法 ====================
                        Api howToFixApi = new HowToFixApi(preferenceStore, trace_id);
                        HowToFixJson howToFixJson = (HowToFixJson) howToFixApi.get();
                        csvLineList.add(StringUtils.abbreviate(howToFixJson.getRecommendation().getText(), CELL_TEXT_MAX));
                    }
                    // ==================== 20(17). コメント(最後尾) ====================
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
                        StringBuilder strBuffer = new StringBuilder();
                        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(note.getLast_modification())), ZoneId.systemDefault());
                        strBuffer.append(String.format("[%s] ", ldt.toString()));
                        if (!statusVal.isEmpty()) {
                            strBuffer.append(String.format("次のステータスに変更: %s", statusVal));
                        }
                        if (!subStatusVal.isEmpty()) {
                            strBuffer.append(String.format("(%s)", subStatusVal));
                        }
                        if (!note.getNote().isEmpty()) {
                            strBuffer.append(String.format(" %s", note.getNote()));
                        }
                        csvLineList.add(strBuffer.toString());
                    }

                    csvList.add(csvLineList);
                    monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                appIdx++;
            }
        } catch (ApiException re) {
            MessageDialog.openError(shell, "脆弱性情報の取得", re.getMessage());
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
            MessageDialog.openError(shell, "脆弱性情報の取得", e.getMessage());
        }

        // ========== CSV出力 ==========
        monitor.beginTask("CSV出力", csvList.size());
        String csvFileFormat = preferenceStore.getString(PreferenceConstants.CSV_FILE_FORMAT);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = preferenceStore.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT);
        }
        String fileName = new SimpleDateFormat(csvFileFormat).format(new Date());
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), "shift-jis"))) {
            CSVPrinter printer = CSVFormat.EXCEL.print(bw);
            if (preferenceStore.getBoolean(PreferenceConstants.CSV_OUT_HEADER)) {
                if (isIncludeDesc) {
                    printer.printRecord(CSV_HEADER_FULL);
                } else {
                    printer.printRecord(CSV_HEADER);
                }
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
