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

import java.util.regex.Pattern;

public class GlobalPropertiesJson extends ContrastJson {

    public enum VersionDiff {
        LESS_THAN,
        GREATER_EQUAL
    }

    private String version;
    private String internal_version;
    private String mode;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInternal_version() {
        return internal_version;
    }

    public void setInternal_version(String internal_version) {
        this.internal_version = internal_version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public static int getInternalVersionNumber(String v) {
        String[] versions = v.split(Pattern.quote(".")); //$NON-NLS-1$
        String v1 = versions[0];
        String v2 = versions[1].length() > 1 ? versions[1] : String.format("0%s", versions[1]); //$NON-NLS-1$
        String v3 = versions[2].length() > 1 ? versions[2] : String.format("0%s", versions[2]); //$NON-NLS-1$
        return Integer.parseInt(String.format("%s%s%s", v1, v2, v3)); //$NON-NLS-1$
    }

    public VersionDiff compareVersion(String chkVersion) {
        int currentVer = getInternalVersionNumber(this.internal_version);
        int checkVer = getInternalVersionNumber(chkVersion);
        if (currentVer >= checkVer) {
            return VersionDiff.GREATER_EQUAL;
        } else {
            return VersionDiff.LESS_THAN;
        }
    }

    @Override
    public String toString() {
        return this.version;
    }

}
