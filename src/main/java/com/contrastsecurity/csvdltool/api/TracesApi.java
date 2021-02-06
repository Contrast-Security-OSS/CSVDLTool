package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.TracesJson;
import com.contrastsecurity.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TracesApi extends Api {

    private String appId;

    public TracesApi(IPreferenceStore preferenceStore, String appId) {
        super(preferenceStore);
        this.appId = appId;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/traces/%s/ids", contrastUrl, orgId, this.appId);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type tracesType = new TypeToken<TracesJson>() {
        }.getType();
        TracesJson tracesJson = gson.fromJson(response, tracesType);
        return tracesJson.getTraces();
    }

}
