package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import console.HTTPSConnect;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        try {
            HTTPSConnect server = new HTTPSConnect();
            server.connectClient(); // Netty + SSL 서버 실행
        } catch (Exception e) {
            System.err.println("[Fatal][Server] 서버 시작 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
