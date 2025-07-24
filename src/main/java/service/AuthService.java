package service;

public interface AuthService {
    boolean authenticate(String input);

    default boolean authenticate(java.util.Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> sb.append(k).append("%").append(v).append("%"));
        return authenticate(sb.toString());
    }

    // 회원가입용 메서드 (임시)
    default boolean signup(java.util.Map<String, String> params) {
        // 실제 구현체에서 DB 중복 체크, 비밀번호 해시, 저장 등 구현 예정
        return true; // 임시로 항상 성공 반환
    }
} 