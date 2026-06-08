package com.chat.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a message sent between clients in the chat system
 * inlcuding:
 * <ul>
 * <li>username</li>
 * <li>The message content</li>
 * <li>A timestamp indicating when the message was created</li>
 * <li>indication of whether it is a message from the system or a user</li>
 * </ul>
 * 
 * <p>The class implements {@link Serializable} so that message
 * objects can be sent across network streams</p>
 * 
 * @author Xen Stathatos
 */

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String sender;
    private final String content;
    private final String timestamp;
    private final boolean systemMessage;

    /**
     * Creates a standard user message
     * @param sender
     * @param content
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.systemMessage = false;
    }

    /**
     * Creates a new system message
     * @param content
     * @return a new system message instance
     */
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

    /**
     * Returns a formatted string representation of the message.
     * @return the formatted message string
     */
    @Override
    public String toString() {
        if (systemMessage) {
            return String.format("[%s] %s", timestamp, content);
        }
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}

    

