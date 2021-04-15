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
import java.util.List;

public enum VulCSVColmunEnum {
    VUL_01("アプリケーション名", true),
    VUL_02("マージしたときの各アプリ名称", true),
    VUL_03("アプリケーションID", false),
    VUL_04("アプリケーションタグ", true),
    VUL_05("カテゴリ", true),
    VUL_06("ルール", true),
    VUL_07("深刻度", true),
    VUL_08("CWE", true),
    VUL_09("ステータス", true),
    VUL_10("言語", true),
    VUL_11("アプリケーションのグループ", true),
    VUL_12("脆弱性のタイトル", true),
    VUL_13("最初の検出", true),
    VUL_14("最後の検出", true),
    VUL_15("ビルド番号", true),
    VUL_16("次のサーバにより報告", true),
    VUL_17("モジュール", true),
    VUL_18("脆弱性タグ", true);

    private String culumn;
    private boolean isDefault;

    private VulCSVColmunEnum(String culumn, boolean isDefault) {
        this.culumn = culumn;
        this.isDefault = isDefault;
    }

    public String getCulumn() {
        return culumn;
    }

    public static VulCSVColmunEnum[] defaultValues() {
        List<VulCSVColmunEnum> list = new ArrayList<VulCSVColmunEnum>();
        for (VulCSVColmunEnum e : VulCSVColmunEnum.values()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new VulCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<String> list = new ArrayList<String>();
        for (VulCSVColmunEnum e : VulCSVColmunEnum.values()) {
            if (e.isDefault) {
                list.add(e.name());
            }
        }
        return String.join(",", list);
    }

    public static VulCSVColmunEnum getByName(String column) {
        for (VulCSVColmunEnum value : VulCSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

}
