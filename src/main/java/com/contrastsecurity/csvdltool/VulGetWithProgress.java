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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.HttpRequest;
import com.contrastsecurity.csvdltool.model.Note;
import com.contrastsecurity.csvdltool.model.Observation;
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

    private static final String ROUTE = "==================== ????????? ====================";
    private static final String HTTP_INFO = "==================== HTTP?????? ====================";
    private static final String WHAT_HAPPEN = "==================== ???????????????????????? ====================";
    private static final String RISK = "==================== ????????????????????????????????? ====================";
    private static final String HOWTOFIX = "==================== ???????????? ====================";
    private static final String COMMENT = "==================== ???????????? ====================";
    private static final String STACK_TRACE = "==================== ?????? ====================";

    private Shell shell;
    private PreferenceStore ps;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private Map<FilterEnum, Set<Filter>> filterMap;
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;
    private boolean isOnlyParentApp;
    private boolean isIncludeDesc;
    private boolean isIncludeStackTrace;

    Logger logger = LogManager.getLogger("csvdltool");

    public VulGetWithProgress(Shell shell, PreferenceStore ps, List<String> dstApps, Map<String, AppInfo> fullAppMap, Map<FilterEnum, Set<Filter>> filterMap, Date frDate,
            Date toDate, boolean isOnlyParentApp, boolean isIncludeDesc, boolean isIncludeStackTrace) {
        this.shell = shell;
        this.ps = ps;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.filterMap = filterMap;
        this.frLastDetectedDate = frDate;
        this.toLastDetectedDate = toDate;
        this.isOnlyParentApp = isOnlyParentApp;
        this.isIncludeDesc = isIncludeDesc;
        this.isIncludeStackTrace = isIncludeStackTrace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName("????????????????????????????????????????????????...");
        monitor.beginTask("????????????????????????????????????????????????...", 100);
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
                MessageDialog.openError(this.shell, "????????????????????????????????????", String.format("?????????????????????????????????????????????????????????\r\n%s", columnJsonStr));
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
            // ?????????????????????????????????????????????????????????????????????????????????????????????
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
            sub1Monitor.beginTask("", orgs.size());
            // ??????????????????????????????????????????????????????
            for (Organization org : orgs) {
                monitor.setTaskName(org.getName());
                monitor.subTask("??????????????????????????????????????????????????????...");
                Api groupsApi = new GroupsApi(this.shell, this.ps, org);
                try {
                    List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
                    SubProgressMonitor sub1_1Monitor = new SubProgressMonitor(sub1Monitor, 1);
                    sub1_1Monitor.beginTask("", customGroups.size());
                    for (CustomGroup customGroup : customGroups) {
                        monitor.subTask(String.format("??????????????????????????????????????????????????????...%s", customGroup.getName()));
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
                    Thread.sleep(1000);
                } catch (ApiException ae) {
                }
            }
            monitor.subTask("");
            sub1Monitor.done();

            // ????????????????????????????????????????????????
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 70);
            sub2Monitor.beginTask("", dstApps.size());
            int appIdx = 1;
            for (String appLabel : dstApps) {
                Organization org = fullAppMap.get(appLabel).getOrganization();
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("[%s] %s (%d/%d)", org.getName(), appName, appIdx, dstApps.size()));
                Api tracesApi = new TracesApi(this.shell, this.ps, org, appId, filterMap, frLastDetectedDate, toLastDetectedDate);
                List<String> traces = (List<String>) tracesApi.get();
                SubProgressMonitor sub2_1Monitor = new SubProgressMonitor(sub2Monitor, 1);
                sub2_1Monitor.beginTask("", traces.size());
                for (String trace_id : traces) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("?????????????????????????????????");
                    }
                    List<String> csvLineList = new ArrayList<String>();
                    Api traceApi = new TraceApi(this.shell, this.ps, org, appId, trace_id);
                    Trace trace = (Trace) traceApi.get();
                    monitor.subTask(trace.getTitle());
                    Application realApp = trace.getApplication();
                    if (isOnlyParentApp) {
                        if (!appName.equals(realApp.getName())) {
                            sub2_1Monitor.worked(1);
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
                                // ==================== 01. ??????????????????????????? ====================
                                csvLineList.add(appName);
                                break;
                            case VUL_02:
                                // ==================== 02. ????????????????????????????????????????????????????????????????????? ====================
                                csvLineList.add(realApp.getName());
                                break;
                            case VUL_03:
                                // ==================== 03. ????????????????????????ID ====================
                                csvLineList.add(realApp.getApp_id());
                                break;
                            case VUL_04:
                                // ==================== 04. ?????????????????????????????? ====================
                                Api applicationTagsApi = new ApplicationTagsApi(this.shell, this.ps, org, appId);
                                List<String> applicationTags = (List<String>) applicationTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), applicationTags));
                                break;
                            case VUL_05:
                                // ==================== 05. ?????????????????????????????? ====================
                                csvLineList.add(trace.getCategory_label());
                                break;
                            case VUL_06:
                                // ==================== 06. ??????????????????????????? ====================
                                csvLineList.add(trace.getRule_title());
                                break;
                            case VUL_07:
                                // ==================== 07. ????????? ====================
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
                                            if (!MessageDialog.openConfirm(shell, "????????????????????????", "???????????????CWE???OWASP????????????????????????????????????????????????????????????\r\n?????????????????????????????????????????????????????????????????????????????????")) {
                                                monitor.setCanceled(true);
                                            }
                                        }
                                    });
                                    Recommendation recommendation = new Recommendation();
                                    recommendation.setText("***** ?????????????????????????????? *****");
                                    howToFixJson = new HowToFixJson();
                                    howToFixJson.setRecommendation(recommendation);
                                    howToFixJson.setCwe("");
                                    howToFixJson.setOwasp("");
                                    csvLineList.add("");
                                }
                                break;
                            case VUL_09:
                                // ==================== 09. ??????????????? ====================
                                csvLineList.add(trace.getStatus());
                                break;
                            case VUL_10:
                                // ==================== 10. ?????????Java????????? ====================
                                csvLineList.add(trace.getLanguage());
                                break;
                            case VUL_11:
                                // ==================== 11. ????????????????????????????????????????????????????????? ====================
                                if (appGroupMap.containsKey(appName)) {
                                    csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), appGroupMap.get(appName)));
                                } else {
                                    csvLineList.add("");
                                }
                                break;
                            case VUL_12:
                                // ==================== 12. ?????????????????????????????????SQL??????????????????????????????/api/v1/approvers/??????????????????????????????????????? ??? ====================
                                csvLineList.add(trace.getTitle());
                                break;
                            case VUL_13:
                                // ==================== 13. ??????????????? ====================
                                csvLineList.add(trace.getFirst_time_seen());
                                break;
                            case VUL_14:
                                // ==================== 14. ??????????????? ====================
                                csvLineList.add(trace.getLast_time_seen());
                                break;
                            case VUL_15:
                                // ==================== 15. ??????????????? ====================
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), trace.getApp_version_tags()));
                                break;
                            case VUL_16:
                                // ==================== 16. ?????????????????????????????? ====================
                                List<String> serverNameList = trace.getServers().stream().map(Server::getName).collect(Collectors.toList());
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), serverNameList));
                                break;
                            case VUL_17:
                                // ==================== 17. ??????????????? ====================
                                Application app = trace.getApplication();
                                String module = String.format("%s (%s) - %s", app.getName(), app.getContext_path(), app.getLanguage());
                                csvLineList.add(module);
                                break;
                            case VUL_18:
                                // ==================== 18. ??????????????? ====================
                                Api traceTagsApi = new TraceTagsApi(this.shell, this.ps, org, trace_id);
                                List<String> traceTags = (List<String>) traceTagsApi.get();
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), traceTags));
                                break;
                            case VUL_19:
                                // ==================== 19. ???????????????????????? ====================
                                csvLineList.add(trace.getPending_status());
                                break;
                            case VUL_20:
                                // ==================== 20. ????????? ====================
                                csvLineList.add(org.getName());
                                break;
                            case VUL_21:
                                // ==================== 21. ??????ID ====================
                                csvLineList.add(org.getOrganization_uuid());
                                break;
                            case VUL_22: {
                                // ==================== 22. ???????????????????????? ====================
                                String link = String.format("%s/static/ng/index.html#/%s/applications/%s/vulns/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                        org.getOrganization_uuid(), trace.getApplication().getApp_id(), trace.getUuid());
                                csvLineList.add(link);
                                break;
                            }
                            case VUL_23: {
                                // ==================== 23. ??????????????????????????????????????????????????? ====================
                                String link = String.format("%s/static/ng/index.html#/%s/applications/%s/vulns/%s", this.ps.getString(PreferenceConstants.CONTRAST_URL),
                                        org.getOrganization_uuid(), trace.getApplication().getApp_id(), trace.getUuid());
                                csvLineList.add(String.format("=HYPERLINK(\"%s\",\"TeamServer???\")", link));
                                break;
                            }
                            case VUL_24:
                                // ==================== 18. ??????????????? ====================
                                Api routesApi = new RoutesApi(this.shell, this.ps, org, appId, trace_id);
                                routes = (List<Route>) routesApi.get();
                                List<String> urlList = new ArrayList<String>();
                                for (Route route : routes) {
                                    urlList.addAll(route.getObservations().stream().map(Observation::getUrl).collect(Collectors.toList()));
                                }
                                csvLineList.add(String.join(csvColumn.getSeparateStr().replace("\\r", "\r").replace("\\n", "\n"), urlList));
                                break;
                            default:
                                continue;
                        }
                    }
                    if (isIncludeDesc) {
                        // ==================== 19. ??????????????????????????? ====================
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

                        // ==================== 19-1. ????????? ====================
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
                            signatureUrlList.add("??????");
                        }
                        signatureUrlList.add(0, ROUTE);
                        FileUtils.writeLines(file, Main.FILE_ENCODING, signatureUrlList, true);

                        // ==================== 19-2. HTTP?????? ====================
                        Api httpRequestApi = new HttpRequestApi(this.shell, this.ps, org, trace_id);
                        HttpRequest httpRequest = (HttpRequest) httpRequestApi.get();
                        if (httpRequest != null) {
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(HTTP_INFO, httpRequest.getText()), true);
                        } else {
                            FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(HTTP_INFO, "??????"), true);
                        }

                        Api storyApi = new StoryApi(this.shell, this.ps, org, trace_id);
                        Story story = null;
                        try {
                            story = (Story) storyApi.get();
                        } catch (Exception e) {
                            this.shell.getDisplay().syncExec(new Runnable() {
                                public void run() {
                                    if (!MessageDialog.openConfirm(shell, "????????????????????????", "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????\r\n?????????????????????????????????????????????????????????????????????????????????")) {
                                        monitor.setCanceled(true);
                                    }
                                }
                            });
                            Risk risk = new Risk();
                            risk.setText("***** ?????????????????????????????? *****");
                            story = new Story();
                            story.setRisk(risk);
                            story.setChapters(new ArrayList<Chapter>());
                        }
                        // ==================== 19-3. ???????????????????????? ====================
                        List<String> chapterLines = new ArrayList<String>();
                        chapterLines.add(WHAT_HAPPEN);
                        for (Chapter chapter : story.getChapters()) {
                            chapterLines.add(chapter.getIntroText());
                            chapterLines.add(chapter.getBody());
                        }
                        FileUtils.writeLines(file, Main.FILE_ENCODING, chapterLines, true);
                        // ==================== 19-4. ????????????????????????????????? ====================
                        FileUtils.writeLines(file, Main.FILE_ENCODING, Arrays.asList(RISK, story.getRisk().getText()), true);
                        // ==================== 19-5. ???????????? ====================
                        List<String> howToFixLines = new ArrayList<String>();
                        howToFixLines.add(HOWTOFIX);
                        if (howToFixJson == null) {
                            Api howToFixApi = new HowToFixApi(this.shell, this.ps, org, trace_id);
                            try {
                                howToFixJson = (HowToFixJson) howToFixApi.get();
                            } catch (Exception e) {
                                this.shell.getDisplay().syncExec(new Runnable() {
                                    public void run() {
                                        if (!MessageDialog.openConfirm(shell, "????????????????????????", "???????????????CWE???OWASP????????????????????????????????????????????????????????????\r\n?????????????????????????????????????????????????????????????????????????????????")) {
                                            monitor.setCanceled(true);
                                        }
                                    }
                                });
                                Recommendation recommendation = new Recommendation();
                                recommendation.setText("***** ?????????????????????????????? *****");
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
                        // ==================== 19-6. ???????????? ====================
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
                            // ??????????????????
                            noteLines.add(String.format("[%s] %s", ldt.toString(), note.getLast_updater()));
                            // ?????????????????????
                            StringBuilder statusBuffer = new StringBuilder();
                            if (!statusVal.isEmpty()) {
                                Matcher stsM = stsPtn.matcher(statusVal);
                                if (stsM.matches()) {
                                    String jpSts = StatusEnum.valueOf(statusVal.replaceAll(" ", "").toUpperCase()).getLabel();
                                    statusBuffer.append(String.format("??????????????????????????????: %s", jpSts));
                                } else {
                                    statusBuffer.append(String.format("??????????????????????????????: %s", statusVal));
                                }
                            }
                            if (!subStatusVal.isEmpty()) {
                                statusBuffer.append(String.format("(%s)", subStatusVal));
                            }
                            if (statusBuffer.length() > 0) {
                                noteLines.add(statusBuffer.toString());
                            }
                            // ??????????????????
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
                        // ==================== 19-7. ???????????????????????? ====================
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
                    sub2_1Monitor.worked(1);
                    Thread.sleep(sleepTrace);
                }
                appIdx++;
            }
            monitor.subTask("");
            sub2Monitor.done();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }

        // ========== CSV?????? ==========
        monitor.setTaskName("CSV??????");
        Thread.sleep(500);
        SubProgressMonitor sub3Monitor = new SubProgressMonitor(monitor, 20);
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
                    csvHeaderList.add("??????");
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
