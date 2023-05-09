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

public enum StatusEnum {
    REPORTED(Messages.getString("statusenum.reported")), //$NON-NLS-1$
    SUSPICIOUS(Messages.getString("statusenum.suspicious")), //$NON-NLS-1$
    CONFIRMED(Messages.getString("statusenum.confirmed")), //$NON-NLS-1$
    NOTAPROBLEM(Messages.getString("statusenum.not-a-problem")), //$NON-NLS-1$
    REMEDIATED(Messages.getString("statusenum.remediated")), //$NON-NLS-1$
    REMEDIATED_AUTO_VERIFIED(Messages.getString("statusenum.remediated-auto-verified")), //$NON-NLS-1$
    FIXED(Messages.getString("statusenum.fixed")); //$NON-NLS-1$

    private String label;

    private StatusEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
