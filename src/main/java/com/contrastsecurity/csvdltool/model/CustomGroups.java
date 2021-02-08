package com.contrastsecurity.csvdltool.model;

import java.util.ArrayList;
import java.util.List;

public class CustomGroups {
    private List<CustomGroup> groups;

    public List<CustomGroup> getGroups() {
        if (this.groups != null) {
            return groups;
        } else {
            return new ArrayList<CustomGroup>();
        }
    }

    public void setGroups(List<CustomGroup> groups) {
        this.groups = groups;
    }

}
