package com.contrastsecurity.comware.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.comware.json.HowToFixJson;
import com.contrastsecurity.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HowToFixApi extends Api {

    private String trace_id;

    public HowToFixApi(IPreferenceStore preferenceStore, String trace_id) {
        super(preferenceStore);
        this.trace_id = trace_id;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/traces/%s/recommendation", contrastUrl, orgId, this.trace_id);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type howToFixType = new TypeToken<HowToFixJson>() {
        }.getType();
        HowToFixJson howToFixJson = gson.fromJson(response, howToFixType);
        return howToFixJson;
    }

}
