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
    LIB_01("ライブラリ名", 1, false, null, false, null, null, true, ""),
    LIB_02("言語", 2, false, null, false, null, null, true, ""),
    LIB_03("現在バージョン", 3, false, null, false, null, null, true, ""),
    LIB_04("現在バージョンリリース日", 4, false, null, false, null, null, true, ""),
    LIB_05("最新バージョン", 5, false, null, false, null, null, true, ""),
    LIB_06("最新バージョンリリース日", 6, false, null, false, null, null, true, ""),
    LIB_07("スコア", 7, false, null, false, null, null, true, ""),
    LIB_08("使用クラス数", 8, false, null, false, null, null, true, ""),
    LIB_09("全体クラス数", 9, false, null, false, null, null, true, ""),
    LIB_10("ライセンス", 10, true, ",", false, null, null, true, ""),
    LIB_11("関連アプリケーション", 11, true, ",", false, null, null, true, ""),
    LIB_12("関連サーバ", 12, true, ",", false, null, null, true, ""),
    LIB_13("CVE", 14, true, ",", false, null, null, true, ""),
    LIB_14("ライブラリタグ", 13, true, ",", false, null, null, true, ""),
    LIB_15("組織名", 18, false, null, false, null, null, false, ""),
    LIB_16("組織ID", 19, false, null, false, null, null, false, ""),
    LIB_17("リンク", 20, false, null, false, null, null, false, "TeamServerへのリンクです。"),
    LIB_18("リンク(ハイパーリンク)", 21, false, null, false, null, null, false, "TeamServerへのリンク（ハイパーリンク）です。"),
    LIB_19("ライブラリ制限に抵触", 15, false, null, true, "Yes", "No", false, "ライブラリ・コンプライアンス"),
    LIB_20("バージョン要件に抵触", 16, false, null, true, "Yes", "No", false, "ライブラリ・コンプライアンス"),
    LIB_21("ライセンス制限に抵触", 17, false, null, true, "Yes", "No", false, "ライブラリ・コンプライアンス");

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
