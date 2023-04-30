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

import com.contrastsecurity.csvdltool.model.ServerCSVColumn;
import com.google.gson.Gson;

public enum ServerCSVColmunEnum {
    SERVER_01(Messages.getString("servercsvcolmunenum.column.server.name"), 1, false, null, false, null, null, true, Messages.getString("servercsvcolmunenum.remarks.server.name")), //$NON-NLS-1$ //$NON-NLS-2$
    SERVER_02(Messages.getString("servercsvcolmunenum.column.agent.path"), 2, false, null, false, null, null, true, Messages.getString("servercsvcolmunenum.remarks.agent.path")), //$NON-NLS-1$ //$NON-NLS-2$
    SERVER_03(Messages.getString("servercsvcolmunenum.column.agent.language"), 3, false, null, false, null, null, true, Messages.getString("servercsvcolmunenum.remarks.agent.language")), //$NON-NLS-1$ //$NON-NLS-2$
    SERVER_04(Messages.getString("servercsvcolmunenum.column.agent.version"), 4, false, null, false, null, null, true, Messages.getString("servercsvcolmunenum.remarks.agent.version")); //$NON-NLS-1$ //$NON-NLS-2$

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isBoolean;
    private String trueStr;
    private String falseStr;
    private boolean isDefault;
    private String remarks;

    private ServerCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isBoolean, String trueStr, String falseStr, boolean isDefault,
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

    public static ServerCSVColmunEnum[] defaultValues() {
        List<ServerCSVColmunEnum> list = new ArrayList<ServerCSVColmunEnum>();
        for (ServerCSVColmunEnum e : ServerCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new ServerCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<ServerCSVColumn> list = new ArrayList<ServerCSVColumn>();
        for (ServerCSVColmunEnum e : ServerCSVColmunEnum.sortedValues()) {
            list.add(new ServerCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static ServerCSVColmunEnum getByName(String column) {
        for (ServerCSVColmunEnum value : ServerCSVColmunEnum.values()) {
            if (value.getCulumn().equals(column)) {
                return value;
            }
        }
        return null;
    }

    public static List<ServerCSVColmunEnum> sortedValues() {
        List<ServerCSVColmunEnum> list = Arrays.asList(ServerCSVColmunEnum.values());
        Collections.sort(list, new Comparator<ServerCSVColmunEnum>() {
            @Override
            public int compare(ServerCSVColmunEnum e1, ServerCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
