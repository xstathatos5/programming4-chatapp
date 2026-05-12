package com.chat.server.model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String content;
    private String timestamp;
    private boolean isSystemMessage;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isSystemMessage = false;
    }

    public Message(String content){
        this("SERVER", content);
        this.isSystemMessage = true;
    }

    public String getSender() {return sender;}
    public String getContent() {return content;}
    public String getTimestamp() {return timestamp;}
    public boolean isSystemMessage() {return isSystemMessage;}

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
    

}
    

