package com.example.authenticationServer;

import java.io.*;
import java.net.*;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SocketServer {

    private final int port = 2000;

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Socket server listening on port " + port);

                while (true) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        System.out.println("Client connected: " + clientSocket.getInetAddress());

                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println("Received from client: " + inputLine);
                            out.println("Echo: " + inputLine);
                            if ("Goodbye".equalsIgnoreCase(inputLine.trim())) {
                                System.out.println("Client requested disconnect.");
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Client communication error: " + e.getMessage());
                    }
                    System.out.println("Client disconnected.");
                }
            } catch (IOException e) {
                System.err.println("Could not start server on port " + port);
                e.printStackTrace();
            }
        }).start();
    }
}
