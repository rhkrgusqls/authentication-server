package com.example.demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DemoApplication {
    public static void main(String[] args) {
        int listenPort = 2020;

        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println("server-connector listening on " + listenPort);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("client-app connected: " + client.getInetAddress() + ":" + client.getPort());

                // do auth
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String auth = in.readLine();
                if (auth != null && auth.equals("allow")) {
                    System.out.println("auth success");

                    // launch client-socket as a new process, pass IP/port
                    String clientIp = client.getInetAddress().getHostAddress();
                    int clientPort = client.getPort();

                    ProcessBuilder pb = new ProcessBuilder(
                            "java", "-jar",
                            "../client-socket/target/client-socket-1.0-SNAPSHOT.jar",
                            clientIp,
                            String.valueOf(clientPort)
                    );
                    pb.inheritIO();
                    pb.start();

                } else {
                    System.out.println("auth failed");
                }

                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
