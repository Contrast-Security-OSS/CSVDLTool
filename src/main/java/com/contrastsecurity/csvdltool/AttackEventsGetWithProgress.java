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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
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

    Logger logger = LogManager.getLogger("csvdltool"); //$NON-NLS-1$

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
        SubMonitor subMonitor = SubMonitor.convert(monitor).setWorkRemaining(100 * this.orgs.size());
        monitor.setTaskName(Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.organization.name")); //$NON-NLS-1$
        for (Organization org : this.orgs) {
            try {
                monitor.setTaskName(String.format("%s %s", org.getName(), //$NON-NLS-1$
                        Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.organization.name"))); //$NON-NLS-1$
                // 攻撃一覧を読み込み
                monitor.subTask(Messages.getString("attackeventsgetwithprogress.progress.loading.attacks")); //$NON-NLS-1$
                List<Attack> allAttacks = new ArrayList<Attack>();
                Api attacksApi = new AttacksApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, 0);
                List<Attack> tmpAttacks = (List<Attack>) attacksApi.post();
                int totalAttackCount = attacksApi.getTotalCount();
                int attackProcessCount = 0;
                monitor.subTask(String.format("%s(%d/%d)", Messages.getString("attackeventsgetwithprogress.progress.loading.attacks"), attackProcessCount, totalAttackCount)); //$NON-NLS-1$ //$NON-NLS-2$
                SubMonitor child1Monitor = subMonitor.split(15).setWorkRemaining(totalAttackCount);
                allAttacks.addAll(tmpAttacks);
                for (Attack atck : tmpAttacks) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    Api attackApi = new AttackApi(this.shell, this.ps, org, atck.getUuid());
                    Attack attackDetail = (Attack) attackApi.get();
                    atck.setSource_name(attackDetail.getSource_name());
                    attackProcessCount++;
                    monitor.subTask(String.format("%s(%d/%d)", Messages.getString("attackeventsgetwithprogress.progress.loading.attacks"), attackProcessCount, totalAttackCount)); //$NON-NLS-1$ //$NON-NLS-2$
                    child1Monitor.worked(1);
                    Thread.sleep(100);
                }
                boolean attackIncompleteFlg = false;
                attackIncompleteFlg = totalAttackCount > allAttacks.size();
                while (attackIncompleteFlg) {
                    Thread.sleep(100);
                    attacksApi = new AttacksApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, allAttacks.size());
                    tmpAttacks = (List<Attack>) attacksApi.post();
                    allAttacks.addAll(tmpAttacks);
                    for (Attack atck : tmpAttacks) {
                        if (monitor.isCanceled()) {
                            throw new OperationCanceledException();
                        }
                        Api attackApi = new AttackApi(this.shell, this.ps, org, atck.getUuid());
                        Attack attackDetail = (Attack) attackApi.get();
                        atck.setSource_name(attackDetail.getSource_name());
                        attackProcessCount++;
                        monitor.subTask(
                                String.format("%s(%d/%d)", Messages.getString("attackeventsgetwithprogress.progress.loading.attacks"), attackProcessCount, totalAttackCount)); //$NON-NLS-1$ //$NON-NLS-2$
                        child1Monitor.worked(1);
                        Thread.sleep(100);
                    }
                    attackIncompleteFlg = totalAttackCount > allAttacks.size();
                }
                child1Monitor.done();
                Thread.sleep(250);

                // 攻撃イベント一覧を読み込み
                monitor.subTask(Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents")); //$NON-NLS-1$
                SubMonitor child2Monitor = subMonitor.split(85).setWorkRemaining(allAttacks.size());
                attackProcessCount = 1;
                for (Attack attack : allAttacks) {
                    List<AttackEvent> orgAttackEvents = new ArrayList<AttackEvent>();
                    Api attackEventsApi = new AttackEventsByAttackUuidApi(this.shell, this.ps, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                    List<AttackEvent> tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                    int totalCount = attackEventsApi.getTotalCount();
                    int atkEvtProcessCount = 0;
                    SubMonitor child2_1Monitor = child2Monitor.split(1).setWorkRemaining(totalCount);
                    for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                        if (monitor.isCanceled()) {
                            throw new OperationCanceledException();
                        }
                        tmpAttackEvent.setSource_name(attack.getSource_name());
                        atkEvtProcessCount++;
                        // monitor.subTask(String.format("%s(%d/%d)", attack.getSource_name(), processCount, totalCount)); //$NON-NLS-1$ //$NON-NLS-2$
                        monitor.subTask(String.format("%s(%d/%d), %s(%d/%d)", Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.attack"),
                                attackProcessCount, allAttacks.size(), Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.attackevent"),
                                atkEvtProcessCount, totalCount));
                        child2_1Monitor.worked(1);
                        Thread.sleep(15);
                    }
                    orgAttackEvents.addAll(tmpAttackEvents);
                    boolean incompleteFlg = false;
                    incompleteFlg = totalCount > orgAttackEvents.size();
                    while (incompleteFlg) {
                        attackEventsApi = new AttackEventsByAttackUuidApi(this.shell, this.ps, org, attack.getUuid(), frDetectedDate, toDetectedDate, orgAttackEvents.size());
                        tmpAttackEvents = (List<AttackEvent>) attackEventsApi.post();
                        for (AttackEvent tmpAttackEvent : tmpAttackEvents) {
                            if (monitor.isCanceled()) {
                                throw new OperationCanceledException();
                            }
                            tmpAttackEvent.setSource_name(attack.getSource_name());
                            atkEvtProcessCount++;
                            // monitor.subTask(String.format("%s(%d/%d)", attack.getSource_name(), processCount, totalCount)); //$NON-NLS-1$ //$NON-NLS-2$
                            monitor.subTask(String.format("%s(%d/%d), %s(%d/%d)", Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.attack"),
                                    attackProcessCount, allAttacks.size(), Messages.getString("attackeventsgetwithprogress.progress.loading.attackevents.attackevent"),
                                    atkEvtProcessCount, totalCount));
                            child2_1Monitor.worked(1);
                            Thread.sleep(15);
                        }
                        orgAttackEvents.addAll(tmpAttackEvents);
                        incompleteFlg = totalCount > orgAttackEvents.size();
                        Thread.sleep(100);
                    }
                    this.allAttackEvents.addAll(orgAttackEvents);
                    attackProcessCount++;
                    Thread.sleep(100);
                }
                child2Monitor.done();
                Thread.sleep(100);
            } catch (OperationCanceledException oce) {
                throw new InvocationTargetException(new OperationCanceledException(Messages.getString("attackeventsgetwithprogress.progress.canceled")));
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        subMonitor.done();
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
                tagFilterSet.add(new Filter("")); //$NON-NLS-1$
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
