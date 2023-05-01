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

import com.contrastsecurity.csvdltool.json.LibrariesJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LibrariesApi extends Api {

    private final static int LIMIT = 50;

    private String appId;
    private String filter;
    private int offset;

    public LibrariesApi(Shell shell, IPreferenceStore ps, Organization org, String appId, String filter, int offset) {
        super(shell, ps, org);
        this.appId = appId;
        this.filter = filter;
        this.offset = offset;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/applications/%s/libraries?limit=%d&offset=%d&expand=servers,vulns,apps,skip_links&quickFilter=%s&sort=fileName", this.contrastUrl, orgId, //$NON-NLS-1$
                this.appId, LIMIT, this.offset, this.filter);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type librariesType = new TypeToken<LibrariesJson>() {
        }.getType();
        LibrariesJson librariesJson = gson.fromJson(response, librariesType);
        this.totalCount = librariesJson.getCount();
        return librariesJson.getLibraries();
    }

}
