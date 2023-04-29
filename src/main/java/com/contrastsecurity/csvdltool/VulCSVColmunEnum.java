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

import com.contrastsecurity.csvdltool.model.VulCSVColumn;
import com.google.gson.Gson;

public enum VulCSVColmunEnum {
    VUL_01(Messages.getString("vulcsvcolmunenum.column.application.name"), 1, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.application.name")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_02(Messages.getString("vulcsvcolmunenum.column.merged.application.name"), 2, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.merged.application.name")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_03(Messages.getString("vulcsvcolmunenum.column.application.id"), 3, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.application.id")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_04(Messages.getString("vulcsvcolmunenum.column.application.tag"), 4, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.application.tag")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_05(Messages.getString("vulcsvcolmunenum.column.category"), 5, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.category")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_06(Messages.getString("vulcsvcolmunenum.column.rule"), 6, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.rule")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_07(Messages.getString("vulcsvcolmunenum.column.vuln.severity"), 7, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.vuln.severity")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_08(Messages.getString("vulcsvcolmunenum.column.cwe"), 8, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.cwe")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_09(Messages.getString("vulcsvcolmunenum.column.status"), 9, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.status")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_10(Messages.getString("vulcsvcolmunenum.column.language"), 11, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.language")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_11(Messages.getString("vulcsvcolmunenum.column.group"), 12, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.group")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_12(Messages.getString("vulcsvcolmunenum.column.vuln.title"), 13, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.vuln.title")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_13(Messages.getString("vulcsvcolmunenum.column.vuln.first.detected"), 14, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.vuln.first.detected")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_14(Messages.getString("vulcsvcolmunenum.column.vuln.last.detected"), 15, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.vuln.last.detected")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_15(Messages.getString("vulcsvcolmunenum.column.buildno"), 17, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.buildno")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_16(Messages.getString("vulcsvcolmunenum.column.reported.server"), 18, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.reported.server")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_17(Messages.getString("vulcsvcolmunenum.column.module"), 19, false, null, false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.module")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_18(Messages.getString("vulcsvcolmunenum.column.vuln.tag"), 20, true, ",", false, null, null, true, Messages.getString("vulcsvcolmunenum.remarks.vuln.tag")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_19(Messages.getString("vulcsvcolmunenum.column.vuln.pending.status"), 10, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.pending.status")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_20(Messages.getString("vulcsvcolmunenum.column.organization.name"), 23, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.organization.name")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_21(Messages.getString("vulcsvcolmunenum.column.organization.id"), 24, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.organization.id")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_22(Messages.getString("vulcsvcolmunenum.column.vuln.link"), 25, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.link")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_23(Messages.getString("vulcsvcolmunenum.column.vuln.hyperlink"), 26, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.hyperlink")), //$NON-NLS-1$ //$NON-NLS-2$
    VUL_24(Messages.getString("vulcsvcolmunenum.column.vuln.route.url"), 27, true, ",", false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.route.url")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_25(Messages.getString("vulcsvcolmunenum.column.vuln.session.metadata"), 21, true, ",", false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.session.metadata")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_26(Messages.getString("vulcsvcolmunenum.column.vuln.compliance.policy"), 22, true, ",", false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.compliance.policy")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VUL_27(Messages.getString("vulcsvcolmunenum.column.vuln.detected"), 16, false, null, false, null, null, false, Messages.getString("vulcsvcolmunenum.remarks.vuln.detected")); //$NON-NLS-1$ //$NON-NLS-2$

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isBoolean;
    private String trueStr;
    private String falseStr;
    private boolean isDefault;
    private String remarks;

    private VulCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isBoolean, String trueStr, String falseStr, boolean isDefault, String remarks) {
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

    public static VulCSVColmunEnum[] defaultValues() {
        List<VulCSVColmunEnum> list = new ArrayList<VulCSVColmunEnum>();
        for (VulCSVColmunEnum e : VulCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new VulCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<VulCSVColumn> list = new ArrayList<VulCSVColumn>();
        for (VulCSVColmunEnum e : VulCSVColmunEnum.sortedValues()) {
            list.add(new VulCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static VulCSVColmunEnum getByName(String column) {
        for (VulCSVColmunEnum value : VulCSVColmunEnum.values()) {
            if (value.getCulumn().equals(column)) {
                return value;
            }
        }
        return null;
    }

    public static List<VulCSVColmunEnum> sortedValues() {
        List<VulCSVColmunEnum> list = Arrays.asList(VulCSVColmunEnum.values());
        Collections.sort(list, new Comparator<VulCSVColmunEnum>() {
            @Override
            public int compare(VulCSVColmunEnum e1, VulCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
