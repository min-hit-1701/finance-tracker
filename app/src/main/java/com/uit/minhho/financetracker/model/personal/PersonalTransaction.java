package com.uit.minhho.financetracker.model.personal;

public class PersonalTransaction {
    private String title;
    private String subtitle;
    private String amount;
    private boolean isIncome;
    private String iconType;

    public PersonalTransaction(String title, String subtitle, String amount, boolean isIncome, String iconType) {
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.isIncome = isIncome;
        this.iconType = iconType;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
    public String getIconType() { return iconType; }
}