package com.contrastsecurity.csvdltool;

public class AppInfo {
    private String appName;
    private String appId;

    public AppInfo(String appName, String appId) {
        this.appName = appName;
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

}
