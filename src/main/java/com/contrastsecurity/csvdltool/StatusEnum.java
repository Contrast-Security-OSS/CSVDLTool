package com.contrastsecurity.csvdltool;

public enum StatusEnum {
    REPORTED("報告済"), SUSPICIOUS("疑わしい"), CONFIRMED("確認済"), NOTAPROBLEM("問題無し"), REMEDIATED("修復済"), FIXED("修正完了");

    private String label;

    private StatusEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
