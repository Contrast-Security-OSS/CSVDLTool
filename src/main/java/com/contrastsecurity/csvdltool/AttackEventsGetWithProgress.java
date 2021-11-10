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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackEventsApi;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;

public class AttackEventsGetWithProgress implements IRunnableWithProgress {

    private PreferenceStore preferenceStore;
    private List<Organization> organizations;
    private List<AttackEvent> attackEvents;
    private Set<Filter> sourceIpFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> applicationFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> ruleFilterSet = new LinkedHashSet<Filter>();

    Logger logger = Logger.getLogger("csvdltool");

    public AttackEventsGetWithProgress(PreferenceStore preferenceStore, List<Organization> organizations) {
        this.preferenceStore = preferenceStore;
        this.organizations = organizations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("攻撃イベント一覧の読み込み...", 100 * this.organizations.size());
        Thread.sleep(300);
        for (Organization org : this.organizations) {
            try {
                monitor.setTaskName(org.getName());
                List<AttackEvent> allAttackEvents = new ArrayList<AttackEvent>();
                // アプリケーション一覧を取得
                monitor.subTask("攻撃イベント一覧の情報を取得...");
                Api attacksApi = new AttackEventsApi(preferenceStore, org, allAttackEvents.size());
                attackEvents = (List<AttackEvent>) attacksApi.get();
                allAttackEvents.addAll((List<AttackEvent>) attacksApi.get());
                int totalCount = attacksApi.getTotalCount();
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
                sub1Monitor.beginTask("", totalCount);
                boolean incompleteFlg = false;
                incompleteFlg = totalCount > allAttackEvents.size();
                while (incompleteFlg) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    attacksApi = new AttackEventsApi(preferenceStore, org, allAttackEvents.size());
                    List<AttackEvent> attackEvents = (List<AttackEvent>) attacksApi.get();
                    sub1Monitor.worked(attackEvents.size());
                    allAttackEvents.addAll(attackEvents);
                    incompleteFlg = totalCount > allAttackEvents.size();
                    Thread.sleep(200);
                }
                sub1Monitor.done();
                System.out.println(allAttackEvents.size());
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }

    public List<AttackEvent> getAttackEvents() {
        return attackEvents;
    }

    public Map<FilterEnum, Set<Filter>> getFilterMap() {
        for (AttackEvent attackEvent : attackEvents) {
            sourceIpFilterSet.add(new Filter(attackEvent.getSource()));
            applicationFilterSet.add(new Filter(attackEvent.getApplication().getName()));
            ruleFilterSet.add(new Filter(attackEvent.getRule()));
        }
        Map<FilterEnum, Set<Filter>> filterMap = new HashMap<FilterEnum, Set<Filter>>();
        filterMap.put(FilterEnum.SOURCEIP, sourceIpFilterSet);
        filterMap.put(FilterEnum.APPLICATION, applicationFilterSet);
        filterMap.put(FilterEnum.RULE, ruleFilterSet);
        return filterMap;
    }

}
