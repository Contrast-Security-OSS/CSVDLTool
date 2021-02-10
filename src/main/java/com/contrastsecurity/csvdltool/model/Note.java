package com.contrastsecurity.csvdltool.model;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class Note {
    private String note;
    private String creation;
    private String creator;
    private String last_modification;
    private String last_updater;
    private List<Property> properties;

    public String getNote() {
        // 文字実体参照を変換
        return StringEscapeUtils.unescapeHtml4(note);
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLast_modification() {
        return last_modification;
    }

    public void setLast_modification(String last_modification) {
        this.last_modification = last_modification;
    }

    public String getLast_updater() {
        return last_updater;
    }

    public void setLast_updater(String last_updater) {
        this.last_updater = last_updater;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return this.getNote();
    }

}
