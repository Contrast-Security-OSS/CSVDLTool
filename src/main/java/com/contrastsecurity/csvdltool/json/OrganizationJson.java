package com.contrastsecurity.csvdltool.json;

import com.contrastsecurity.csvdltool.model.Organization;

public class OrganizationJson extends ContrastJson {
    private Organization organization;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return this.organization.toString();
    }

}
