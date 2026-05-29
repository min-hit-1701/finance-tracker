package com.uit.minhho.financetracker.model;

public class ChatMessage {
    private String text;
    private boolean isBot;

    public ChatMessage(String text, boolean isBot) {
        this.text = text;
        this.isBot = isBot;
    }

    public String getText() {
        return text;
    }

    public boolean isBot() {
        return isBot;
    }
}
