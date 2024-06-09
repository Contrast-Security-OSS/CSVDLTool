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

package com.contrastsecurity.csvdltool;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.scan.ProjectsApi;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.scan.ScanProject;

public class ScanProjectsGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private Map<String, ScanProjectInfo> fullProjectMap;
    private boolean targetArchivedProj;

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

    public ScanProjectsGetWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, boolean targetArchivedProj) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.targetArchivedProj = targetArchivedProj;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        fullProjectMap = new TreeMap<String, ScanProjectInfo>();
        SubMonitor subMonitor = SubMonitor.convert(monitor).setWorkRemaining(100 * this.orgs.size());
        monitor.setTaskName(Messages.getString("appsgetwithprogress.progress.loading.applications")); //$NON-NLS-1$
        Thread.sleep(300);
        for (Organization org : this.orgs) {
            try {
                List<ScanProject> orgProjects = new ArrayList<ScanProject>();
                monitor.setTaskName(org.getName());
                // プロジェクト一覧を取得
                monitor.subTask("プロジェクト一覧の情報を取得...");
                Api projectsApi = new ProjectsApi(this.shell, this.ps, org, this.targetArchivedProj, 0);
                List<ScanProject> tmpProjects = (List<ScanProject>) projectsApi.get();
                System.out.println(tmpProjects.size());
                orgProjects.addAll(tmpProjects);
                int totalCount = projectsApi.getTotalCount();
                System.out.println(totalCount);
                SubMonitor child1Monitor = subMonitor.split(100).setWorkRemaining(totalCount);
                monitor.subTask(String.format("%s(%d/%d)", "プロジェクト一覧の情報を取得...", orgProjects.size(), totalCount)); //$NON-NLS-1$
                child1Monitor.worked(tmpProjects.size());
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > orgProjects.size();
                while (incompleteFlg) {
                    Thread.sleep(200);
                    if (subMonitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    projectsApi = new ProjectsApi(this.shell, this.ps, org, this.targetArchivedProj, orgProjects.size());
                    tmpProjects = (List<ScanProject>) projectsApi.get();
                    System.out.println(tmpProjects.size());
                    orgProjects.addAll(tmpProjects);
                    monitor.subTask(String.format("%s(%d/%d)", "プロジェクト一覧の情報を取得...", orgProjects.size(), totalCount)); //$NON-NLS-1$
                    child1Monitor.worked(tmpProjects.size());
                    incompleteFlg = totalCount > orgProjects.size();
                }
                child1Monitor.done();
                for (ScanProject scanProj : orgProjects) {
                    System.out.println(scanProj.getName());
                    fullProjectMap.put(scanProj.getName(), new ScanProjectInfo(org, scanProj.getId(), scanProj.getName())); // $NON-NLS-1$
                }
                Thread.sleep(500);
            } catch (OperationCanceledException oce) {
                throw new InvocationTargetException(new OperationCanceledException(Messages.getString("appsgetwithprogress.progress.canceled")));
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        subMonitor.done();
    }

    public Map<String, ScanProjectInfo> getFullProjectMap() {
        return fullProjectMap;
    }

}
