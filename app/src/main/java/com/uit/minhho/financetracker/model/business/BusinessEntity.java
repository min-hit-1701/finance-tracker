package com.uit.minhho.financetracker.model.business;

public class BusinessEntity {
    private final String name;
    private final String type;
    private final String note;

    public BusinessEntity(String name, String type, String note) {
        this.name = name;
        this.type = type;
        this.note = note;
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
}
