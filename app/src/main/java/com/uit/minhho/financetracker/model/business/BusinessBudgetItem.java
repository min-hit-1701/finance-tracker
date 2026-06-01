package com.uit.minhho.financetracker.model.business;

public class BusinessBudgetItem {
    private final int id;
    private final String name;
    private final String categoryName;
    private final int used;
    private final int limit;

    public BusinessBudgetItem(String name, int used, int limit) {
        this(0, name, name, used, limit);
    }

    public BusinessBudgetItem(int id, String name, String categoryName, int used, int limit) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
        this.used = used;
        this.limit = limit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getUsed() {
        return used;
    }

    public int getLimit() {
        return limit;
    }

    public int getProgress() {
        if (limit <= 0) {
            return 0;
        }
        return Math.min(100, (used * 100) / limit);
    }
}
