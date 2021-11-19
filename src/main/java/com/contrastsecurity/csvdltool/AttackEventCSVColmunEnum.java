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

import com.contrastsecurity.csvdltool.model.AttackEventCSVColumn;
import com.google.gson.Gson;

public enum AttackEventCSVColmunEnum {
    ATTACK_EVENT_01("ソースIP", 1, false, null, true, ""),
    ATTACK_EVENT_02("結果", 2, false, null, true, ""),
    ATTACK_EVENT_03("アプリケーション", 3, false, null, true, ""),
    ATTACK_EVENT_04("サーバ", 4, false, null, true, ""),
    ATTACK_EVENT_05("ルール", 5, false, null, true, ""),
    ATTACK_EVENT_06("時間", 6, false, null, true, ""),
    ATTACK_EVENT_07("URL", 7, false, null, true, ""),
    ATTACK_EVENT_08("タグ", 8, true, ",", true, ""),
    ATTACK_EVENT_09("組織名", 9, false, null, false, ""),
    ATTACK_EVENT_10("組織ID", 10, false, null, false, ""),
    ATTACK_EVENT_11("リンク", 11, false, null, false, "TeamServerへのリンクです。"),
    ATTACK_EVENT_12("リンク(ハイパーリンク)", 12, false, null, false, "TeamServerへのリンク（ハイパーリンク）です。");

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isDefault;
    private String remarks;

    private AttackEventCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isDefault, String remarks) {
        this.culumn = culumn;
        this.order = order;
        this.isSeparate = isSeparate;
        this.separate = separate;
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

    public boolean isDefault() {
        return isDefault;
    }

    public String getRemarks() {
        return remarks;
    }

    public static AttackEventCSVColmunEnum[] defaultValues() {
        List<AttackEventCSVColmunEnum> list = new ArrayList<AttackEventCSVColmunEnum>();
        for (AttackEventCSVColmunEnum e : AttackEventCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new AttackEventCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<AttackEventCSVColumn> list = new ArrayList<AttackEventCSVColumn>();
        for (AttackEventCSVColmunEnum e : AttackEventCSVColmunEnum.sortedValues()) {
            list.add(new AttackEventCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static AttackEventCSVColmunEnum getByName(String column) {
        for (AttackEventCSVColmunEnum value : AttackEventCSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

    public static List<AttackEventCSVColmunEnum> sortedValues() {
        List<AttackEventCSVColmunEnum> list = Arrays.asList(AttackEventCSVColmunEnum.values());
        Collections.sort(list, new Comparator<AttackEventCSVColmunEnum>() {
            @Override
            public int compare(AttackEventCSVColmunEnum e1, AttackEventCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
