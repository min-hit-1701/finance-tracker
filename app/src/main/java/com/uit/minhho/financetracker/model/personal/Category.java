package com.uit.minhho.financetracker.model.personal;

public class Category {
    private final String id;
    private final String name;
    private final int iconResId;
    private final boolean isIncome;

    public Category(String id, String name, int iconResId, boolean isIncome) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.isIncome = isIncome;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isIncome() {
        return isIncome;
    }
}
