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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.json.ResultDataJson;
import com.contrastsecurity.csvdltool.model.Account;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ResultsApi extends Api {

    private final static int LIMIT = 50;

    private String serverLessApiHost;
    private String token;
    private String provider;
    private String accountId;
    private String region;

    public ResultsApi(Shell shell, IPreferenceStore ps, Organization org, Account account) {
        super(shell, ps, org);
        this.serverLessApiHost = ps.getString(PreferenceConstants.SERVERLESS_HOST);
        this.token = ps.getString(PreferenceConstants.SERVERLESS_TOKEN);
        this.provider = account.getProvider();
        this.accountId = account.getAccountId();
        this.region = account.getAgentRegion();
    }

    @Override
    protected List<Header> getHeaders() {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json")); //$NON-NLS-1$
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json")); //$NON-NLS-1$
        headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, String.format("jwt %s", this.token)));
        return headers;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/v1/organizations/%s/providers/%s/accounts/%s/regions/%s/results?limit=%d", this.serverLessApiHost, orgId, this.provider, this.accountId, //$NON-NLS-1$
                this.region, LIMIT);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ResultDataJson>() {
        }.getType();
        ResultDataJson json = gson.fromJson(response, contType);
        return json.getResults();
    }

}
