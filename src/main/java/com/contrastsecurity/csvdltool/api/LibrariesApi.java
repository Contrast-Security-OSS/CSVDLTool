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

import com.contrastsecurity.csvdltool.json.LibrariesJson;
import com.contrastsecurity.csvdltool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LibrariesApi extends Api {

    private String appId;
    private String filter;

    public LibrariesApi(IPreferenceStore preferenceStore, String appId, String filter) {
        super(preferenceStore);
        this.appId = appId;
        this.filter = filter;
    }

    @Override
    protected String getUrl() {
        String contrastUrl = preferenceStore.getString(PreferenceConstants.CONTRAST_URL);
        String orgId = preferenceStore.getString(PreferenceConstants.ORG_ID);
        return String.format("%s/api/ng/%s/applications/%s/libraries?expand=vulns,apps,skip_links&quickFilter=%s&sort=fileName", contrastUrl, orgId, this.appId, this.filter);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type librariesType = new TypeToken<LibrariesJson>() {
        }.getType();
        LibrariesJson librariesJson = gson.fromJson(response, librariesType);
        return librariesJson.getLibraries();
    }

}
