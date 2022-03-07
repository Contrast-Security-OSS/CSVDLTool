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
import java.util.Date;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.json.AttackEventsJson;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class AttackEventsByAttackUuidApi extends Api {

    private final static int LIMIT = 1000;
    private String attackUuid;
    private Date startDate;
    private Date endDate;
    private int offset;

    public AttackEventsByAttackUuidApi(Shell shell, IPreferenceStore ps, Organization org, String attackUuid, Date startDate, Date endDate, int offset) {
        super(shell, ps, org);
        this.attackUuid = attackUuid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.offset = offset;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/rasp/events/new?expand=drilldownDetails,application_roles,tags,skip_links&limit=%d&offset=%d&sort=-timestamp", this.contrastUrl, orgId,
                LIMIT, this.offset);
    }

    @Override
    protected RequestBody getBody() {
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8");
        String json = String.format("{\"attackUuid\":\"%s\",\"quickFilter\":\"ALL\",\"startDate\":\"%s\",\"endDate\":\"%s\"}", this.attackUuid, this.startDate.getTime(),
                this.endDate.getTime());
        return RequestBody.create(json, mediaTypeJson);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<AttackEventsJson>() {
        }.getType();
        AttackEventsJson attackEventsJson = gson.fromJson(response, contType);
        this.totalCount = attackEventsJson.getCount();
        List<AttackEvent> attackEvents = attackEventsJson.getEvents();
        for (AttackEvent attackEvent : attackEvents) {
            attackEvent.setOrganization(this.org);
        }
        return attackEvents;
    }

}
