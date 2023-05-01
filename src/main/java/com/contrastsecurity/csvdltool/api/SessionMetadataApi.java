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
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.json.SessionMetadataJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SessionMetadataApi extends Api {

    private String appId;
    private String trace_id;

    public SessionMetadataApi(Shell shell, IPreferenceStore ps, Organization org, String appId, String trace_id) {
        super(shell, ps, org);
        this.appId = appId;
        this.trace_id = trace_id;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/traces/%s/trace/%s/metadata?expand=skip_links", this.contrastUrl, orgId, this.appId, this.trace_id); //$NON-NLS-1$
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type sessionMetadataType = new TypeToken<SessionMetadataJson>() {
        }.getType();
        SessionMetadataJson sessionMetadataJson = gson.fromJson(response, sessionMetadataType);
        return sessionMetadataJson.getSession().getMetadata();
    }

}
