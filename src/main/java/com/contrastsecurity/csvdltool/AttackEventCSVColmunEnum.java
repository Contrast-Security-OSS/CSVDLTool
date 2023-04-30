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
    ATTACK_EVENT_01(Messages.getString("attackeventcsvcolmunenum.column.source.name"), 1, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.source.name")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_02(Messages.getString("attackeventcsvcolmunenum.column.source.ip"), 2, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.source.ip")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_03(Messages.getString("attackeventcsvcolmunenum.column.result"), 3, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.result")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_04(Messages.getString("attackeventcsvcolmunenum.column.application.name"), 4, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.application.name")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_05(Messages.getString("attackeventcsvcolmunenum.column.server.name"), 5, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.server.name")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_06(Messages.getString("attackeventcsvcolmunenum.column.rule.title"), 6, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.rule.title")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_07(Messages.getString("attackeventcsvcolmunenum.column.attack.detected"), 7, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.attack.detected")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_08(Messages.getString("attackeventcsvcolmunenum.column.attack.url"), 8, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.attack.url")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_09(Messages.getString("attackeventcsvcolmunenum.column.attack.value"), 9, false, null, true, Messages.getString("attackeventcsvcolmunenum.remarks.attack.value")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_10(Messages.getString("attackeventcsvcolmunenum.column.attack.tag"), 10, true, ",", true, Messages.getString("attackeventcsvcolmunenum.remarks.attack.tag")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ATTACK_EVENT_11(Messages.getString("attackeventcsvcolmunenum.column.organization.name"), 11, false, null, false, Messages.getString("attackeventcsvcolmunenum.remarks.organization.name")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_12(Messages.getString("attackeventcsvcolmunenum.column.organization.id"), 12, false, null, false, Messages.getString("attackeventcsvcolmunenum.remarks.organization.id")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_13(Messages.getString("attackeventcsvcolmunenum.column.attack.link"), 13, false, null, false, Messages.getString("attackeventcsvcolmunenum.remarks.attack.link")), //$NON-NLS-1$ //$NON-NLS-2$
    ATTACK_EVENT_14(Messages.getString("attackeventcsvcolmunenum.column.attack.hyperlink"), 14, false, null, false, Messages.getString("attackeventcsvcolmunenum.remarks.attack.hyperlink")); //$NON-NLS-1$ //$NON-NLS-2$

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
