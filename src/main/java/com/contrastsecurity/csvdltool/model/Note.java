package com.contrastsecurity.csvdltool.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Note {
    private String note;
    private String creation;
    private String creator;
    private String last_modification;
    private String last_updater;

    public String getNote() {
        return this.ncr(note);
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

    @Override
    public String toString() {
        return this.getNote();
    }

    private static final Pattern P = Pattern.compile("&#(x([0-9a-f]+)|([0-9]+));", Pattern.CASE_INSENSITIVE);

    private boolean isHex(final String str) {
        final char x = str.charAt(0);
        return 'x' == x || 'X' == x;
    }

    public String ncr(final String str) {
        final StringBuffer rtn = new StringBuffer();
        final Matcher matcher = P.matcher(str);
        while (matcher.find()) {
            final String group = matcher.group(1);
            int parseInt;
            if (isHex(group)) {
                parseInt = Integer.parseInt(group.substring(1), 16);
            } else {
                parseInt = Integer.parseInt(group, 10);
            }

            final char c;
            if (0 != (0x0ffff & parseInt)) {
                c = (char) parseInt;
            } else {
                c = '?';
            }
            matcher.appendReplacement(rtn, Character.toString(c));
        }
        matcher.appendTail(rtn);

        return rtn.toString();
    }

}
