package com.example.note;

import java.io.Serializable;

public class Note implements Serializable {
    private String title;
    private String description;

    public Note(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toFileString() {
        return title + "||" + description;
    }

    public static Note fromFileString(String line) {
        String[] parts = line.split("\\|\\|");
        if (parts.length == 2) {
            return new Note(parts[0], parts[1]);
        }
        return new Note("", "");
    }
}