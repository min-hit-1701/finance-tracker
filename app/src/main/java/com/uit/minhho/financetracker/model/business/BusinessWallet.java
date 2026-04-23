package com.uit.minhho.financetracker.model.business;

public class BusinessWallet {
    private final String name;
    private final String balance;
    private final String note;

    public BusinessWallet(String name, String balance, String note) {
        this.name = name;
        this.balance = balance;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public String getBalance() {
        return balance;
    }

    public String getNote() {
        return note;
    }
}
