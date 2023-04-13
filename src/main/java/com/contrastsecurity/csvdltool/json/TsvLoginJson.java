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

package com.contrastsecurity.csvdltool.json;

public class TsvLoginJson extends ContrastJson {

    private boolean tsv_login;
    private boolean tsv_setup;
    private boolean tsv_setup_optional;
    private boolean tsv_trusted_device;

    public boolean isTsv_login() {
        return tsv_login;
    }

    public void setTsv_login(boolean tsv_login) {
        this.tsv_login = tsv_login;
    }

    public boolean isTsv_setup() {
        return tsv_setup;
    }

    public void setTsv_setup(boolean tsv_setup) {
        this.tsv_setup = tsv_setup;
    }

    public boolean isTsv_setup_optional() {
        return tsv_setup_optional;
    }

    public void setTsv_setup_optional(boolean tsv_setup_optional) {
        this.tsv_setup_optional = tsv_setup_optional;
    }

    public boolean isTsv_trusted_device() {
        return tsv_trusted_device;
    }

    public void setTsv_trusted_device(boolean tsv_trusted_device) {
        this.tsv_trusted_device = tsv_trusted_device;
    }

}
