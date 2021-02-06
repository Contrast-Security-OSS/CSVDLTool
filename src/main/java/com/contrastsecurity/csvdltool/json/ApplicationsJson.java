package com.contrastsecurity.csvdltool.json;

import java.util.List;
import java.util.StringJoiner;

import com.contrastsecurity.csvdltool.model.Application;

public class ApplicationsJson extends ContrastJson {
    private List<Application> applications;

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        for (Application a : this.applications) {
            sj.add(a.toString());
        }
        return sj.toString();
    }

}
