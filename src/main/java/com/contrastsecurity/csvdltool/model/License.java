package com.contrastsecurity.csvdltool.model;

public class License {
    private String level;
    private String start;
    private String end;
    private String near_expiration;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getNear_expiration() {
        return near_expiration;
    }

    public void setNear_expiration(String near_expiration) {
        this.near_expiration = near_expiration;
    }

}
