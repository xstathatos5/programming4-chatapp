package com.chat.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String sender;
    private final String content;
    private final String timestamp;
    private final boolean systemMessage;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.systemMessage = false;
    }

    public static Message systemMessage(String content) {
        return new Message("SYSTEM", content, true);
    }

    private Message(String sender, String content, boolean systemMessage) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.systemMessage = systemMessage;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public boolean isSystemMessage() { return systemMessage; }

    @Override
    public String toString() {
        if (systemMessage) {
            return String.format("[%s] %s", timestamp, content);
        }
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}

    

