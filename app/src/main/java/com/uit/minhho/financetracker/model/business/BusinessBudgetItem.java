package com.uit.minhho.financetracker.model.business;

public class BusinessBudgetItem {
    private final String name;
    private final int used;
    private final int limit;

    public BusinessBudgetItem(String name, int used, int limit) {
        this.name = name;
        this.used = used;
        this.limit = limit;
    }

    public String getName() {
        return name;
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
