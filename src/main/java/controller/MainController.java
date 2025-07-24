package controller;

import service.AuthService;
import service.TestAuthService;
import model.*;

public class MainController {

    private final AuthService authService = new TestAuthService(); // 인증 서비스
    private final JWTModel jwtModel = new JWTModel(); // JWT 서비스 (직접 구현 필요)

    // 1. 테이블 데이터 가져오기
    public String fetchTableData(String tableName) {
        // 예시 응답
        return "item%1%카테고리1%상품A|2%카테고리2%상품B";
    }

    // 2. 로그인 처리
    public String login(String credentials) {
        boolean result = authService.authenticate(credentials);
        return "loginResult%" + (result ? "success" : "fail");
    }

    // 3. 토큰 생성
    public String createToken(String userId) {
        String token = jwtModel.generateToken(userId);
        return "token%" + token;
    }

    // 4. 공개키 반환
    public String getPublicKey() {
        String publicKey = jwtModel.getPublicKeyString();
        return "publicKey%" + publicKey;
    }
}
