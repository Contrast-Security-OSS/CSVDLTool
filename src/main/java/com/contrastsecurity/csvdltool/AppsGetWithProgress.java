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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ApplicationsApi;
import com.contrastsecurity.csvdltool.api.GroupsApi;
import com.contrastsecurity.csvdltool.exception.ApiException;
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.Organization;

public class AppsGetWithProgress implements IRunnableWithProgress {

    private PreferenceStore preferenceStore;
    private List<Organization> organizations;
    private Map<String, AppInfo> fullAppMap;

    Logger logger = Logger.getLogger("csvdltool");

    public AppsGetWithProgress(PreferenceStore preferenceStore, List<Organization> organizations) {
        this.preferenceStore = preferenceStore;
        this.organizations = organizations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        fullAppMap = new TreeMap<String, AppInfo>();
        boolean prefix_org_flg = false;
        if (this.organizations.size() > 1) {
            prefix_org_flg = true;
        }
        monitor.beginTask("アプリケーション一覧の読み込み...", 100 * this.organizations.size());
        Thread.sleep(300);
        for (Organization org : this.organizations) {
            try {
                monitor.setTaskName(org.getName());
                // アプリケーショングループの情報を取得
                monitor.subTask("アプリケーショングループの情報を取得...");
                Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
                Api groupsApi = new GroupsApi(preferenceStore, org);
                try {
                    List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
                    SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 10);
                    sub1Monitor.beginTask("", customGroups.size());
                    // monitor.worked(1);
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
                        sub1Monitor.worked(1);
                    }
                    sub1Monitor.done();
                } catch (ApiException ae) {
                }
                // アプリケーション一覧を取得
                monitor.subTask("アプリケーション一覧の情報を取得...");
                Api applicationsApi = new ApplicationsApi(preferenceStore, org);
                List<Application> applications = (List<Application>) applicationsApi.get();
                // monitor.worked(1);
                SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 90);
                sub2Monitor.beginTask("", applications.size());
                for (Application app : applications) {
                    monitor.subTask(String.format("アプリケーション一覧の情報を取得...%s", app.getName()));
                    if (app.getLicense().getLevel().equals("Unlicensed")) {
                        sub2Monitor.worked(1);
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
                    sub2Monitor.worked(1);
                }
                sub2Monitor.done();
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

}
