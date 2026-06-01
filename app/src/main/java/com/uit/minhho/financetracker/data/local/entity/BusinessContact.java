package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "business_contacts")
public class BusinessContact {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String type;
    private String note;

    public BusinessContact(String name, String type, String note) {
        this.name = name;
        this.type = type;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
