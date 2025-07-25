package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class identityServerConnector {

    private final String serverHost = "localhost";  // 인증서버 호스트
    private final int serverPort = 2010;            // 인증서버 포트

    public Map<String, String> requestTokensFromExternalServer(Map<String, String> paramMap) {
        Map<String, String> result = new HashMap<>();
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // id, password 조합으로 명령어 형식 만듦 (예: login%id$someid&password$somepass)
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("login%");
            if (paramMap.containsKey("id")) {
                commandBuilder.append("id$").append(paramMap.get("id")).append("&");
            }
            if (paramMap.containsKey("password")) {
                commandBuilder.append("password$").append(paramMap.get("password"));
            }
            String command = commandBuilder.toString();

            // 명령어 전송
            out.println(command);

            // 서버로부터 응답 수신 (예: %login%&accessToken$abc123&refreshToken$def456)
            String response = in.readLine();
            if (response == null || !response.startsWith("%login%")) {
                return null; // 실패 시 null 반환
            }

            // 응답 파싱: &accessToken$abc123&refreshToken$def456
            String[] parts = response.split("&");
            for (String part : parts) {
                if (part.startsWith("accessToken$")) {
                    result.put("accessToken", part.substring("accessToken$".length()));
                } else if (part.startsWith("refreshToken$")) {
                    result.put("refreshToken", part.substring("refreshToken$".length()));
                }
            }

            // 액세스토큰, 리프레시토큰 모두 있으면 반환
            if (result.containsKey("accessToken") && result.containsKey("refreshToken")) {
                return result;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
