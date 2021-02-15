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
import com.contrastsecurity.csvdltool.model.Application;
import com.contrastsecurity.csvdltool.model.ApplicationInCustomGroup;
import com.contrastsecurity.csvdltool.model.CustomGroup;

public class AppsGetWithProgress implements IRunnableWithProgress {

    private PreferenceStore preferenceStore;
    private Map<String, AppInfo> fullAppMap;

    Logger logger = Logger.getLogger("csvdltool");

    public AppsGetWithProgress(PreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
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
            Api groupsApi = new GroupsApi(preferenceStore);
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
            // アプリケーション一覧を取得
            monitor.subTask("アプリケーション一覧の情報を取得...");
            Api applicationsApi = new ApplicationsApi(preferenceStore);
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
