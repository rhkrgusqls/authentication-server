package service;

import java.util.Map;

public class IdentityService {
    public boolean authenticate(Map<String, String> params) {
        // TODO: 실제 DB에서 id, password 검증
        // 성공 시 true, 실패 시 false 반환
        return "testuser".equals(params.get("id")) && "1234".equals(params.get("password"));
    }

    public boolean signup(Map<String, String> params) {
        // TODO: 실제 DB에 사용자 정보 저장, 중복 체크, 비밀번호 해시 등
        // 성공 시 true, 실패 시 false 반환
        return true; // 임시로 항상 성공
    }
} 