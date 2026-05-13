package com.uit.minhho.financetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private long timestamp;
    private String note;
    private int categoryId;
    private int walletId;
    private int partnerId; // New field for Partner link
    private boolean isIncome;
    private boolean isBusiness;

    public Transaction(double amount, long timestamp, String note, int categoryId, int walletId, int partnerId, boolean isIncome, boolean isBusiness) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = note;
        this.categoryId = categoryId;
        this.walletId = walletId;
        this.partnerId = partnerId;
        this.isIncome = isIncome;
        this.isBusiness = isBusiness;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }
    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }
    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }
    public boolean isBusiness() { return isBusiness; }
    public void setBusiness(boolean business) { isBusiness = business; }
}
