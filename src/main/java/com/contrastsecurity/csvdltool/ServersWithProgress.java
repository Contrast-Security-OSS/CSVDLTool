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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.ServersApi;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.contrastsecurity.csvdltool.model.Server;

public class ServersWithProgress implements IRunnableWithProgress {

    private PreferenceStore ps;
    private Shell shell;
    private List<Organization> orgs;
    private List<Server> allServers;
    private Set<Filter> agentVerFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> languageFilterSet = new LinkedHashSet<Filter>();

    Logger logger = Logger.getLogger("csvdltool");

    public ServersWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.allServers = new ArrayList<Server>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("サーバ一覧の読み込み...", 100 * this.orgs.size());
        for (Organization org : this.orgs) {
            try {
                List<Server> orgAttackEvents = new ArrayList<Server>();
                monitor.setTaskName(org.getName());
                // アプリケーション一覧を取得
                monitor.subTask("サーバ一覧の情報を取得...");
                Api attacksApi = new ServersApi(this.shell, this.ps, org, orgAttackEvents.size());
                List<Server> tmpAttackEvents = (List<Server>) attacksApi.get();
                orgAttackEvents.addAll(tmpAttackEvents);
                int totalCount = attacksApi.getTotalCount();
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
                sub1Monitor.beginTask("", totalCount);
                monitor.subTask(String.format("サーバ一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                sub1Monitor.worked(tmpAttackEvents.size());
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > orgAttackEvents.size();
                while (incompleteFlg) {
                    Thread.sleep(200);
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    attacksApi = new ServersApi(this.shell, this.ps, org, orgAttackEvents.size());
                    tmpAttackEvents = (List<Server>) attacksApi.get();
                    orgAttackEvents.addAll(tmpAttackEvents);
                    monitor.subTask(String.format("サーバ一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                    sub1Monitor.worked(tmpAttackEvents.size());
                    incompleteFlg = totalCount > orgAttackEvents.size();
                }
                sub1Monitor.done();
                this.allServers.addAll(orgAttackEvents);
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }

    public List<Server> getAllServers() {
        return this.allServers;
    }

    public Map<FilterEnum, Set<Filter>> getFilterMap() {
        for (Server server : this.allServers) {
            languageFilterSet.add(new Filter(server.getLanguage()));
            agentVerFilterSet.add(new Filter(server.getAgent_version()));
        }
        Map<FilterEnum, Set<Filter>> filterMap = new HashMap<FilterEnum, Set<Filter>>();
        filterMap.put(FilterEnum.LANGUAGE, languageFilterSet);
        filterMap.put(FilterEnum.AGENT_VERSION, agentVerFilterSet);
        return filterMap;
    }
}
