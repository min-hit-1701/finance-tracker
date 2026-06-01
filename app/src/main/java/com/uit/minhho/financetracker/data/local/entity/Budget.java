package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int categoryId;
    private double limitAmount;
    private double spentAmount;
    private String period; // "week", "month", "year"
    private boolean isBusiness;

    @Ignore
    public Budget(int categoryId, double limitAmount, double spentAmount, String period) {
        this("", categoryId, limitAmount, spentAmount, period, false);
    }

    public Budget(String name, int categoryId, double limitAmount, double spentAmount, String period, boolean isBusiness) {
        this.name = name;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.period = period;
        this.isBusiness = isBusiness;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public boolean isBusiness() { return isBusiness; }
    public void setBusiness(boolean business) { isBusiness = business; }
}
