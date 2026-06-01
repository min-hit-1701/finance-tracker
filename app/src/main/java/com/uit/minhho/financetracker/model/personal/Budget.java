package com.uit.minhho.financetracker.model.personal;

public class Budget {
    private final int id;
    private final String categoryId;
    private final String categoryName;
    private final double limitAmount;
    private final double spentAmount;

    public Budget(String categoryId, String categoryName, double limitAmount, double spentAmount) {
        this(0, categoryId, categoryName, limitAmount, spentAmount);
    }

    public Budget(int id, String categoryId, String categoryName, double limitAmount, double spentAmount) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
    }

    public int getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public int getProgressPercent() {
        if (limitAmount == 0) return 0;
        return (int) ((spentAmount / limitAmount) * 100);
    }

    public boolean isOverBudget() {
        return spentAmount > limitAmount;
    }
}
