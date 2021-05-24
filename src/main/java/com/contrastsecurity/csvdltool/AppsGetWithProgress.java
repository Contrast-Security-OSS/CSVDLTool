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
    private Organization organization;
    private Map<String, AppInfo> fullAppMap;

    Logger logger = Logger.getLogger("csvdltool");

    public AppsGetWithProgress(PreferenceStore preferenceStore, Organization organization) {
        this.preferenceStore = preferenceStore;
        this.organization = organization;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        fullAppMap = new TreeMap<String, AppInfo>();
        monitor.beginTask("アプリケーション一覧の読み込み", 2);
        try {
            // アプリケーショングループの情報を取得
            monitor.subTask("アプリケーショングループの情報を取得...");
            Map<String, List<String>> appGroupMap = new HashMap<String, List<String>>();
            Api groupsApi = new GroupsApi(preferenceStore, organization);
            try {
                List<CustomGroup> customGroups = (List<CustomGroup>) groupsApi.get();
                monitor.worked(1);
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
                }
            } catch (ApiException ae) {
            }
            // アプリケーション一覧を取得
            monitor.subTask("アプリケーション一覧の情報を取得...");
            Api applicationsApi = new ApplicationsApi(preferenceStore, organization);
            List<Application> applications = (List<Application>) applicationsApi.get();
            monitor.worked(1);
            for (Application app : applications) {
                if (app.getLicense().getLevel().equals("Unlicensed")) {
                    continue;
                }
                if (appGroupMap.containsKey(app.getName())) {
                    String appLabel = String.format("%s - %s", app.getName(), String.join(", ", appGroupMap.get(app.getName())));
                    fullAppMap.put(appLabel, new AppInfo(app.getName(), app.getApp_id()));
                } else {
                    fullAppMap.put(app.getName(), new AppInfo(app.getName(), app.getApp_id()));
                }
            }
            Thread.sleep(500); // ただの演出
            monitor.done();
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public Map<String, AppInfo> getFullAppMap() {
        return fullAppMap;
    }

}
