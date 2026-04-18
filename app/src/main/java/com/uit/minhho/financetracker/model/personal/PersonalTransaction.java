package com.uit.minhho.financetracker.model.personal;

public class PersonalTransaction {
    private final String id;
    private final String title;
    private final String note;
    private final double amount;
    private final String date;
    private final String categoryId;
    private final String walletId;
    private final boolean isIncome;

    public PersonalTransaction(String id, String title, String note, double amount, String date, String categoryId, String walletId, boolean isIncome) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.walletId = walletId;
        this.isIncome = isIncome;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getWalletId() {
        return walletId;
    }

    public boolean isIncome() {
        return isIncome;
    }
}
