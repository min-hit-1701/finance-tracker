package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int iconRes;
    private String colorHex;
    private boolean isIncome;

    public Category(String name, int iconRes, String colorHex, boolean isIncome) {
        this.name = name;
        this.iconRes = iconRes;
        this.colorHex = colorHex;
        this.isIncome = isIncome;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }
}
