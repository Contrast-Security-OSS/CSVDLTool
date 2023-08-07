package com.contrastsecurity.csvdltool.model.serverless;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String functionName;
    private String functionArn;
    private List<Result> results;

    public Function() {
        this.results = new ArrayList<Result>();
    }

    public Function(String functionArn) {
        this.functionArn = functionArn;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionArn() {
        return functionArn;
    }

    public void setFunctionArn(String functionArn) {
        this.functionArn = functionArn;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("functionArn : " + this.functionArn); //$NON-NLS-1$
        return String.join(", ", strList); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Function) {
            Function other = (Function) obj;
            return other.functionArn.equals(this.functionArn);
        }
        return false;
    }

}
