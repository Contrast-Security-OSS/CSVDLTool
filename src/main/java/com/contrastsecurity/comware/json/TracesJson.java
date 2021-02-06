package com.contrastsecurity.comware.json;

import java.util.List;
import java.util.StringJoiner;

public class TracesJson extends ContrastJson {
    private List<String> traces;

    public List<String> getTraces() {
        return traces;
    }

    public void setTraces(List<String> traces) {
        this.traces = traces;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        for (String s : this.traces) {
            sj.add(s);
        }
        return sj.toString();
    }

}
