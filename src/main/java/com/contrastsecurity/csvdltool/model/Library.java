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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Library {
    private String hash;
    private String file_name;
    private String app_language;
    private String grade;
    private String version;
    private String release_date;
    private String latest_version;
    private String latest_release_date;
    private int classes_used;
    private int class_count;
    private int total_vulnerabilities;
    private List<Vuln> vulns;
    private List<String> licenses;
    private List<Application> apps;
    private List<Server> servers;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getApp_language() {
        return app_language;
    }

    public void setApp_language(String app_language) {
        this.app_language = app_language;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease_date() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.release_date)), ZoneId.systemDefault());
        DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return datetimeformatter.format(ldt);
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getLatest_version() {
        return latest_version;
    }

    public void setLatest_version(String latest_version) {
        this.latest_version = latest_version;
    }

    public String getLatest_release_date() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.latest_release_date)), ZoneId.systemDefault());
        DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return datetimeformatter.format(ldt);
    }

    public void setLatest_release_date(String latest_release_date) {
        this.latest_release_date = latest_release_date;
    }

    public int getClasses_used() {
        return classes_used;
    }

    public void setClasses_used(int classes_used) {
        this.classes_used = classes_used;
    }

    public int getClass_count() {
        return class_count;
    }

    public void setClass_count(int class_count) {
        this.class_count = class_count;
    }

    public int getTotal_vulnerabilities() {
        return total_vulnerabilities;
    }

    public void setTotal_vulnerabilities(int total_vulnerabilities) {
        this.total_vulnerabilities = total_vulnerabilities;
    }

    public List<Vuln> getVulns() {
        return vulns;
    }

    public void setVulns(List<Vuln> vulns) {
        this.vulns = vulns;
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<String> licenses) {
        this.licenses = licenses;
    }

    public List<Application> getApps() {
        return apps;
    }

    public void setApps(List<Application> apps) {
        this.apps = apps;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

}
