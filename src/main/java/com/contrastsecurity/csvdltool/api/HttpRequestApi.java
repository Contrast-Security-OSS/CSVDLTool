package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.HttpRequestJson;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpRequestApi extends Api {

    private String trace_id;

    public HttpRequestApi(IPreferenceStore preferenceStore, String trace_id) {
        super(preferenceStore);
        this.trace_id = trace_id;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/traces/%s/httprequest?expand=skip_links", contrastUrl, orgId, this.trace_id);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type httpRequestType = new TypeToken<HttpRequestJson>() {
        }.getType();
        HttpRequestJson httpRequestJson = gson.fromJson(response, httpRequestType);
        return httpRequestJson.getHttp_request();
    }

}
