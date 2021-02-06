package com.contrastsecurity.csvdltool.model;

import java.util.ArrayList;
import java.util.List;

public class Story {
    private String traceId;
    private List<Chapter> chapters;
    private Risk risk;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add(this.traceId);
        strList.add(this.risk.toString());
        return String.join("\r\n", strList);
    }
}
