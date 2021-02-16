package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.ApplicationTagsJson;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApplicationTagsApi extends Api {

    private String appId;

    public ApplicationTagsApi(IPreferenceStore preferenceStore, String appId) {
        super(preferenceStore);
        this.appId = appId;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/tags/application/list/%s", contrastUrl, orgId, this.appId);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type applicationTagsType = new TypeToken<ApplicationTagsJson>() {
        }.getType();
        ApplicationTagsJson applicationTagsJson = gson.fromJson(response, applicationTagsType);
        return applicationTagsJson.getTags();
    }

}
