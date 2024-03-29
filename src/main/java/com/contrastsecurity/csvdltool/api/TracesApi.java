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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.csvdltool.FilterEnum;
import com.contrastsecurity.csvdltool.json.TracesJson;
import com.contrastsecurity.csvdltool.model.Filter;
import com.contrastsecurity.csvdltool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TracesApi extends Api {

    private String appId;
    private Map<FilterEnum, Set<Filter>> filterMap;
    private Date frLastDetectedDate;
    private Date toLastDetectedDate;

    public TracesApi(Shell shell, IPreferenceStore ps, Organization org, String appId, Map<FilterEnum, Set<Filter>> filterMap, Date frDate, Date toDate) {
        super(shell, ps, org);
        this.appId = appId;
        this.filterMap = filterMap;
        this.frLastDetectedDate = frDate;
        this.toLastDetectedDate = toDate;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        boolean inValidFoundFlg = false;
        // 重大度のクエリ文字列
        List<String> severityFilters = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.SEVERITY)) {
            if (filter.isValid()) {
                severityFilters.add(filter.getKeycode());
            } else {
                inValidFoundFlg |= true;
            }
        }
        String severityFilterQuery = ""; //$NON-NLS-1$
        if (inValidFoundFlg && !severityFilters.isEmpty()) {
            severityFilterQuery = String.format("severities=%s", String.join(",", severityFilters)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // 脆弱性タイプのクエリ文字列
        List<String> vulnTypeFilters = new ArrayList<String>();
        inValidFoundFlg = false;
        for (Filter filter : filterMap.get(FilterEnum.VULNTYPE)) {
            if (filter.isValid()) {
                vulnTypeFilters.add(filter.getKeycode());
            } else {
                inValidFoundFlg |= true;
            }
        }
        String vulnTypeFilterQuery = ""; //$NON-NLS-1$
        if (inValidFoundFlg && !vulnTypeFilters.isEmpty()) {
            if (severityFilterQuery.isEmpty()) {
                vulnTypeFilterQuery = String.format("vulnTypes=%s", String.join(",", vulnTypeFilters)); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                vulnTypeFilterQuery = String.format("&vulnTypes=%s", String.join(",", vulnTypeFilters)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        // 最終検出日のクエリ文字列
        String lastDetectedFilterQuery = ""; //$NON-NLS-1$
        if (frLastDetectedDate != null || toLastDetectedDate != null) {
            String startDate = ""; //$NON-NLS-1$
            if (frLastDetectedDate != null) {
                startDate = String.format("&startDate=%s", frLastDetectedDate.getTime()); //$NON-NLS-1$
            }
            String endDate = ""; //$NON-NLS-1$
            if (toLastDetectedDate != null) {
                endDate = String.format("&endDate=%s", toLastDetectedDate.getTime()); //$NON-NLS-1$
            }
            if (severityFilterQuery.isEmpty() && vulnTypeFilterQuery.isEmpty()) {
                lastDetectedFilterQuery = String.format("timestampFilter=LAST%s%s", startDate, endDate); //$NON-NLS-1$
            } else {
                lastDetectedFilterQuery = String.format("&timestampFilter=LAST%s%s", startDate, endDate); //$NON-NLS-1$
            }
        }

        if (severityFilterQuery.isEmpty() && vulnTypeFilterQuery.isEmpty() && lastDetectedFilterQuery.isEmpty()) {
            return String.format("%s/api/ng/%s/traces/%s/ids", this.contrastUrl, orgId, this.appId); //$NON-NLS-1$
        } else {
            return String.format("%s/api/ng/%s/traces/%s/ids?%s%s%s", this.contrastUrl, orgId, this.appId, severityFilterQuery, vulnTypeFilterQuery, lastDetectedFilterQuery); //$NON-NLS-1$
        }
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type tracesType = new TypeToken<TracesJson>() {
        }.getType();
        TracesJson tracesJson = gson.fromJson(response, tracesType);
        return tracesJson.getTraces();
    }

}
