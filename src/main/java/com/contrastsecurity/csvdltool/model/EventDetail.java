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

import java.util.ArrayList;
import java.util.List;

import com.contrastsecurity.csvdltool.Messages;
import com.google.gson.annotations.SerializedName;

public class EventDetail {
    @SerializedName("class")
    private String classStr;
    private String method;
    private String object;
    @SerializedName("return")
    private String returnStr;
    private List<Parameter> parameters;
    private List<StackTrace> stacktraces;

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getReturnStr() {
        return returnStr;
    }

    public void setReturnStr(String returnStr) {
        this.returnStr = returnStr;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<StackTrace> getStacktraces() {
        return stacktraces;
    }

    public void setStacktraces(List<StackTrace> stacktraces) {
        this.stacktraces = stacktraces;
    }

    public List<String> getDetailLines() {
        List<String> detailLines = new ArrayList<String>();
        detailLines.add(String.format("%s %s.%s", Messages.getString("eventdetail.detail.class.method.text"), this.classStr, this.method)); //$NON-NLS-1$ //$NON-NLS-2$
        detailLines.add(String.format("%s %s", Messages.getString("eventdetail.detail.object.text"), this.object)); //$NON-NLS-1$ //$NON-NLS-2$
        detailLines.add(String.format("%s %s", Messages.getString("eventdetail.detail.return.text"), this.returnStr)); //$NON-NLS-1$ //$NON-NLS-2$
        List<String> paramList = new ArrayList<String>();
        if (this.getParameters() != null) {
            for (Parameter param : this.getParameters()) {
                paramList.add(param.getParameter());
            }
        }
        detailLines.add(String.format("%s %s", Messages.getString("eventdetail.detail.parameter.text"), String.join(", ", paramList))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        detailLines.add(Messages.getString("eventdetail.detail.stacktrace.text")); //$NON-NLS-1$
        if (this.getStacktraces() != null) {
            for (StackTrace stackTrace : this.getStacktraces()) {
                detailLines.add(stackTrace.getDescription());
            }
        }
        return detailLines;
    }

}
