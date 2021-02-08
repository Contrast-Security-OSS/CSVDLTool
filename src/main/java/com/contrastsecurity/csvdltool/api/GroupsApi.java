package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.GroupsJson;
import com.contrastsecurity.csvdltool.model.CustomGroup;
import com.contrastsecurity.csvdltool.model.CustomGroups;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GroupsApi extends Api {

    public GroupsApi(IPreferenceStore preferenceStore) {
        super(preferenceStore);
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/groups?expand=users,applications,skip_links&q=&quickFilter=ALL", contrastUrl, orgId);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<GroupsJson>() {
        }.getType();
        GroupsJson groupsJson = gson.fromJson(response, contType);
        CustomGroups customGroups = groupsJson.getCustom_groups();
        if (customGroups != null) {
            return customGroups.getGroups();
        } else {
            return new ArrayList<CustomGroup>();
        }
    }

}
