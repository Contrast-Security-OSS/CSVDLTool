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

public enum LibCSVColmunEnum {
    LIB_01("ライブラリ名", 1, true),
    LIB_02("言語", 2, true),
    LIB_03("現在バージョン", 3, true),
    LIB_04("現在バージョンリリース日", 4, true),
    LIB_05("最新バージョン", 5, true),
    LIB_06("最新バージョンリリース日", 6, true),
    LIB_07("スコア", 7, true),
    LIB_08("使用クラス数", 8, true),
    LIB_09("全体クラス数", 9, true),
    LIB_10("ライセンス", 10, true),
    LIB_11("関連アプリケーション", 11, true),
    LIB_12("関連サーバ", 12, true),
    LIB_13("CVE", 13, true);

    private String culumn;
    private int order;
    private boolean isDefault;

    private LibCSVColmunEnum(String culumn, int order, boolean isDefault) {
        this.culumn = culumn;
        this.order = order;
        this.isDefault = isDefault;
    }

    public String getCulumn() {
        return culumn;
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
        List<String> list = new ArrayList<String>();
        for (LibCSVColmunEnum e : LibCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e.name());
            }
        }
        return String.join(",", list);
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
