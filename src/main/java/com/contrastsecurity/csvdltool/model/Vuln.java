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

public class Vuln {
    private String name;
    private String description;
    private String severity_value;
    private String severity_code;
    private String authentication;
    private String access_vector;
    private String access_complexity;
    private String availability_impact;
    private String confidentiality_impact;
    private String integrity_impact;
    private float epss_score;
    private float epss_percentile;
    private boolean cisa;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity_value() {
        return severity_value;
    }

    public void setSeverity_value(String severity_value) {
        this.severity_value = severity_value;
    }

    public String getSeverity_code() {
        return severity_code;
    }

    public void setSeverity_code(String severity_code) {
        this.severity_code = severity_code;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getAccess_vector() {
        return access_vector;
    }

    public void setAccess_vector(String access_vector) {
        this.access_vector = access_vector;
    }

    public String getAccess_complexity() {
        return access_complexity;
    }

    public void setAccess_complexity(String access_complexity) {
        this.access_complexity = access_complexity;
    }

    public String getAvailability_impact() {
        return availability_impact;
    }

    public void setAvailability_impact(String availability_impact) {
        this.availability_impact = availability_impact;
    }

    public String getConfidentiality_impact() {
        return confidentiality_impact;
    }

    public void setConfidentiality_impact(String confidentiality_impact) {
        this.confidentiality_impact = confidentiality_impact;
    }

    public String getIntegrity_impact() {
        return integrity_impact;
    }

    public void setIntegrity_impact(String integrity_impact) {
        this.integrity_impact = integrity_impact;
    }

    public float getEpss_score() {
        return epss_score;
    }

    public void setEpss_score(float epss_score) {
        this.epss_score = epss_score;
    }

    public float getEpss_percentile() {
        return epss_percentile;
    }

    public void setEpss_percentile(float epss_percentile) {
        this.epss_percentile = epss_percentile;
    }

    public boolean isCisa() {
        return cisa;
    }

    public void setCisa(boolean cisa) {
        this.cisa = cisa;
    }

}
