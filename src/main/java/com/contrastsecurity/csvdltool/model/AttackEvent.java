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

public class AttackEvent {
    private String received;
    private String source;
    private String result;
    private String displayName;
    private String rule;
    private String url;
    private boolean suppressed;
    private Application application;
    private Server server;
    private HttpRequest httpRequest;
    private String event_uuid;
    private Organization organization;
    private List<String> tags;
    private UserInput user_input;
    // Jsonにはないフィールド
    private String source_name;

    public String getReceived() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.received)), ZoneId.systemDefault());
        return ldt.toString();
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getEvent_uuid() {
        return event_uuid;
    }

    public void setEvent_uuid(String event_uuid) {
        this.event_uuid = event_uuid;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<String> getTags() {
        if (this.tags == null) {
            return new ArrayList<String>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public UserInput getUser_input() {
        return user_input;
    }

    public void setUser_input(UserInput user_input) {
        this.user_input = user_input;
    }

    public String getSource_name() {
        if (this.source_name == null || this.source_name.isEmpty()) {
            return "";
        }
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add(String.format("--- %s ---------------", this.event_uuid));
        strList.add("source: " + this.source);
        strList.add("result: " + this.result);
        strList.add("displayName: " + this.displayName);
        strList.add("Application: " + this.application.getName());
        strList.add("Server: " + this.server.getName());
        return String.join("\r\n", strList);
    }

}
