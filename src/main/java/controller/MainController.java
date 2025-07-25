package controller;

import service.AuthService;
import service.TestAuthService;
import model.*;

import java.util.Map;
import java.util.HashMap;

public class MainController {

    private final AuthService authService = new TestAuthService(); // 인증 서비스
    private final JWTModel jwtModel = new JWTModel(); // JWT 서비스 (직접 구현 필요)

    // 1. 테이블 데이터 가져오기
    public String fetchTableData(String tableName) {
        // 예시 응답
        return "item%1%카테고리1%상품A|2%카테고리2%상품B";
    }

    // 2. 로그인 처리
    public String login(DataStruct data) {
        Map<String, String> paramMap = dataStructToMap(data);
        System.out.println("[로그인 시도] 입력 데이터: " + paramMap);
        try {
            boolean result = authService.authenticate(paramMap); // identity서버로 전달
            if (result) {
                System.out.println("[로그인 성공] id: " + paramMap.get("id"));
                // TODO: 토큰 생성 및 반환 (곽현빈 담당)
                return "loginResult%success%token$TODO";
            } else {
                System.out.println("[로그인 실패] 인증 실패, id: " + paramMap.get("id"));
                return "loginResult%fail%error%Invalid credentials";
            }
        } catch (Exception e) {
            System.out.println("[로그인 예외] " + e.getMessage());
            return "loginResult%fail%error%Exception: " + e.getMessage();
        }
    }

    public String signup(DataStruct data) {
        Map<String, String> paramMap = dataStructToMap(data);
        System.out.println("[회원가입 시도] 입력 데이터: " + paramMap);
        try {
            boolean result = authService.signup(paramMap); // identity서버로 전달
            if (result) {
                System.out.println("[회원가입 성공] id: " + paramMap.get("id"));
                return "signupResult%success";
            } else {
                System.out.println("[회원가입 실패] id: " + paramMap.get("id"));
                return "signupResult%fail%error%Signup failed";
            }
        } catch (Exception e) {
            System.out.println("[회원가입 예외] " + e.getMessage());
            return "signupResult%fail%error%Exception: " + e.getMessage();
        }
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

    private Map<String, String> dataStructToMap(DataStruct data) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", data.getId());
        paramMap.put("password", data.getPassword());
        return paramMap;
    }
}
