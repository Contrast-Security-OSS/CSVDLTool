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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.contrastsecurity.csvdltool.model.RouteCoverageCSVColumn;
import com.google.gson.Gson;

public enum RouteCoverageCSVColmunEnum {
    RC_01(Messages.getString("routecoveragecsvcolmunenum.column.method_signature"), 1, false, null, false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.method_signature")), //$NON-NLS-1$ //$NON-NLS-2$
    RC_02(Messages.getString("routecoveragecsvcolmunenum.column.routeurl"), 2, true, ",", false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.routeurl")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    RC_03(Messages.getString("routecoveragecsvcolmunenum.column.environment"), 3, true, ",", false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.environment")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    RC_04(Messages.getString("routecoveragecsvcolmunenum.column.server"), 4, true, ",", false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.server")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    RC_05(Messages.getString("routecoveragecsvcolmunenum.column.vulnerability"), 5, false, null, false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.vulnerability")), //$NON-NLS-1$ //$NON-NLS-2$
    RC_06(Messages.getString("routecoveragecsvcolmunenum.column.application"), 6, false, null, false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.application")), //$NON-NLS-1$ //$NON-NLS-2$
    RC_07(Messages.getString("routecoveragecsvcolmunenum.column.detected"), 7, false, null, false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.detected")), //$NON-NLS-1$ //$NON-NLS-2$
    RC_08(Messages.getString("routecoveragecsvcolmunenum.column.status"), 8, false, null, false, null, null, true, Messages.getString("routecoveragecsvcolmunenum.remarks.status")); //$NON-NLS-1$ //$NON-NLS-2$

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isBoolean;
    private String trueStr;
    private String falseStr;
    private boolean isDefault;
    private String remarks;

    private RouteCoverageCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isBoolean, String trueStr, String falseStr, boolean isDefault,
            String remarks) {
        this.culumn = culumn;
        this.order = order;
        this.isSeparate = isSeparate;
        this.separate = separate;
        this.isBoolean = isBoolean;
        this.trueStr = trueStr;
        this.falseStr = falseStr;
        this.isDefault = isDefault;
        this.remarks = remarks;
    }

    public String getCulumn() {
        return culumn;
    }

    public boolean isSeparate() {
        return isSeparate;
    }

    public String getSeparate() {
        return separate;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public String getTrueStr() {
        return trueStr;
    }

    public String getFalseStr() {
        return falseStr;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getRemarks() {
        return remarks;
    }

    public static RouteCoverageCSVColmunEnum[] defaultValues() {
        List<RouteCoverageCSVColmunEnum> list = new ArrayList<RouteCoverageCSVColmunEnum>();
        for (RouteCoverageCSVColmunEnum e : RouteCoverageCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new RouteCoverageCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<RouteCoverageCSVColumn> list = new ArrayList<RouteCoverageCSVColumn>();
        for (RouteCoverageCSVColmunEnum e : RouteCoverageCSVColmunEnum.sortedValues()) {
            list.add(new RouteCoverageCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static RouteCoverageCSVColmunEnum getByName(String column) {
        for (RouteCoverageCSVColmunEnum value : RouteCoverageCSVColmunEnum.values()) {
            if (value.getCulumn().equals(column)) {
                return value;
            }
        }
        return null;
    }

    public static List<RouteCoverageCSVColmunEnum> sortedValues() {
        List<RouteCoverageCSVColmunEnum> list = Arrays.asList(RouteCoverageCSVColmunEnum.values());
        Collections.sort(list, new Comparator<RouteCoverageCSVColmunEnum>() {
            @Override
            public int compare(RouteCoverageCSVColmunEnum e1, RouteCoverageCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
