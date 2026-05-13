package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "businesses")
public class BusinessEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String type;
    private String taxCode;
    private String note;

    public BusinessEntity(String name, String type, String taxCode, String note) {
        this.name = name;
        this.type = type;
        this.taxCode = taxCode;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
