package com.contrastsecurity.csvdltool.model;

import java.util.List;

public class CustomGroup {
    private String name;
    private List<ApplicationInCustomGroup> applications;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApplicationInCustomGroup> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationInCustomGroup> applications) {
        this.applications = applications;
    }

}
