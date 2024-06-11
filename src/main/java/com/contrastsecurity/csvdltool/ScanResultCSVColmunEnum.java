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

import com.contrastsecurity.csvdltool.model.ScanResultCSVColumn;
import com.google.gson.Gson;

public enum ScanResultCSVColmunEnum {
    SCAN_RESULT_01("脆弱性", 1, false, null, false, null, null, true, ""),
    SCAN_RESULT_02("深刻度", 2, false, null, false, null, null, true, ""),
    SCAN_RESULT_03("言語", 3, false, null, false, null, null, true, ""),
    SCAN_RESULT_04("最後の検出", 4, false, null, false, null, null, true, ""),
    SCAN_RESULT_05("ステータス", 5, false, null, false, null, null, true, "");

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isBoolean;
    private String trueStr;
    private String falseStr;
    private boolean isDefault;
    private String remarks;

    private ScanResultCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isBoolean, String trueStr, String falseStr, boolean isDefault,
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

    public static ScanResultCSVColmunEnum[] defaultValues() {
        List<ScanResultCSVColmunEnum> list = new ArrayList<ScanResultCSVColmunEnum>();
        for (ScanResultCSVColmunEnum e : ScanResultCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new ScanResultCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<ScanResultCSVColumn> list = new ArrayList<ScanResultCSVColumn>();
        for (ScanResultCSVColmunEnum e : ScanResultCSVColmunEnum.sortedValues()) {
            list.add(new ScanResultCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static ScanResultCSVColmunEnum getByName(String column) {
        for (ScanResultCSVColmunEnum value : ScanResultCSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

    public static List<ScanResultCSVColmunEnum> sortedValues() {
        List<ScanResultCSVColmunEnum> list = Arrays.asList(ScanResultCSVColmunEnum.values());
        Collections.sort(list, new Comparator<ScanResultCSVColmunEnum>() {
            @Override
            public int compare(ScanResultCSVColmunEnum e1, ScanResultCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
