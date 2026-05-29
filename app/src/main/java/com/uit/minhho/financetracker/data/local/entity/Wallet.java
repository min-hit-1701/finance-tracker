package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class Wallet {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double balance;
    private String type;
    private boolean isBusiness;

    public Wallet(String name, double balance, String type, boolean isBusiness) {
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.isBusiness = isBusiness;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isBusiness() { return isBusiness; }
    public void setBusiness(boolean business) { isBusiness = business; }
}
