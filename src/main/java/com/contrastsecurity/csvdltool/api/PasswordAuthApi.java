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

import com.contrastsecurity.csvdltool.json.ContrastJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PasswordAuthApi extends Api {

    private String password;

    public PasswordAuthApi(Shell shell, IPreferenceStore ps, Organization org, String contrastUrl, String userName, String password) {
        super(shell, ps, org);
        this.contrastUrl = contrastUrl;
        this.userName = userName;
        this.password = password;
    }

    @Override
    protected String getUrl() {
        return String.format("%s/authenticate.html", this.contrastUrl); //$NON-NLS-1$
    }

    @Override
    protected RequestBody getBody() throws Exception {
        return new FormBody.Builder().add("ui", "true").add("username", this.userName).add("password", this.password).add("sso", "").build(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
