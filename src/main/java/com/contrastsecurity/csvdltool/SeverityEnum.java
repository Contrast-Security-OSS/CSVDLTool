package com.contrastsecurity.csvdltool;

public enum SeverityEnum {
    CRITICAL("重大"), HIGH("高"), MEDIUM("中"), LOW("低"), NOTE("注意");

    private String label;

    private SeverityEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
