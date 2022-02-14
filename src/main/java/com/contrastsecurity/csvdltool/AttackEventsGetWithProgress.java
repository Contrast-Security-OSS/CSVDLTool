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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;

import com.contrastsecurity.csvdltool.api.Api;
import com.contrastsecurity.csvdltool.api.AttackApi;
import com.contrastsecurity.csvdltool.api.AttackEventsByAttackUuidApi;
import com.contrastsecurity.csvdltool.api.AttacksApi;
import com.contrastsecurity.csvdltool.model.Attack;
import com.contrastsecurity.csvdltool.model.AttackEvent;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;

public class AttackEventsGetWithProgress implements IRunnableWithProgress {

    private PreferenceStore preferenceStore;
    private List<Organization> organizations;
    private Date frDetectedDate;
    private Date toDetectedDate;
    private List<AttackEvent> allAttackEvents;
    private Set<Filter> sourceNameFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> sourceIpFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> applicationFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> ruleFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> tagFilterSet = new LinkedHashSet<Filter>();

    Logger logger = Logger.getLogger("csvdltool");

    public AttackEventsGetWithProgress(PreferenceStore preferenceStore, List<Organization> organizations, Date frDate, Date toDate) {
        this.preferenceStore = preferenceStore;
        this.organizations = organizations;
        this.frDetectedDate = frDate;
        this.toDetectedDate = toDate;
        this.allAttackEvents = new ArrayList<AttackEvent>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("攻撃イベント一覧の読み込み...", 100 * this.organizations.size());
        for (Organization org : this.organizations) {
            try {
                // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                // String startDateStr = "2022-02-01";
                // Date startDate = dateFormat.parse(startDateStr);
                // String endDateStr = "2022-02-28";
                // Date endDate = dateFormat.parse(endDateStr);
                // 攻撃一覧を読み込み
                List<Attack> allAttacks = new ArrayList<Attack>();
                Api attackssApi = new AttacksApi(preferenceStore, org, frDetectedDate, toDetectedDate, 0);
                List<Attack> tmpAttacks = (List<Attack>) attackssApi.post();
                for (Attack atck : tmpAttacks) {
                    Api attackApi = new AttackApi(preferenceStore, org, atck.getUuid());
                    Attack attackDetail = (Attack) attackApi.get();
                    atck.setSource_name(attackDetail.getSource_name());
                }
                allAttacks.addAll(tmpAttacks);
                int totalAttackCount = attackssApi.getTotalCount();
                boolean attackIncompleteFlg = false;
                attackIncompleteFlg = totalAttackCount > allAttacks.size();
                while (attackIncompleteFlg) {
                    Thread.sleep(100);
                    attackssApi = new AttacksApi(preferenceStore, org, frDetectedDate, toDetectedDate, allAttacks.size());
                    tmpAttacks = (List<Attack>) attackssApi.post();
                    for (Attack atck : tmpAttacks) {
                        Api attackApi = new AttackApi(preferenceStore, org, atck.getUuid());
                        Attack attackDetail = (Attack) attackApi.get();
                        atck.setSource_name(attackDetail.getSource_name());
                    }
                    attackIncompleteFlg = totalAttackCount > allAttacks.size();
                }

                // 攻撃イベント一覧を読み込み
                List<AttackEvent> orgAttackEvents = new ArrayList<AttackEvent>();
                monitor.setTaskName(org.getName());
                monitor.subTask("攻撃イベント一覧の情報を取得...");
                for (Attack attack : allAttacks) {
                    Api attackEventsApi = new AttackEventsByAttackUuidApi(preferenceStore, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                    List<AttackEvent> tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                    for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                        tmpAttackEvent.setSource_name(attack.getSource_name());
                    }
                    orgAttackEvents.addAll(tmpAttackEvents);
                    int totalCount = attackEventsApi.getTotalCount();
                    SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
                    sub1Monitor.beginTask("", totalCount);
                    monitor.subTask(String.format("攻撃イベント一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                    sub1Monitor.worked(tmpAttackEvents.size());
                    boolean incompleteFlg = false;
                    incompleteFlg = totalCount > orgAttackEvents.size();
                    while (incompleteFlg) {
                        Thread.sleep(200);
                        if (monitor.isCanceled()) {
                            throw new InterruptedException("キャンセルされました。");
                        }
                        attackEventsApi = new AttackEventsByAttackUuidApi(preferenceStore, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                        tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                        for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                            tmpAttackEvent.setSource_name(attack.getSource_name());
                        }
                        orgAttackEvents.addAll(tmpAttackEvents);
                        monitor.subTask(String.format("攻撃イベント一覧の情報を取得...(%d/%d)", orgAttackEvents.size(), totalCount));
                        sub1Monitor.worked(tmpAttackEvents.size());
                        incompleteFlg = totalCount > orgAttackEvents.size();
                    }
                    sub1Monitor.done();
                    this.allAttackEvents.addAll(orgAttackEvents);

                }
                Thread.sleep(500);
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
