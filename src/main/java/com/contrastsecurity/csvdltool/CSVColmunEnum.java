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

public enum CSVColmunEnum {
    COL_01("アプリケーション名", true),
    COL_02("マージしたときの各アプリ名称", true),
    COL_03("アプリケーションID", false),
    COL_04("アプリケーションタグ", true),
    COL_05("カテゴリ", true),
    COL_06("ルール", true),
    COL_07("深刻度", true),
    COL_08("CWE", true),
    COL_09("ステータス", true),
    COL_10("言語", true),
    COL_11("アプリケーションのグループ", true),
    COL_12("脆弱性のタイトル", true),
    COL_13("最初の検出", true),
    COL_14("最後の検出", true),
    COL_15("ビルド番号", true),
    COL_16("次のサーバにより報告", true),
    COL_17("モジュール", true),
    COL_18("脆弱性タグ", true);

    private String culumn;
    private boolean isDefault;

    private CSVColmunEnum(String culumn, boolean isDefault) {
        this.culumn = culumn;
        this.isDefault = isDefault;
    }

    public String getCulumn() {
        return culumn;
    }

    public static CSVColmunEnum[] defaultValues() {
        List<CSVColmunEnum> list = new ArrayList<CSVColmunEnum>();
        for (CSVColmunEnum e : CSVColmunEnum.values()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new CSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<String> list = new ArrayList<String>();
        for (CSVColmunEnum e : CSVColmunEnum.values()) {
            if (e.isDefault) {
                list.add(e.name());
            }
        }
        return String.join(",", list);
    }

    public static CSVColmunEnum getByName(String column) {
        for (CSVColmunEnum value : CSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

}
