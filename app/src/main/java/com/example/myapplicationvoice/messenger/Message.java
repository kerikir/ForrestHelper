package com.example.myapplicationvoice.messenger;

public class Message {
    private String text;
    private boolean isMine;
    private String senderName;

    public Message(String text, boolean isMine, String senderName) {
        this.text = text;
        this.isMine = isMine;
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public boolean isMine() {
        return isMine;
    }

    public String getSenderName() {
        return senderName;
    }
}
