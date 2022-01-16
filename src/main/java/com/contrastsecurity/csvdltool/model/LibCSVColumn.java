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

package com.contrastsecurity.csvdltool.model;

import com.contrastsecurity.csvdltool.LibCSVColmunEnum;

public class LibCSVColumn {
    private LibCSVColmunEnum column;
    private String separateStr;
    private boolean separate;
    private String trueStr;
    private String falseStr;
    private boolean isBoolean;
    private boolean valid;

    public LibCSVColumn(LibCSVColmunEnum column) {
        this.column = column;
        this.separateStr = column.getSeparate();
        this.separate = column.isSeparate();
        this.trueStr = column.getTrueStr();
        this.falseStr = column.getFalseStr();
        this.isBoolean = column.isBoolean();
        this.valid = column.isDefault();
    }

    public LibCSVColmunEnum getColumn() {
        return column;
    }

    public void setColumn(LibCSVColmunEnum column) {
        this.column = column;
    }

    public String getSeparateStr() {
        return separateStr;
    }

    public void setSeparateStr(String separateStr) {
        this.separateStr = separateStr;
    }

    public boolean isSeparate() {
        return separate;
    }

    public void setSeparate(boolean separate) {
        this.separate = separate;
    }

    public String getTrueStr() {
        return trueStr;
    }

    public void setTrueStr(String trueStr) {
        this.trueStr = trueStr;
    }

    public String getFalseStr() {
        return falseStr;
    }

    public void setFalseStr(String falseStr) {
        this.falseStr = falseStr;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public void setBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LibCSVColumn) {
            LibCSVColumn other = (LibCSVColumn) obj;
            return other.column == this.column;
        }
        return false;
    }
}
