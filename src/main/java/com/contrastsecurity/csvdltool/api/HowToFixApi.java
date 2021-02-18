package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.HowToFixJson;
import com.contrastsecurity.csvdltool.model.Recommendation;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HowToFixApi extends Api {

    private String trace_id;

    public HowToFixApi(IPreferenceStore preferenceStore, String trace_id) {
        super(preferenceStore);
        this.trace_id = trace_id;
    }

    @Override
    public Object get() throws Exception {
        try {
            String response = this.getResponse();
            return this.convert(response);
        } catch (Exception e) {
            Recommendation recommendation = new Recommendation();
            recommendation.setText("取得に失敗しました。");
            HowToFixJson howToFixJson = new HowToFixJson();
            howToFixJson.setRecommendation(recommendation);
            howToFixJson.setCwe("");
            howToFixJson.setOwasp("");
            return howToFixJson;
        }
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
