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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ApplicationsApi;
import com.contrastsecurity.csvdltool.api.FilterSeverityApi;
import com.contrastsecurity.csvdltool.api.FilterVulnTypeApi;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;

public class AppsGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private Map<String, AppInfo> fullAppMap;
    private Set<Filter> severityFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> vulnTypeFilterSet = new LinkedHashSet<Filter>();

    Logger logger = LogManager.getLogger("csvdltool");

    public AppsGetWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        fullAppMap = new TreeMap<String, AppInfo>();
        boolean prefix_org_flg = false;
        if (this.orgs.size() > 1) {
            prefix_org_flg = true;
        }
        monitor.beginTask("アプリケーション一覧の読み込み...", 100 * this.orgs.size());
        Thread.sleep(300);
        for (Organization org : this.orgs) {
            try {
                monitor.setTaskName(org.getName());
                // フィルタの情報を取得
                monitor.subTask("フィルタの情報を取得...");
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 10);
                sub1Monitor.beginTask("", 2);
                Api filterSeverityApi = new FilterSeverityApi(this.shell, this.ps, org);
                Api filterVulnTypeApi = new FilterVulnTypeApi(this.shell, this.ps, org);
                try {
                    List<Filter> filterSeverities = (List<Filter>) filterSeverityApi.get();
                    for (Filter filter : filterSeverities) {
                        severityFilterSet.add(filter);
                    }
                    sub1Monitor.worked(1);
                    List<Filter> filterVulnTypes = (List<Filter>) filterVulnTypeApi.get();
                    for (Filter filter : filterVulnTypes) {
                        vulnTypeFilterSet.add(filter);
                    }
                    sub1Monitor.worked(1);
                } catch (ApiException ae) {
                    throw ae;
                }
                sub1Monitor.done();

                // アプリケーショングループの情報を取得
                monitor.subTask("アプリケーショングループの情報を取得...");
                Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
                Api groupsApi = new GroupsApi(this.shell, this.ps, org);
                groupsApi.setIgnoreStatusCodes(new ArrayList(Arrays.asList(403)));
                try {
                    List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
                    SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 10);
                    sub2Monitor.beginTask("", customGroups.size());
                    for (CustomGroup customGroup : customGroups) {
                        monitor.subTask(String.format("アプリケーショングループの情報を取得...%s", customGroup.getName()));
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
                        sub2Monitor.worked(1);
                    }
                    sub2Monitor.done();
                } catch (ApiException ae) {
                    throw ae;
                }
                // アプリケーション一覧を取得
                monitor.subTask("アプリケーション一覧の情報を取得...");
                Api applicationsApi = new ApplicationsApi(this.shell, this.ps, org);
                List<Application> applications = (List<Application>) applicationsApi.get();
                SubProgressMonitor sub3Monitor = new SubProgressMonitor(monitor, 80);
                sub3Monitor.beginTask("", applications.size());
                for (Application app : applications) {
                    monitor.subTask(String.format("アプリケーション一覧の情報を取得...%s", app.getName()));
                    if (app.getLicense().getLevel().equals("Unlicensed")) {
                        sub3Monitor.worked(1);
                        continue;
                    }
                    if (appGroupMap.containsKey(app.getName())) {
                        if (prefix_org_flg) {
                            fullAppMap.put(String.format("[%s] %s - %s", org.getName(), app.getName(), String.join(", ", appGroupMap.get(app.getName()))),
                                    new AppInfo(org, app.getName(), app.getApp_id()));
                        } else {
                            fullAppMap.put(String.format("%s - %s", app.getName(), String.join(", ", appGroupMap.get(app.getName()))),
                                    new AppInfo(org, app.getName(), app.getApp_id()));
                        }
                    } else {
                        if (prefix_org_flg) {
                            fullAppMap.put(String.format("[%s] %s", org.getName(), app.getName()), new AppInfo(org, app.getName(), app.getApp_id()));
                        } else {
                            fullAppMap.put(String.format("%s", app.getName()), new AppInfo(org, app.getName(), app.getApp_id()));
                        }
                    }
                    sub3Monitor.worked(1);
                    Thread.sleep(10);
                }
                sub3Monitor.done();
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }

    public Map<String, AppInfo> getFullAppMap() {
        return fullAppMap;
    }

    public Map<FilterEnum, Set<Filter>> getFilterMap() {
        Map<FilterEnum, Set<Filter>> filterMap = new HashMap<FilterEnum, Set<Filter>>();
        filterMap.put(FilterEnum.SEVERITY, severityFilterSet);
        filterMap.put(FilterEnum.VULNTYPE, vulnTypeFilterSet);
        return filterMap;
    }

}
