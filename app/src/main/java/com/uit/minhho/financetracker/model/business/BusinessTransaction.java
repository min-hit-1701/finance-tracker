package com.uit.minhho.financetracker.model.business;

public class BusinessTransaction {
    private final String title;
    private final String subtitle;
    private final String amount;
    private final boolean income;

    public BusinessTransaction(String title, String subtitle, String amount, boolean income) {
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.income = income;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isIncome() {
        return income;
    }
}
