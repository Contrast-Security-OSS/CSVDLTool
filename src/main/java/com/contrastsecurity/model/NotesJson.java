package com.contrastsecurity.model;

import java.util.List;
import java.util.StringJoiner;

public class NotesJson extends Contrast {
    private List<Note> notes;

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        for (Note n : this.notes) {
            sj.add(n.toString());
        }
        return sj.toString();
    }

}
