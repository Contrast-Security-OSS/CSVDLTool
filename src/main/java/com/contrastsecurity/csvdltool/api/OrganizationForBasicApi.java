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

import com.contrastsecurity.csvdltool.json.OrganizationsJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OrganizationForBasicApi extends Api {
    public OrganizationForBasicApi(Shell shell, IPreferenceStore ps, Organization org, String contrastUrl, String userName) {
        super(shell, ps, org);
        this.contrastUrl = contrastUrl;
        this.userName = userName;
    }

    @Override
    protected String getUrl() {
        return String.format("%s/api/ng/profile/organizations?expand=skip_links", this.contrastUrl);
    }

    @Override
    protected Object convert(String response) {
        String orgId = this.org.getOrganization_uuid();
        Gson gson = new Gson();
        Type organizationsType = new TypeToken<OrganizationsJson>() {
        }.getType();
        OrganizationsJson organizationsJson = gson.fromJson(response, organizationsType);
        for (Organization org : organizationsJson.getOrganizations()) {
            if (org.getOrganization_uuid().equals(orgId)) {
                return org;
            }
        }
        return null;
    }

}
