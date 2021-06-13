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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.eclipse.jface.preference.IPreferenceStore;

import com.contrastsecurity.csvdltool.json.OrganizationJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OrganizationApi extends Api {
    private String url;
    private String username;
    private String sevice_key;

    public OrganizationApi(IPreferenceStore preferenceStore, Organization organization, String url, String username, String sevice_key) {
        super(preferenceStore, organization);
        this.url = url;
        this.username = username;
        this.sevice_key = sevice_key;
    }

    @Override
    protected String getUrl() {
        String orgId = this.organization.getOrganization_uuid();
        return String.format("%s/api/ng/profile/organizations/%s?expand=freemium,skip_links", this.url, orgId);
    }

    @Override
    protected List<Header> getHeaders() {
        String auth = String.format("%s:%s", this.username, this.sevice_key);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = new String(encodedAuth);
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        headers.add(new BasicHeader("API-Key", this.organization.getApikey()));
        headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
        return headers;
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type organizationType = new TypeToken<OrganizationJson>() {
        }.getType();
        OrganizationJson organizationJson = gson.fromJson(response, organizationType);
        return organizationJson.getOrganization();
    }

}
