package com.contrastsecurity.csvdltool.json;

import com.contrastsecurity.csvdltool.model.CustomGroups;

public class GroupsJson extends ContrastJson {
    private CustomGroups custom_groups;

    public CustomGroups getCustom_groups() {
        return custom_groups;
    }

    public void setCustom_groups(CustomGroups custom_groups) {
        this.custom_groups = custom_groups;
    }

}
