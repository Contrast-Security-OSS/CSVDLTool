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
    VUL_01("アプリケーション名", 1, false, null, true, ""),
    VUL_02("マージしたときの各アプリ名称", 2, false, null, true, ""),
    VUL_03("アプリケーションID", 3, false, null, false, ""),
    VUL_04("アプリケーションタグ", 4, true, ",", true, ""),
    VUL_05("カテゴリ", 5, false, null, true, ""),
    VUL_06("ルール", 6, false, null, true, ""),
    VUL_07("深刻度", 7, false, null, true, ""),
    VUL_08("CWE", 8, true, ",", true, ""),
    VUL_09("ステータス", 9, false, null, true, ""),
    VUL_10("言語", 11, false, null, true, ""),
    VUL_11("アプリケーションのグループ", 12, true, ",", true, "Admin権限のユーザーが設定されている場合、取得可能です。"),
    VUL_12("脆弱性のタイトル", 13, false, null, true, ""),
    VUL_13("最初の検出", 14, false, null, true, ""),
    VUL_14("最後の検出", 15, false, null, true, ""),
    VUL_15("ビルド番号", 16, true, ",", true, ""),
    VUL_16("次のサーバにより報告", 17, true, ",", true, ""),
    VUL_17("モジュール", 18, false, null, true, ""),
    VUL_18("脆弱性タグ", 19, true, ",", true, ""),
    VUL_19("保留中ステータス", 10, false, null, false, ""),
    VUL_20("組織名", 20, false, null, false, ""),
    VUL_21("組織ID", 21, false, null, false, ""),
    VUL_22("リンク", 22, false, null, false, "TeamServerへのリンクです。"),
    VUL_23("リンク(ハイパーリンク)", 23, false, null, false, "TeamServerへのリンク（ハイパーリンク）です。");

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isDefault;
    private String remarks;

    private VulCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isDefault, String remarks) {
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
