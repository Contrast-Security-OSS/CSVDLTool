package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.TraceJson;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TraceApi extends Api {

    private String appId;
    private String trace_id;

    public TraceApi(IPreferenceStore preferenceStore, String appId, String trace_id) {
        super(preferenceStore);
        this.appId = appId;
        this.trace_id = trace_id;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/traces/%s/trace/%s?expand=events,notes,request,application,servers,server_environments,skip_links", contrastUrl, orgId, this.appId,
                this.trace_id);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type traceType = new TypeToken<TraceJson>() {
        }.getType();
        TraceJson traceJson = gson.fromJson(response, traceType);
        return traceJson.getTrace();
    }

}
