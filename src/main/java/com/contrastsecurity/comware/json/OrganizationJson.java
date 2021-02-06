package com.contrastsecurity.comware.json;

import com.contrastsecurity.comware.model.Organization;

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
