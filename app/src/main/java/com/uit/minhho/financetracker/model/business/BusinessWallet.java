package com.uit.minhho.financetracker.model.business;

public class BusinessWallet {
    private final int id;
    private final String name;
    private final String balance;
    private final String note;

    public BusinessWallet(int id, String name, String balance, String note) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.note = note;
    }

    public int getId() {
        return id;
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

    @Override
    public String toString() {
        return name;
    }
}
