package com.chat.server.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
    private final int port;
    private final ChatServer chatServer;
    private HttpServer httpServer;

    public WebServer(int port, ChatServer chatServer) {
        this.port = port;
        this.chatServer = chatServer;
    }

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

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int activeUsers = chatServer.getClientCount();
            long totalMessages = chatServer.getTotalMessages();
            String response = "<html><head><title>Chat Server Dashboard</title></head><body>" +
                    "<h1>Chat Server Dashboard</h1>" +
                    "<p>Connected clients: " + activeUsers + "</p>" +
                    "<p>Total messages: " + totalMessages + "</p>" +
                    "</body></html>";

            byte[] bytes = response.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
    
}
