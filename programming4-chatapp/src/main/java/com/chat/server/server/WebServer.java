package com.chat.server.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Lightweight HTTP server that provides a web dashboard for the
 * current server stats while the server is open.
 * 
 * The statistics shown are:
 * <ul>
 * <li>Number of connected clients</li>
 * <li>Total number of messages sent</li>
 * <li>List of currently connected users</li>
 * </ul>
 * 
 * <p>It runs alongside the main chat server
 * and uses {@link com.sun.net.httpserver.HttpServer}
 * to serve a simple HTML page</p>
 */

public class WebServer {
    private final int port;
    private final ChatServer chatServer;
    private HttpServer httpServer;

    /**
     * Creates a new web dashboard server.
     * 
     * @param port
     * @param chatServer
     */
    public WebServer(int port, ChatServer chatServer) {
        this.port = port;
        this.chatServer = chatServer;
    }

    /**
     * Starts the HTTP server and intialises the dashboard endpoint.
     * 
     * This method:
     * <ul>
     * <li>Creates the HTTP server bound to the specified port.</li>
     * <li>Registers the dashboard handler at the root path.</li>
     * <li>Starts listening for incoming http requests</li>
     * </ul>
     * 
     * <p>if the server fails and error is logged using
     * {@link ServerLogger}</p>
     */
    public void start() {
        try{
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", new DashboardHandler());
        httpServer.setExecutor(null);
        httpServer.start();
        ServerLogger.log("WEB", "Web dashboard started on port " + port);
        }catch (IOException e) {
            ServerLogger.log("ERROR", "Failed to start web server: " + e.getMessage());
        }
    }

    /**
     * Handles incoming HTTP requests and generates the simple HTML page.
     * 
     * <p>This handler retreives live data directly from {@link ChatServer}
     */
    private class DashboardHandler implements HttpHandler {
        @Override
        /**
         * Processes the HTTP and returns a generated HTML dashboard page
         * including real time statistics and a list of active users with their
         * inputted usernames
         * @param exchange contains request/response data
         * @throws IOException if an IO error occurs 
         */
        public void handle(HttpExchange exchange) throws IOException {
            int activeUsers = chatServer.getClientCount();
            long totalMessages = chatServer.getTotalMessages();
            StringBuilder userList = new StringBuilder("<ul>");

            for (String username : chatServer.getUsernames()) {
                userList.append("<li>").append(username).append("</li>");
            }
            userList.append("</ul>");

            String response = "<html><head><title>Chat Server Dashboard</title></head><body>" +
                    "<h1>Chat Server Dashboard</h1>" +
                    "<p>Connected clients: " + activeUsers + "</p>" +
                    "<p>Total messages: " + totalMessages + "</p>" +
                    "<p>User List: " + userList.toString() + "</p>" +
                    "</body></html>";

            byte[] bytes = response.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            
        }
    }
    /**
     * Escapes special HTML characters to prevent rendering issues.
     * 
     * @param input
     * @return a safe version of the input string
     */
    private String escapeHtml(String input) {
    if (input == null) return "";
    return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
