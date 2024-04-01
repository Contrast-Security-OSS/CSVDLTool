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

import com.contrastsecurity.csvdltool.model.LibCSVColumn;
import com.google.gson.Gson;

public enum LibCSVColmunEnum {
    LIB_01(Messages.getString("libcsvcolmunenum.column.name"), 1, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.name")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_02(Messages.getString("libcsvcolmunenum.column.language"), 2, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.language")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_03(Messages.getString("libcsvcolmunenum.column.current.version"), 3, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.current.version")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_04(Messages.getString("libcsvcolmunenum.column.current.release.date"), 4, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.current.release.date")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_05(Messages.getString("libcsvcolmunenum.column.latest.version"), 5, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.latest.version")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_06(Messages.getString("libcsvcolmunenum.column.latest.release.date"), 6, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.latest.release.date")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_07(Messages.getString("libcsvcolmunenum.column.score"), 7, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.score")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_08(Messages.getString("libcsvcolmunenum.column.used.class.count"), 8, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.used.class.count")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_09(Messages.getString("libcsvcolmunenum.column.total.class.count"), 9, false, null, false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.total.class.count")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_10(Messages.getString("libcsvcolmunenum.column.license"), 10, true, ",", false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.license")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LIB_11(Messages.getString("libcsvcolmunenum.column.related.application"), 11, true, ",", false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.related.application")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LIB_12(Messages.getString("libcsvcolmunenum.column.related.server"), 12, true, ",", false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.related.server")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LIB_13(Messages.getString("libcsvcolmunenum.column.cve"), 15, true, ",", false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.cve")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LIB_14(Messages.getString("libcsvcolmunenum.column.library.tag"), 13, true, ",", false, null, null, true, Messages.getString("libcsvcolmunenum.remarks.library.tag")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    LIB_15(Messages.getString("libcsvcolmunenum.column.organization.name"), 19, false, null, false, null, null, false, Messages.getString("libcsvcolmunenum.remarks.organization.name")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_16(Messages.getString("libcsvcolmunenum.column.organization.id"), 20, false, null, false, null, null, false, Messages.getString("libcsvcolmunenum.remarks.organization.id")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_17(Messages.getString("libcsvcolmunenum.column.link"), 21, false, null, false, null, null, false, Messages.getString("libcsvcolmunenum.remarks.link")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_18(Messages.getString("libcsvcolmunenum.column.hyperlink"), 22, false, null, false, null, null, false, Messages.getString("libcsvcolmunenum.remarks.hyperlink")), //$NON-NLS-1$ //$NON-NLS-2$
    LIB_19(Messages.getString("libcsvcolmunenum.column.constraint.library"), 16, false, null, true, Messages.getString("libcsvcolmunenum.truestr.constraint.library"), Messages.getString("libcsvcolmunenum.falsestr.constraint.library"), false, Messages.getString("libcsvcolmunenum.remarks.constraint.library")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    LIB_20(Messages.getString("libcsvcolmunenum.column.constraint.version"), 17, false, null, true, Messages.getString("libcsvcolmunenum.truestr.constraint.version"), Messages.getString("libcsvcolmunenum.falsestr.constraint.version"), false, Messages.getString("libcsvcolmunenum.remarks.constraint.version")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    LIB_21(Messages.getString("libcsvcolmunenum.column.constraint.license"), 18, false, null, true, Messages.getString("libcsvcolmunenum.truestr.constraint.license"), Messages.getString("libcsvcolmunenum.falsestr.constraint.license"), false, Messages.getString("libcsvcolmunenum.remarks.constraint.license")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    LIB_22(Messages.getString("libcsvcolmunenum.column.kev"), 14, false, null, true, Messages.getString("libcsvcolmunenum.truestr.kev"), Messages.getString("libcsvcolmunenum.falsestr.kev"), true, Messages.getString("libcsvcolmunenum.remarks.kev")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isBoolean;
    private String trueStr;
    private String falseStr;
    private boolean isDefault;
    private String remarks;

    private LibCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isBoolean, String trueStr, String falseStr, boolean isDefault, String remarks) {
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

    public static LibCSVColmunEnum[] defaultValues() {
        List<LibCSVColmunEnum> list = new ArrayList<LibCSVColmunEnum>();
        for (LibCSVColmunEnum e : LibCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new LibCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<LibCSVColumn> list = new ArrayList<LibCSVColumn>();
        for (LibCSVColmunEnum e : LibCSVColmunEnum.sortedValues()) {
            list.add(new LibCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static LibCSVColmunEnum getByName(String column) {
        for (LibCSVColmunEnum value : LibCSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

    public static List<LibCSVColmunEnum> sortedValues() {
        List<LibCSVColmunEnum> list = Arrays.asList(LibCSVColmunEnum.values());
        Collections.sort(list, new Comparator<LibCSVColmunEnum>() {
            @Override
            public int compare(LibCSVColmunEnum e1, LibCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
