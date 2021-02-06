package com.contrastsecurity.comware.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.comware.json.ApplicationJson;
import com.contrastsecurity.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApplicationsApi extends Api {
    public ApplicationsApi(IPreferenceStore preferenceStore) {
        super(preferenceStore);
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/applications?expand=modules,skip_links", contrastUrl, orgId);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ApplicationJson>() {
        }.getType();
        ApplicationJson applicationJson = gson.fromJson(response, contType);
        return applicationJson.getApplications();
    }

}
