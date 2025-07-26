package controller;

import model.JWTModel;
import model.identityServerConnector;
import service.AuthService;
import service.TestAuthService;
import service.TokenService;
import controller.ParsingController.DataStruct;

import java.util.Map;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    private static Map<String, String> dataStructToMap(DataStruct data) {
        Map<String, String> map = new java.util.HashMap<>();
        if (data.id != null && data.id.length > 0) map.put("id", data.id[0]);
        if (data.password != null && data.password.length > 0) map.put("password", data.password[0]);
        // 필요시 추가
        return map;
    }

    public static String login(String refreshToken) {
        AuthService auth = new TestAuthService();
        System.out.println("[로그인 시도] 입력 데이터: " + refreshToken);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return "loginResult%error%Null refreshToken";
        }

        try {
            if (auth.authenticate(refreshToken)) {
                TokenService tokenService = TokenService.getInstance();
                tokenService.initializeKeys();  // 수동 초기화 필수!

                String userId = tokenService.extractUserIdFromRefreshToken(refreshToken);
                String accessToken = tokenService.generateAccessToken(userId);

                return "login%&accessToken$" + accessToken;
            }
            return "login%error%Invalid refreshToken";
        } catch (Exception e) {
            e.printStackTrace();
            return "login%error%" + e.getMessage();
        }
    }

    // 간단히 refreshToken에서 userId를 추출하는 예시 함수 (실제 구현에 맞게 수정 필요)
    private static String extractUserIdFromRefreshToken(String refreshToken) {
        TokenService tokenService = TokenService.getInstance();
        return tokenService.extractUserIdFromRefreshToken(refreshToken);
    }


    public static String login(String id, String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("password", password);

        System.out.println("[로그인 시도] 입력 데이터: " + paramMap);
        if (!paramMap.containsKey("id") || !paramMap.containsKey("password")) {
            System.out.println("[로그인 실패] id 또는 password 누락");
            return null; // 또는 예외 throw 고려 가능
        }

        AuthService authService = new TestAuthService(); // 또는 실제 AuthService 구현체
        identityServerConnector authServiceServer = new identityServerConnector();
        Map<String, String> tokenSet = null;
        try {
            TokenService tokenService = TokenService.getInstance();
            tokenSet = authServiceServer.requestTokensFromExternalServer(paramMap);
            System.out.println("tokens = " + tokenSet);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to request tokens from identity server");
            e.printStackTrace();
            return null; // 실패 시 처리
        }
        return login(tokenSet.get("refreshToken"));
    }


    public static String getAccessTokenPublicKey() {
        TokenService tokenService = TokenService.getInstance();
        String acPublicKeyPem = tokenService.getPublicKey();
        // PEM 개행 문자 -> \n 으로 변환
        return "%acpublicKey%&acKey$" + acPublicKeyPem.replace("\r", "").replace("\n", "\\n");
    }

    // 나머지 메서드들도 동일하게 필요시 new로 객체 생성해서 구현 가능

}
