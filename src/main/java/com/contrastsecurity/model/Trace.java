package com.contrastsecurity.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private List<String> app_version_tags;
    private String first_time_seen;
    private String last_time_seen;

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

    public String getApp_version_tags() {
        return String.join("|", this.app_version_tags);
    }

    public void setApp_version_tags(List<String> app_version_tags) {
        this.app_version_tags = app_version_tags;
    }

    public String getFirst_time_seen() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.first_time_seen)), ZoneId.systemDefault());
        return ldt.toString();
    }

    public void setFirst_time_seen(String first_time_seen) {
        this.first_time_seen = first_time_seen;
    }

    public String getLast_time_seen() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.last_time_seen)), ZoneId.systemDefault());
        return ldt.toString();
    }

    public void setLast_time_seen(String last_time_seen) {
        this.last_time_seen = last_time_seen;
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
