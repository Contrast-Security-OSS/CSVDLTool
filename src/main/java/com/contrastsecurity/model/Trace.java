package com.contrastsecurity.model;

import java.util.ArrayList;
import java.util.List;

public class Trace {
    private String category_label;
    private String rule_title;
    private String uuid;
    private String impact_label;
    private String severity_label;
    private String status;
    private String language;

    public String getCategory_label() {
        return category_label;
    }

    public void setCategory_label(String category_label) {
        this.category_label = category_label;
    }

    public String getRule_title() {
        return rule_title;
    }

    public void setRule_title(String rule_title) {
        this.rule_title = rule_title;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getImpact_label() {
        return impact_label;
    }

    public void setImpact_label(String impact_label) {
        this.impact_label = impact_label;
    }

    public String getSeverity_label() {
        return severity_label;
    }

    public void setSeverity_label(String severity_label) {
        this.severity_label = severity_label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        strList.add(String.format("--- %s ---------------", this.uuid));
        strList.add("category_label: " + this.category_label);
        strList.add("rule_title: " + this.rule_title);
        strList.add("impact_label: " + this.impact_label);
        return String.join("\r\n", strList);
    }

}
