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

package com.contrastsecurity.csvdltool.api.scan;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.json.scan.ProjectsJson;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.scan.ScanProject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProjectsApi extends Api {

    private final static int LIMIT = 10;
    private boolean targetArchivedProj;
    private int offset;

    public ProjectsApi(Shell shell, IPreferenceStore ps, Organization org, boolean targetArchivedProj, int offset) {
        super(shell, ps, org);
        this.offset = offset;
        this.targetArchivedProj = targetArchivedProj;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        int page = this.offset / LIMIT;
        if (this.targetArchivedProj) {
            return String.format("%s/api/sast/organizations/%s/projects?page=%d&size=%d&sort=name,asc", this.contrastUrl, orgId, page, LIMIT); //$NON-NLS-1$
        } else {
            return String.format("%s/api/sast/organizations/%s/projects?page=%d&size=%d&archived=false&sort=name,asc", this.contrastUrl, orgId, page, LIMIT); //$NON-NLS-1$
        }
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ProjectsJson>() {
        }.getType();
        ProjectsJson projectsJson = gson.fromJson(response, contType);
        this.totalCount = projectsJson.getTotalElements();
        List<ScanProject> projects = projectsJson.getProjects();
        return projects;
    }

}
