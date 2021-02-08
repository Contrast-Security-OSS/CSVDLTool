package com.contrastsecurity.csvdltool.model;

import java.util.List;

public class CustomGroup {
    private String name;
    private List<Application> applications;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

}
