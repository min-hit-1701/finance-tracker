package com.uit.minhho.financetracker.model.business;

public class BusinessEntity {
    private final int id;
    private final String name;
    private final String type;
    private final String note;

    public BusinessEntity(int id, String name, String type, String note) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.note = note;
    }

    public BusinessEntity(String name, String type, String note) {
        this(0, name, type, note);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return name;
    }
}
