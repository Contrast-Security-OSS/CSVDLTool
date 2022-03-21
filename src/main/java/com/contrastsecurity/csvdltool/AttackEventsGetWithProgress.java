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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackApi;
import com.contrastsecurity.csvdltool.api.AttackEventsByAttackUuidApi;
import com.contrastsecurity.csvdltool.api.AttacksApi;
import com.contrastsecurity.csvdltool.model.Attack;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;

public class AttackEventsGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private Date frDetectedDate;
    private Date toDetectedDate;
    private List<AttackEvent> allAttackEvents;
    private Set<Filter> sourceNameFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> sourceIpFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> applicationFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> ruleFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> tagFilterSet = new LinkedHashSet<Filter>();

    Logger logger = LogManager.getLogger("csvdltool");

    public AttackEventsGetWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, Date frDate, Date toDate) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.frDetectedDate = frDate;
        this.toDetectedDate = toDate;
        this.allAttackEvents = new ArrayList<AttackEvent>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("攻撃イベント一覧の読み込み...", 100 * this.orgs.size());
        for (Organization org : this.orgs) {
            try {
                monitor.setTaskName(String.format("[%s] 攻撃イベント一覧の読み込み", org.getName()));
                // 攻撃一覧を読み込み
                monitor.subTask("攻撃一覧の情報を取得...");
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 30);
                List<Attack> allAttacks = new ArrayList<Attack>();
                Api attackssApi = new AttacksApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, 0);
                List<Attack> tmpAttacks = (List<Attack>) attackssApi.post();
                int totalAttackCount = attackssApi.getTotalCount();
                sub1Monitor.beginTask("", totalAttackCount);
                allAttacks.addAll(tmpAttacks);
                for (Attack atck : tmpAttacks) {
                    Api attackApi = new AttackApi(this.shell, this.ps, org, atck.getUuid());
                    Attack attackDetail = (Attack) attackApi.get();
                    atck.setSource_name(attackDetail.getSource_name());
                }
                sub1Monitor.worked(tmpAttacks.size());
                boolean attackIncompleteFlg = false;
                attackIncompleteFlg = totalAttackCount > allAttacks.size();
                while (attackIncompleteFlg) {
                    Thread.sleep(100);
                    attackssApi = new AttacksApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, allAttacks.size());
                    tmpAttacks = (List<Attack>) attackssApi.post();
                    allAttacks.addAll(tmpAttacks);
                    for (Attack atck : tmpAttacks) {
                        Api attackApi = new AttackApi(this.shell, this.ps, org, atck.getUuid());
                        Attack attackDetail = (Attack) attackApi.get();
                        atck.setSource_name(attackDetail.getSource_name());
                    }
                    sub1Monitor.worked(tmpAttacks.size());
                    attackIncompleteFlg = totalAttackCount > allAttacks.size();
                }
                sub1Monitor.done();

                // 攻撃イベント一覧を読み込み
                monitor.subTask("攻撃イベント一覧の情報を取得...");
                SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 70);
                sub2Monitor.beginTask("", allAttacks.size());
                for (Attack attack : allAttacks) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    List<AttackEvent> orgAttackEvents = new ArrayList<AttackEvent>();
                    Api attackEventsApi = new AttackEventsByAttackUuidApi(this.shell, this.ps, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                    List<AttackEvent> tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                    for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                        tmpAttackEvent.setSource_name(attack.getSource_name());
                    }
                    orgAttackEvents.addAll(tmpAttackEvents);
                    int totalCount = attackEventsApi.getTotalCount();
                    monitor.subTask(String.format("攻撃イベント一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                    boolean incompleteFlg = false;
                    incompleteFlg = totalCount > orgAttackEvents.size();
                    while (incompleteFlg) {
                        Thread.sleep(100);
                        if (monitor.isCanceled()) {
                            throw new InterruptedException("キャンセルされました。");
                        }
                        attackEventsApi = new AttackEventsByAttackUuidApi(this.shell, this.ps, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                        tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                        for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                            tmpAttackEvent.setSource_name(attack.getSource_name());
                        }
                        orgAttackEvents.addAll(tmpAttackEvents);
                        monitor.subTask(String.format("攻撃イベント一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                        incompleteFlg = totalCount > orgAttackEvents.size();
                    }
                    sub2Monitor.worked(1);
                    this.allAttackEvents.addAll(orgAttackEvents);
                }
                sub2Monitor.done();
                Thread.sleep(100);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }

    public List<AttackEvent> getAllAttackEvents() {
        return this.allAttackEvents;
    }

    public Map<FilterEnum, Set<Filter>> getFilterMap() {
        for (AttackEvent attackEvent : this.allAttackEvents) {
            sourceNameFilterSet.add(new Filter(attackEvent.getSource_name()));
            sourceIpFilterSet.add(new Filter(attackEvent.getSource()));
            applicationFilterSet.add(new Filter(attackEvent.getApplication().getName()));
            ruleFilterSet.add(new Filter(attackEvent.getRule()));
            if (attackEvent.getTags().isEmpty()) {
                tagFilterSet.add(new Filter(""));
            } else {
                for (String tag : attackEvent.getTags()) {
                    tagFilterSet.add(new Filter(tag));
                }
            }
        }
        Map<FilterEnum, Set<Filter>> filterMap = new HashMap<FilterEnum, Set<Filter>>();
        filterMap.put(FilterEnum.SOURCE_NAME, sourceNameFilterSet);
        filterMap.put(FilterEnum.SOURCE_IP, sourceIpFilterSet);
        filterMap.put(FilterEnum.APPLICATION, applicationFilterSet);
        filterMap.put(FilterEnum.RULE, ruleFilterSet);
        filterMap.put(FilterEnum.TAG, tagFilterSet);
        return filterMap;
    }

}
