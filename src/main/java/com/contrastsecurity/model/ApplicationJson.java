package com.contrastsecurity.model;

import java.util.List;
import java.util.StringJoiner;

public class ApplicationJson extends Contrast {
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
