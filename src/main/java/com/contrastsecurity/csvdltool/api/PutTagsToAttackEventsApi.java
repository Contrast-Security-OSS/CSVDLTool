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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.json.ContrastJson;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PutTagsToAttackEventsApi extends Api {

    private List<AttackEvent> attackEvents;
    private String tag;
    private List<String> removeTags;

    public PutTagsToAttackEventsApi(Shell shell, IPreferenceStore ps, Organization org, List<AttackEvent> attackEvents, String tag, List<String> removeTags) {
        super(shell, ps, org);
        this.attackEvents = attackEvents;
        this.tag = tag;
        this.removeTags = removeTags;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/tags/attack/events/bulk?expand=skip_links", this.contrastUrl, orgId); //$NON-NLS-1$
    }

    @Override
    protected RequestBody getBody() throws Exception {
        String addTag = ""; //$NON-NLS-1$
        if (!this.tag.isEmpty()) {
            addTag = String.format("\"%s\"", this.tag); //$NON-NLS-1$
        }
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8"); //$NON-NLS-1$
        String json = String.format("{\"attack_events_uuid\":[%s],\"tags\":[%s],\"tags_remove\":[%s]}", //$NON-NLS-1$
                this.attackEvents.stream().map(ae -> ae.getEvent_uuid()).collect(Collectors.joining("\",\"", "\"", "\"")), addTag, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                this.removeTags.stream().map(tag -> tag).collect(Collectors.joining("\",\"", "\"", "\""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return RequestBody.create(json, mediaTypeJson);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ContrastJson>() {
        }.getType();
        ContrastJson contrastJson = gson.fromJson(response, contType);
        return contrastJson.getSuccess();
    }

}
