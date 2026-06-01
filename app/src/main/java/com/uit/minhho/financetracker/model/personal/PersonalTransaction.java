package com.uit.minhho.financetracker.model.personal;

public class PersonalTransaction {
    private int id;
    private String title;
    private String subtitle;
    private String amount;
    private boolean isIncome;
    private String iconType;

    public PersonalTransaction(String title, String subtitle, String amount, boolean isIncome, String iconType) {
        this(0, title, subtitle, amount, isIncome, iconType);
    }

    public PersonalTransaction(int id, String title, String subtitle, String amount, boolean isIncome, String iconType) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.isIncome = isIncome;
        this.iconType = iconType;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public String getIconType() { return iconType; }
}
