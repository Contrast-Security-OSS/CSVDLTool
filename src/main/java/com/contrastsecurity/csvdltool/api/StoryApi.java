package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.StoryJson;
import com.contrastsecurity.csvdltool.model.Chapter;
import com.contrastsecurity.csvdltool.model.Risk;
import com.contrastsecurity.csvdltool.model.Story;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class StoryApi extends Api {

    private String trace_id;

    public StoryApi(IPreferenceStore preferenceStore, String trace_id) {
        super(preferenceStore);
        this.trace_id = trace_id;
    }

    @Override
    public Object get() throws Exception {
        try {
            String response = this.getResponse();
            return this.convert(response);
        } catch (Exception e) {
            Risk risk = new Risk();
            risk.setText("取得に失敗しました。");
            Story story = new Story();
            story.setRisk(risk);
            story.setChapters(new ArrayList<Chapter>());
            return story;
        }
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/traces/%s/story", contrastUrl, orgId, this.trace_id);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type storyType = new TypeToken<StoryJson>() {
        }.getType();
        StoryJson storyJson = gson.fromJson(response, storyType);
        return storyJson.getStory();
    }

}
