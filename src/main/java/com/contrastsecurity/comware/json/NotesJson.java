package com.contrastsecurity.comware.json;

import java.util.List;
import java.util.StringJoiner;

import com.contrastsecurity.comware.model.Note;

public class NotesJson extends ContrastJson {
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
