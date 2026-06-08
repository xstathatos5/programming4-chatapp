package com.chat.server.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class responible for logging server events.
 * 
 * This class logs messages to both:
 * <ul>
 * <li>The console</li>
 * <li>A log file {@code server.log.txt}</li>
 * </ul>
 * 
 * <p>Each log entry includes a timestamp, event type and message.
 * The logging is thread safe to support concurrent access</p>
 */

public class ServerLogger {
    private static final String LOG_FILE = "server.log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Writes a log entry to both the console and the log file
     * @param eventType
     * @param message
     */
    public static synchronized void log(String eventType, String message){
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s", timestamp, eventType, message);

        System.out.println(logEntry);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
