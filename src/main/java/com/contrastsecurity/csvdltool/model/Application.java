package com.contrastsecurity.csvdltool.model;

import java.util.ArrayList;
import java.util.List;

public class Application {
    private String name;
    private String app_id;
    private String context_path;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getContext_path() {
        return context_path;
    }

    public void setContext_path(String context_path) {
        this.context_path = context_path;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("app_id: " + this.app_id);
        strList.add("name: " + this.name);
        return String.join(", ", strList);
    }

}
