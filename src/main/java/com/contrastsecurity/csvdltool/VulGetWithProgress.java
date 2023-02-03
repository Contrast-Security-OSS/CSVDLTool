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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import com.contrastsecurity.csvdltool.api.ApplicationTagsApi;
import com.contrastsecurity.csvdltool.api.EventDetailApi;
import com.contrastsecurity.csvdltool.api.EventSummaryApi;
import com.contrastsecurity.csvdltool.api.FilterSecurityStandardApi;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.api.HowToFixApi;
import com.contrastsecurity.csvdltool.api.HttpRequestApi;
import com.contrastsecurity.csvdltool.api.RoutesApi;
import com.contrastsecurity.csvdltool.api.SessionMetadataApi;
import com.contrastsecurity.csvdltool.api.StoryApi;
import com.contrastsecurity.csvdltool.api.TraceApi;
import com.contrastsecurity.csvdltool.api.TraceInstancesApi;
import com.contrastsecurity.csvdltool.api.TraceTagsApi;
import com.contrastsecurity.csvdltool.api.TracesApi;
import com.contrastsecurity.csvdltool.api.TracesFilterBySecurityStandardApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.json.HowToFixJson;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.Chapter;
import com.contrastsecurity.csvdltool.model.CollapsedEventSummary;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.EventDetail;
import com.contrastsecurity.csvdltool.model.EventSummary;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.HttpRequest;
import com.contrastsecurity.csvdltool.model.Instance;
import com.contrastsecurity.csvdltool.model.Note;
import com.contrastsecurity.csvdltool.model.Observation;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Property;
import com.contrastsecurity.csvdltool.model.Recommendation;
import com.contrastsecurity.csvdltool.model.Risk;
import com.contrastsecurity.csvdltool.model.Route;
import com.contrastsecurity.csvdltool.model.Server;
import com.contrastsecurity.csvdltool.model.SessionMetadata;
import com.contrastsecurity.csvdltool.model.Story;
import com.contrastsecurity.csvdltool.model.Trace;
import com.contrastsecurity.csvdltool.model.VulCSVColumn;
import com.contrastsecurity.csvdltool.model.Vulnerability;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class VulGetWithProgress implements IRunnableWithProgress {

    private static final String ROUTE = "==================== ルート ====================";
    private static final String HTTP_INFO = "==================== HTTP情報 ====================";
    private static final String WHAT_HAPPEN = "==================== 何が起こったか？ ====================";
    private static final String RISK = "==================== どんなリスクであるか？ ====================";
    private static final String HOWTOFIX = "==================== 修正方法 ====================";
    private static final String COMMENT = "==================== コメント ====================";
    private static final String STACK_TRACE = "==================== 詳細 ====================";

    private Shell shell;
    private PreferenceStore ps;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> filterMap;
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;
    private boolean isOnlyParentApp;
    private boolean isOnlyCurVulExp;
    private boolean isIncludeDesc;
    private boolean isIncludeStackTrace;
    private Timer timer;

    Logger logger = LogManager.getLogger("csvdltool");

    public VulGetWithProgress(Shell shell, PreferenceStore ps, List<String> dstApps, Map<String, AppInfo> fullAppMap, Map<FilterEnum, Set<Filter>> filterMap, Date frDate,
            Date toDate, boolean isOnlyParentApp, boolean isOnlyCurVulExp, boolean isIncludeDesc, boolean isIncludeStackTrace) {
        this.shell = shell;
        this.ps = ps;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.filterMap = filterMap;
        this.frLastDetectedDate = frDate;
        this.toLastDetectedDate = toDate;
        this.isOnlyParentApp = isOnlyParentApp;
        this.isOnlyCurVulExp = isOnlyCurVulExp;
        this.isIncludeDesc = isIncludeDesc;
        this.isIncludeStackTrace = isIncludeStackTrace;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        monitor.setTaskName("脆弱性情報の取得を開始しています...");
        monitor.beginTask("脆弱性情報の取得を開始しています...", 100);
        String csvFileFormat = this.ps.getString(PreferenceConstants.CSV_FILE_FORMAT_VUL);
        if (csvFileFormat == null || csvFileFormat.isEmpty()) {
            csvFileFormat = this.ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_VUL);
        }
        Pattern cwePtn = Pattern.compile("\\/(\\d+)\\.html$");
        Pattern stsPtn = Pattern.compile("^[A-Za-z\\s]+$");
        String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
        int sleepTrace = this.ps.getInt(PreferenceConstants.SLEEP_VUL);
        String columnJsonStr = this.ps.getString(PreferenceConstants.CSV_COLUMN_VUL);
        List<VulCSVColumn> columnList = null;
        if (columnJsonStr.trim().length() > 0) {
            try {
                columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<VulCSVColumn>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(this.shell, "脆弱性出力項目の読み込み", String.format("脆弱性出力項目の内容に問題があります。\r\n%s", columnJsonStr));
                columnList = new ArrayList<VulCSVColumn>();
            }
        } else {
            columnList = new ArrayList<VulCSVColumn>();
            for (VulCSVColmunEnum colEnum : VulCSVColmunEnum.sortedValues()) {
                columnList.add(new VulCSVColumn(colEnum));
            }
        }
        Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
        List<List<String>> csvList = new ArrayList<List<String>>();
        try {
            // 長文情報（何が起こったか？など）を出力する場合はフォルダに出力
            if (this.isIncludeDesc) {
                String dirPath = timestamp;
                if (OS.isFamilyMac()) {
                    if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                        dirPath = "../../../" + timestamp;
                    }
                }
                Path dir = Paths.get(dirPath);
                Files.createDirectory(dir);
            }
            Set<Organization> orgs = new HashSet<Organization>();
            for (String appLabel : dstApps) {
                orgs.add(fullAppMap.get(appLabel).getOrganization());
            }
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 10);
            sub1Monitor.beginTask("", orgs.size() * 100);
            // アプリケーショングループの情報を取得
            monitor.setTaskName("アプリケーショングループの情報を取得...");
            for (Organization org : orgs) {
                monitor.setTaskName(String.format("アプリケーショングループの情報を取得...%s", org.getName()));
                Api groupsApi = new GroupsApi(this.shell, this.ps, org);
                groupsApi.setIgnoreStatusCodes(new ArrayList(Arrays.asList(403)));
                try {
                    List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
                    SubProgressMonitor sub1_1Monitor = new SubProgressMonitor(sub1Monitor, 100);
                    sub1_1Monitor.beginTask("", customGroups.size());
                    for (CustomGroup customGroup : customGroups) {
                        monitor.subTask(customGroup.getName());
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
                        sub1_1Monitor.worked(1);
                    }
                    sub1_1Monitor.done();
                    Thread.sleep(500);
                } catch (ApiException ae) {
                }
            }
            monitor.subTask("");
            sub1Monitor.done();

            // コンプライアンスポリシーの情報を取得するか判定
            boolean validCompliancePolicy = false;
            for (VulCSVColumn csvColumn : columnList) {
                if (csvColumn.getColumn() == VulCSVColmunEnum.VUL_26) {
                    if (csvColumn.isValid()) {
                        validCompliancePolicy = true;
                    }
                }
            }

            // 選択済みアプリの脆弱性情報を取得
            monitor.setTaskName("脆弱性の情報を取得...");
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 80);
            sub2Monitor.beginTask("", dstApps.size() * 100);
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("脆弱性の情報を取得...[%s] %s (%d/%d)", org.getName(), appName, appIdx, dstApps.size()));
                // コンプライアンスポリシーの情報を取得
                Map<String, List<Vulnerability>> securityStandardVulnMap = new HashMap<String, List<Vulnerability>>();
                if (validCompliancePolicy) {
                    SubProgressMonitor sub2_1Monitor = new SubProgressMonitor(sub2Monitor, 20);
                    Api filterSecurityStandardApi = new FilterSecurityStandardApi(this.shell, this.ps, org);
                    List<Filter> filterSecurityStandards = (List<Filter>) filterSecurityStandardApi.get();
                    sub2_1Monitor.beginTask("", filterSecurityStandards.size());
                    for (Filter ssFilter : filterSecurityStandards) {
                        monitor.subTask(String.format("コンプライアンスポリシー: %s", ssFilter.getLabel()));
                        if (monitor.isCanceled()) {
                            if (this.timer != null) {
                                timer.cancel();
                            }
                            throw new InterruptedException("キャンセルされました。");
                        }
                        List<Vulnerability> allVuls = new ArrayList<Vulnerability>();
                        Api tracesFilterBySecurityStandardApi = new TracesFilterBySecurityStandardApi(this.shell, this.ps, org, appId, ssFilter.getKeycode(), 0);
                        List<Vulnerability> tmpVuls = (List<Vulnerability>) tracesFilterBySecurityStandardApi.post();
                        int totalTraceByFilterCount = tracesFilterBySecurityStandardApi.getTotalCount();
                        allVuls.addAll(tmpVuls);
                        boolean traceByFilterIncompleteFlg = false;
                        traceByFilterIncompleteFlg = totalTraceByFilterCount > allVuls.size();
                        while (traceByFilterIncompleteFlg) {
                            Thread.sleep(100);
                            tracesFilterBySecurityStandardApi = new TracesFilterBySecurityStandardApi(this.shell, this.ps, org, appId, ssFilter.getKeycode(), allVuls.size());
                            tmpVuls = (List<Vulnerability>) tracesFilterBySecurityStandardApi.post();
                            allVuls.addAll(tmpVuls);
                            traceByFilterIncompleteFlg = totalTraceByFilterCount > allVuls.size();
                        }
                        securityStandardVulnMap.put(ssFilter.getLabel(), allVuls);
                        sub2_1Monitor.worked(1);
                    }
                }
                Thread.sleep(500);
                Api tracesApi = new TracesApi(this.shell, this.ps, org, appId, filterMap, frLastDetectedDate, toLastDetectedDate);
                List<String> traces = (List<String>) tracesApi.get();
                if (!isOnlyCurVulExp) {
                    List<String> copyTraces = new ArrayList<String>(traces);
                    Collections.reverse(copyTraces);
                    for (String trace_id : copyTraces) {
                        int insertIdx = traces.indexOf(trace_id);
                        Api traceInstancesApi = new TraceInstancesApi(shell, ps, org, appId, trace_id);
                        List<Instance> instances = (List<Instance>) traceInstancesApi.get();
                        for (Instance instance : instances) {
                            if (trace_id.equals(instance.getUuid())) {
                                continue;
                            }
                            traces.add(insertIdx, instance.getUuid());
                        }
                    }
                }
                SubProgressMonitor sub2_2Monitor = new SubProgressMonitor(sub2Monitor, 80);
                sub2_2Monitor.beginTask("", traces.size());
                int traceIdx = 1;
                for (String trace_id : traces) {
                    if (monitor.isCanceled()) {
                        if (this.timer != null) {
                            timer.cancel();
                        }
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    Api traceApi = new TraceApi(this.shell, this.ps, org, appId, trace_id);
                    Trace trace = (Trace) traceApi.get();
                    monitor.subTask(String.format("脆弱性: %s (%d/%d)", trace.getTitle(), traceIdx, traces.size()));
                    Application realApp = trace.getApplication();
                    if (isOnlyParentApp) {
                        if (!appName.equals(realApp.getName())) {
                            sub2_2Monitor.worked(1);
                            continue;
                        }
                    }
                    HowToFixJson howToFixJson = null;
                    List<Route> routes = null;
                    for (VulCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case VUL_01:
                                // ==================== 01. アプリケーション名 ====================
                                csvLineList.add(appName);
                                break;
                            case VUL_02:
                                // ==================== 02. マージしたときの、各アプリ名称（可能であれば） ====================
                                csvLineList.add(realApp.getName());
                                break;
                            case VUL_03:
                                // ==================== 03. アプリケーションID ====================
                                csvLineList.add(realApp.getApp_id());
                                break;
                            case VUL_04:
                                // ==================== 04. アプリケーションタグ ====================
                                Api applicationTagsApi = new ApplicationTagsApi(this.shell, this.ps, org, appId);
                                List<String> applicationTags = (List<String>) applicationTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), applicationTags));
                                break;
                            case VUL_05:
                                // ==================== 05. （脆弱性の）カテゴリ ====================
                                csvLineList.add(trace.getCategory_label());
                                break;
                            case VUL_06:
                                // ==================== 06. （脆弱性の）ルール ====================
                                csvLineList.add(trace.getRule_title());
                                break;
                            case VUL_07:
                                // ==================== 07. 深刻度 ====================
                                csvLineList.add(trace.getSeverity_label());
                                break;
                            case VUL_08:
                                // ==================== 08. CWE ====================
                                Api howToFixApi = new HowToFixApi(this.shell, this.ps, org, trace_id);
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
                                    this.shell.getDisplay().syncExec(new Runnable() {
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
                                // ==================== 09. ステータス ====================
                                csvLineList.add(trace.getStatus());
                                break;
                            case VUL_10:
                                // ==================== 10. 言語（Javaなど） ====================
                                csvLineList.add(trace.getLanguage());
                                break;
                            case VUL_11:
                                // ==================== 11. グループ（アプリケーションのグループ） ====================
                                if (appGroupMap.containsKey(appName)) {
                                    csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), appGroupMap.get(appName)));
                                } else {
                                    csvLineList.add("");
                                }
                                break;
                            case VUL_12:
                                // ==================== 12. 脆弱性のタイトル（例：SQLインジェクション：「/api/v1/approvers/」ページのリクエストボディ ） ====================
                                csvLineList.add(trace.getTitle());
                                break;
                            case VUL_13:
                                // ==================== 13. 最初の検出 ====================
                                csvLineList.add(trace.getFirst_time_seen());
                                break;
                            case VUL_14:
                                // ==================== 14. 最後の検出 ====================
                                csvLineList.add(trace.getLast_time_seen());
                                break;
                            case VUL_15:
                                // ==================== 15. ビルド番号 ====================
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), trace.getApp_version_tags()));
                                break;
                            case VUL_16:
                                // ==================== 16. 次のサーバにより報告 ====================
                                List<String> serverNameList = trace.getServers().stream().map(Server::getName).collect(Collectors.toList());
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), serverNameList));
                                break;
                            case VUL_17:
                                // ==================== 17. モジュール ====================
                                Application app = trace.getApplication();
                                String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                                csvLineList.add(module);
                                break;
                            case VUL_18:
                                // ==================== 18. 脆弱性タグ ====================
                                Api traceTagsApi = new TraceTagsApi(this.shell, this.ps, org, trace_id);
                                List<String> traceTags = (List<String>) traceTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), traceTags));
                                break;
                            case VUL_19:
                                // ==================== 19. 保留中ステータス ====================
                                csvLineList.add(trace.getPending_status());
                                break;
                            case VUL_20:
                                // ==================== 20. 組織名 ====================
                                csvLineList.add(org.getName());
                                break;
                            case VUL_21:
                                // ==================== 21. 組織ID ====================
                                csvLineList.add(org.getOrganization_uuid());
                                break;
                            case VUL_22: {
                                // ==================== 22. 脆弱性へのリンク ====================
                                String link = String.format("%s/static/ng/index.html#/%s/applications/%s/vulns/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                        org.getOrganization_uuid(), trace.getApplication().getApp_id(), trace.getUuid());
                                csvLineList.add(link);
                                break;
                            }
                            case VUL_23: {
                                // ==================== 23. 脆弱性へのリンク（ハイパーリンク） ====================
                                String link = String.format("%s/static/ng/index.html#/%s/applications/%s/vulns/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                        org.getOrganization_uuid(), trace.getApplication().getApp_id(), trace.getUuid());
                                csvLineList.add(String.format("=HYPERLINK(\"%s\",\"TeamServerへ\")", link));
                                break;
                            }
                            case VUL_24:
                                // ==================== 18. 脆弱性タグ ====================
                                Api routesApi = new RoutesApi(this.shell, this.ps, org, appId, trace_id);
                                routes = (List<Route>) routesApi.get();
                                List<String> urlList = new ArrayList<String>();
                                for (Route route : routes) {
                                    urlList.addAll(route.getObservations().stream().map(Observation::getUrl).collect(Collectors.toList()));
                                }
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), urlList));
                                break;
                            case VUL_25:
                                // ==================== 25. セッションメタデータ ====================
                                SessionMetadataApi sessionMetadataApi = new SessionMetadataApi(this.shell, this.ps, org, appId, trace_id);
                                List<SessionMetadata> metadatas = (List<SessionMetadata>) sessionMetadataApi.get();
                                List<String> smList = new ArrayList<String>();
                                for (SessionMetadata sm : metadatas) {
                                    smList.add(String.format("%s: %s", sm.getDisplay_label(), sm.getValue()));
                                }
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), smList));
                                break;
                            case VUL_26:
                                // ==================== 26. コンプライアンスポリシー ====================
                                List<String> ssNameList = new ArrayList<String>();
                                securityStandardVulnMap.forEach((k, v) -> {
                                    boolean matchFlg = false;
                                    for (Vulnerability vul : v) {
                                        if (trace_id.equals(vul.getUuid())) {
                                            matchFlg |= true;
                                        }
                                    }
                                    if (matchFlg) {
                                        ssNameList.add(k);
                                    }
                                });
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), ssNameList));
                                break;
                            case VUL_27:
                                // ==================== 27. 検出日時 ====================
                                csvLineList.add(trace.getDiscovered());
                                break;
                            default:
                                continue;
                        }
                    }
                    if (isIncludeDesc) {
                        // ==================== 19. 詳細（長文データ） ====================
                        if (OS.isFamilyWindows()) {
                            csvLineList.add(String.format("=HYPERLINK(\".\\%s.txt\",\"%s\")", trace.getUuid(), trace.getUuid()));
                        } else {
                            csvLineList.add(String.format("=HYPERLINK(\"%s.txt\",\"%s\")", trace.getUuid(), trace.getUuid()));
                        }
                        String textFileName = String.format("%s\\%s.txt", timestamp, trace.getUuid());
                        if (OS.isFamilyMac()) {
                            textFileName = String.format("%s/%s.txt", timestamp, trace.getUuid());
                            if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                                textFileName = String.format("../../../%s/%s.txt", timestamp, trace.getUuid());
                            }
                        }
                        File file = new File(textFileName);

                        // ==================== 19-1. ルート ====================
                        if (routes == null) {
                            Api routesApi = new RoutesApi(this.shell, this.ps, org, appId, trace_id);
                            routes = (List<Route>) routesApi.get();
                        }
                        List<String> signatureUrlList = new ArrayList<String>();
                        for (Route route : routes) {
                            signatureUrlList.add(route.getSignature());
                            for (String url : route.getObservations().stream().map(Observation::getUrl).collect(Collectors.toList())) {
                                signatureUrlList.add(String.format("- %s", url));
                            }
                        }
                        if (signatureUrlList.isEmpty()) {
                            signatureUrlList.add("なし");
                        }
                        signatureUrlList.add(0, ROUTE);
                        FileUtils.writeLines(file, Main.FILE_ENCODING, signatureUrlList, true);

                        // ==================== 19-2. HTTP情報 ====================
                        Api httpRequestApi = new HttpRequestApi(this.shell, this.ps, org, trace_id);
                        HttpRequest httpRequest = (HttpRequest) httpRequestApi.get();
                        if (httpRequest != null) {
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(HTTP_INFO, httpRequest.getText()), true);
                        } else {
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(HTTP_INFO, "なし"), true);
                        }

                        Api storyApi = new StoryApi(this.shell, this.ps, org, trace_id);
                        Story story = null;
                        try {
                            story = (Story) storyApi.get();
                        } catch (Exception e) {
                            this.shell.getDisplay().syncExec(new Runnable() {
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
                        FileUtils.writeLines(file, Main.FILE_ENCODING, chapterLines, true);
                        // ==================== 19-4. どんなリスクであるか？ ====================
                        FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(RISK, story.getRisk().getText()), true);
                        // ==================== 19-5. 修正方法 ====================
                        List<String> howToFixLines = new ArrayList<String>();
                        howToFixLines.add(HOWTOFIX);
                        if (howToFixJson == null) {
                            Api howToFixApi = new HowToFixApi(this.shell, this.ps, org, trace_id);
                            try {
                                howToFixJson = (HowToFixJson) howToFixApi.get();
                            } catch (Exception e) {
                                this.shell.getDisplay().syncExec(new Runnable() {
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
                        FileUtils.writeLines(file, Main.FILE_ENCODING, howToFixLines, true);
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
                        FileUtils.writeLines(file, Main.FILE_ENCODING, noteLines, true);
                    }
                    if (isIncludeStackTrace) {
                        String textFileName = String.format("%s\\%s.txt", timestamp, trace.getUuid());
                        if (OS.isFamilyMac()) {
                            textFileName = String.format("%s/%s.txt", timestamp, trace.getUuid());
                            if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                                textFileName = String.format("../../../%s/%s.txt", timestamp, trace.getUuid());
                            }
                        }
                        File file = new File(textFileName);
                        // ==================== 19-7. スタックトレース ====================
                        List<String> detailLines = new ArrayList<String>();
                        detailLines.add(STACK_TRACE);
                        Api eventSummaryApi = new EventSummaryApi(this.shell, this.ps, org, trace_id);
                        List<EventSummary> eventSummaries = (List<EventSummary>) eventSummaryApi.get();
                        for (EventSummary es : eventSummaries) {
                            if (es.getCollapsedEvents() != null && es.getCollapsedEvents().isEmpty()) {
                                detailLines.add(String.format("[%s]", es.getDescription()));
                                Api eventDetailApi = new EventDetailApi(this.shell, this.ps, org, trace_id, es.getId());
                                EventDetail ed = (EventDetail) eventDetailApi.get();
                                detailLines.addAll(ed.getDetailLines());
                            } else {
                                for (CollapsedEventSummary ce : es.getCollapsedEvents()) {
                                    detailLines.add(String.format("[%s]", es.getDescription()));
                                    Api eventDetailApi = new EventDetailApi(this.shell, this.ps, org, trace_id, ce.getId());
                                    EventDetail ed = (EventDetail) eventDetailApi.get();
                                    detailLines.addAll(ed.getDetailLines());
                                }
                            }
                        }
                        FileUtils.writeLines(file, Main.FILE_ENCODING, detailLines, true);
                    }

                    csvList.add(csvLineList);
                    traceIdx++;
                    sub2_2Monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                appIdx++;
            }
            monitor.subTask("");
            sub2Monitor.done();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (this.timer != null) {
                this.timer.cancel();
            }
        }

        // ========== CSV出力 ==========
        monitor.setTaskName("CSV出力");
        Thread.sleep(500);
        SubProgressMonitor sub3Monitor = new SubProgressMonitor(monitor, 10);
        sub3Monitor.beginTask("", csvList.size());
        String filePath = timestamp + ".csv";
        if (OS.isFamilyMac()) {
            if (System.getProperty("user.dir").contains(".app/Contents/Java")) {
                filePath = "../../../" + timestamp + ".csv";
            }
        }
        if (isIncludeDesc) {
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
            if (this.ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_VUL)) {
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
                sub3Monitor.worked(1);
                Thread.sleep(10);
            }
            sub3Monitor.done();
        } catch (IOException e) {
            e.printStackTrace();
        }
        monitor.done();
    }
}
