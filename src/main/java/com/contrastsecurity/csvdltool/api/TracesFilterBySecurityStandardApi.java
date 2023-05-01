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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.json.TraceFilterBySecurityStandardJson;
import com.contrastsecurity.csvdltool.model.ItemForVulnerability;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Vulnerability;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TracesFilterBySecurityStandardApi extends Api {

    private final static int LIMIT = 15;
    private String appId;
    private String securityStandardName;
    private int offset;

    public TracesFilterBySecurityStandardApi(Shell shell, IPreferenceStore ps, Organization org, String appId, String securityStandardName, int offset) {
        super(shell, ps, org);
        this.appId = appId;
        this.securityStandardName = securityStandardName;
        this.offset = offset;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/organizations/%s/orgtraces/ui?expand=limit=%d&offset=%d", this.contrastUrl, orgId, LIMIT, this.offset); //$NON-NLS-1$
    }

    @Override
    protected RequestBody getBody() throws Exception {
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8"); //$NON-NLS-1$
        String json = String.format("{\"quickFilter\":\"ALL\",\"modules\":[\"%s\"],\"securityStandards\":[\"%s\"]}", this.appId, this.securityStandardName); //$NON-NLS-1$
        return RequestBody.create(json, mediaTypeJson);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type type = new TypeToken<TraceFilterBySecurityStandardJson>() {
        }.getType();
        TraceFilterBySecurityStandardJson json = gson.fromJson(response, type);
        this.totalCount = json.getCount();
        List<Vulnerability> vulns = new ArrayList<Vulnerability>();
        for (ItemForVulnerability item : json.getItems()) {
            vulns.add(item.getVulnerability());
        }
        return vulns;
    }

}
