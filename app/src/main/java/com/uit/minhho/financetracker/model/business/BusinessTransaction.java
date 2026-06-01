package com.uit.minhho.financetracker.model.business;

public class BusinessTransaction {
    private final int id;
    private final String title;
    private final String subtitle;
    private final String amount;
    private final boolean income;
    private final double rawAmount;
    private final long timestamp;

    public BusinessTransaction(String title, String subtitle, String amount, boolean income) {
        this(0, title, subtitle, amount, income, 0.0, 0L);
    }

    public BusinessTransaction(int id, String title, String subtitle, String amount, boolean income) {
        this(id, title, subtitle, amount, income, 0.0, 0L);
    }

    public BusinessTransaction(int id, String title, String subtitle, String amount, boolean income, double rawAmount, long timestamp) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.income = income;
        this.rawAmount = rawAmount;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
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

    public double getRawAmount() {
        return rawAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
