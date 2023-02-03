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

package com.contrastsecurity.csvdltool.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.contrastsecurity.csvdltool.SeverityEnum;
import com.contrastsecurity.csvdltool.StatusEnum;

public class Trace {
    private String title;
    private String rule_title;
    private String uuid;
    private String category_label;
    private String impact_label;
    private String severity_label;
    private String status;
    private String pending_status;
    private String language;
    private List<String> app_version_tags;
    private String first_time_seen;
    private String last_time_seen;
    private String discovered;
    private Application application;
    private Request request;
    private List<Server> servers;
    private List<Note> notes;

    // 3.7.9までの対応
    private String category;
    private String impact;
    private String severity;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory_label() {
        if (this.category_label != null) {
            return category_label;
        }
        return this.getCategory();
    }

    public void setCategory_label(String category_label) {
        this.category_label = category_label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getImpact_label() {
        if (this.impact_label != null) {
            return impact_label;
        }
        return this.getImpact();
    }

    public void setImpact_label(String impact_label) {
        this.impact_label = impact_label;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSeverity_label() {
        if (this.severity_label != null) {
            return severity_label;
        }
        return SeverityEnum.valueOf(this.getSeverity().toUpperCase()).getLabel();
    }

    public void setSeverity_label(String severity_label) {
        this.severity_label = severity_label;
    }

    public String getStatus() {
        Pattern p = Pattern.compile("^[A-Za-z\\s]+$");
        Matcher m = p.matcher(this.status);
        if (m.matches()) {
            return StatusEnum.valueOf(this.status.replaceAll(" ", "").toUpperCase()).getLabel();
        }
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPending_status() {
        if (this.pending_status == null) {
            return "";
        }
        Pattern p = Pattern.compile("^[A-Za-z\\s]+$");
        Matcher m = p.matcher(this.pending_status);
        if (m.matches()) {
            return StatusEnum.valueOf(this.pending_status.replaceAll(" ", "").toUpperCase()).getLabel();
        }
        return this.pending_status;
    }

    public void setPending_status(String pending_status) {
        this.pending_status = pending_status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getApp_version_tags() {
        return this.app_version_tags;
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

    public String getDiscovered() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.discovered)), ZoneId.systemDefault());
        return ldt.toString();
    }

    public void setDiscovered(String discovered) {
        this.discovered = discovered;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public List<Note> getNotes() {
        if (this.notes == null) {
            return new ArrayList<Note>();
        }
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
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
