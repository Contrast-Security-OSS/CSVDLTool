package com.contrastsecurity.csvdltool.model;

import java.util.ArrayList;
import java.util.List;

public class Organization {
    private String name;
    private String organization_uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization_uuid() {
        return organization_uuid;
    }

    public void setOrganization_uuid(String organization_uuid) {
        this.organization_uuid = organization_uuid;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("name: " + this.name);
        strList.add("organization_uuid: " + this.organization_uuid);
        return String.join(", ", strList);
    }

}
