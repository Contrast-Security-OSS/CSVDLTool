package com.contrastsecurity.csvdltool.json;

import com.contrastsecurity.csvdltool.model.Trace;

public class TraceJson extends ContrastJson {
    private Trace trace;

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return this.trace.toString();
    }

}
