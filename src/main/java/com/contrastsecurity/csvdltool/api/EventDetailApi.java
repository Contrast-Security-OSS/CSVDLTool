/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.csvdltool.api;

import java.lang.reflect.Type;

import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.EventDetailJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EventDetailApi extends Api {

    private String trace_id;
    private String event_id;

    public EventDetailApi(IPreferenceStore preferenceStore, Organization organization, String trace_id, String event_id) {
        super(preferenceStore, organization);
        this.trace_id = trace_id;
        this.event_id = event_id;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = this.organization.getOrganization_uuid();
        return String.format("%s/api/ng/%s/traces/%s/events/%s/details?expand=skip_links", contrastUrl, orgId, this.trace_id, this.event_id);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type eventDetailType = new TypeToken<EventDetailJson>() {
        }.getType();
        EventDetailJson eventDetailJson = gson.fromJson(response, eventDetailType);
        return eventDetailJson.getEvent();
    }

}