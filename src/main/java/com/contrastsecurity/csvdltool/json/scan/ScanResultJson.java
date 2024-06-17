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

package com.contrastsecurity.csvdltool.json.scan;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.contrastsecurity.csvdltool.json.ContrastJson;
import com.contrastsecurity.csvdltool.model.scan.Audit;

public class ScanResultJson extends ContrastJson {
    private String id;
    private String scanId;
    private String organizationId;
    private String projectId;
    private String ruleId;
    private String name;
    private String language;
    private String severity;
    private String status;
    private String firstCreatedTime;
    private String lastSeenTime;
    private Map<String, String> message;
    private String category;
    private String confidence;
    private String risk;
    private Map<String, List<String>> standards;
    private List<String> cwe;
    private List<String> owasp;
    private List<String> reference;
    private String recommendation;
    private List<Audit> audit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public Map<String, String> getMessage() {
        return message;
    }

    public void setMessage(Map<String, String> message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n"); //$NON-NLS-1$
        return sj.toString();
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getFirstCreatedTime() {
        return firstCreatedTime;
    }

    public void setFirstCreatedTime(String firstCreatedTime) {
        this.firstCreatedTime = firstCreatedTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public Map<String, List<String>> getStandards() {
        return standards;
    }

    public void setStandards(Map<String, List<String>> standards) {
        this.standards = standards;
    }

    public List<String> getCwe() {
        return cwe;
    }

    public void setCwe(List<String> cwe) {
        this.cwe = cwe;
    }

    public List<String> getOwasp() {
        return owasp;
    }

    public void setOwasp(List<String> owasp) {
        this.owasp = owasp;
    }

    public List<String> getReference() {
        return reference;
    }

    public void setReference(List<String> reference) {
        this.reference = reference;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public List<Audit> getAudit() {
        return audit;
    }

    public void setAudit(List<Audit> audit) {
        this.audit = audit;
    }

    public List<Audit> sortedAudits() {
        List<Audit> list = this.audit.stream().collect(Collectors.toList());
        Collections.sort(list, new Comparator<Audit>() {
            @Override
            public int compare(Audit a1, Audit a2) {
                return a2.getDateModified().compareTo(a1.getDateModified());
            }
        });
        return list;
    }
}
