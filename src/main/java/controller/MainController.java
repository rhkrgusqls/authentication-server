package controller;

import model.JWTModel;
import model.identityServerConnector;
import service.AuthService;
import service.TestAuthService;
import service.TokenService;
import controller.ParsingController.DataStruct;

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
        if (refreshToken == null || refreshToken.isEmpty()) {
            return "loginResult%error%Null refreshToken";
        }
        if (auth.authenticate(refreshToken)) {
            // 로그인 성공 시 액세스 토큰 발급
            TokenService tokenService = new TokenService();
            // refreshToken에서 사용자 ID 추출하는 부분은 예시로 간단히 처리
            String userId = extractUserIdFromRefreshToken(refreshToken);

            String accessToken = tokenService.generateAccessToken(userId);

            // 로그인 성공과 액세스토큰 같이 반환
            return "loginResult%&accessToken$" + accessToken;
        }
        return "loginResult%error%Invalid refreshToken";
    }

    // 간단히 refreshToken에서 userId를 추출하는 예시 함수 (실제 구현에 맞게 수정 필요)
    private static String extractUserIdFromRefreshToken(String refreshToken) {
        TokenService tokenService = new TokenService();
        return tokenService.extractUserIdFromRefreshToken(refreshToken);
    }


    public static String login(DataStruct data) {
        Map<String, String> paramMap = dataStructToMap(data);
        System.out.println("[로그인 시도] 입력 데이터: " + paramMap);
        if (!paramMap.containsKey("id") || !paramMap.containsKey("password")) {
            System.out.println("[로그인 실패] id 또는 password 누락");
            return "loginResult%error%Missing id or password";
        }

        AuthService authService = new TestAuthService(); // 혹은 실제 AuthService 구현체
        identityServerConnector authServiceServer = new identityServerConnector();
        Map<String, String> tokenSet = authServiceServer.requestTokensFromExternalServer(paramMap);

        if (tokenSet == null || !tokenSet.containsKey("accessToken") || !tokenSet.containsKey("refreshToken")) {
            System.out.println("[로그인 실패] 외부서버 토큰발급 실패");
            return "loginResult%error%Token issuance failed";
        }

        // %login%&accessToken$실제토큰값 반환
        return "%login%&accessToken$" + tokenSet.get("accessToken") + "&accessToken$" + tokenSet.get("refreshToken");
    }

    public static String getAccessTokenPublicKey() {
        TokenService tokenService = new TokenService();
        String acPublicKeyPem = tokenService.getPublicKey();
        // PEM 개행 문자 -> \n 으로 변환
        return "%acpublicKey%&acKey$" + acPublicKeyPem.replace("\r", "").replace("\n", "\\n");
    }

    // 나머지 메서드들도 동일하게 필요시 new로 객체 생성해서 구현 가능

}
