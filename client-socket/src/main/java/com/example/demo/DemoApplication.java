package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class DemoApplication {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar client-socket.jar <clientIp> <clientPort>");
            System.exit(1);
        }

        String clientIp = args[0];
        int clientPort = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(clientIp, clientPort)) {
            System.out.println("client-socket connected to client-app at " + clientIp + ":" + clientPort);

            // you can now communicate with client-app
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write("hello from client-socket\n");
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("client-app says: " + line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
