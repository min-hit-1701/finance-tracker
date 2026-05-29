package com.uit.minhho.financetracker.model.personal;

public class Wallet {
    private final String id;
    private final String name;
    private final double balance;
    private final String type; // e.g., Cash, Bank, E-Wallet

    public Wallet(String id, String name, double balance, String type) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }
}
