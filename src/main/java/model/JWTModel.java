package model;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import service.AuthService;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


public class JWTModel implements AuthService {

    // 로그 저장 리스트
    private final List<String> validationLogs = new ArrayList<>();

    // 로그 전체 조회
    public List<String> getValidationLogs() {
        return new ArrayList<>(validationLogs); // 외부 수정 방지용 복사본
    }

    // 로그 기록 유틸
    private void log(String message) {
        String timestamp = new Date().toString();
        validationLogs.add("[" + timestamp + "] " + message);
    }

    // 비밀키와 공개키 (RSA 방식 예시)
    private KeyPair keyPair;

    // 비밀키 유효시간 (밀리초 단위), 예: 1시간
    private long secretKeyExpireMillis = 3600_000L;

    // 비밀키 생성 시각
    private long secretKeyCreatedAt = 0L;

    // 토큰 유효시간 (예: 15분)
    private long tokenExpireMillis = 900_000L;

    public JWTModel() {
        generateKeyPair();
    }

    // 비밀키-공개키 페어 생성 및 갱신
    private void generateKeyPair() {
        keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        secretKeyCreatedAt = System.currentTimeMillis();
    }

    public String generateToken(String userId) {
        return this.getJWTToken(userId, null);
    }


    // 공개키 반환 (Base64 인코딩 문자열 형태로 반환 가능)
    public String getPublicKeyString() {
        PublicKey pubKey = keyPair.getPublic();
        return java.util.Base64.getEncoder().encodeToString(pubKey.getEncoded());
    }

    // 비밀키 유효시간 검사 및 자동 갱신
    private void checkAndRenewKey() {
        long now = System.currentTimeMillis();
        if (now - secretKeyCreatedAt > secretKeyExpireMillis) {
            generateKeyPair();
            // 필요시 클라이언트에 갱신된 공개키 전송 로직 추가
        }
    }

    // 외부 호출용: JWT 토큰 생성
    public String getJWTToken(String subject, Claims additionalClaims) {
        checkAndRenewKey();

        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + tokenExpireMillis))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256);

        if (additionalClaims != null) {
            builder.addClaims(additionalClaims);
        }

        return builder.compact();
    }

    // 외부 호출용: JWT 토큰 읽기 및 유효성 검사
    public Jws<Claims> readJWTToken(String jwtToken) {
        checkAndRenewKey();

        PublicKey publicKey = keyPair.getPublic();

        JwtParser parser = Jwts.parser().verifyWith(publicKey).build();
        return parser.parseSignedClaims(jwtToken);
    }

    // 토큰 유효성 체크 예시
    public boolean validateToken(String jwtToken) {
        try {
            readJWTToken(jwtToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // (추가) 비밀키 유효시간 반환
    public long getSecretKeyExpireMillis() {
        return secretKeyExpireMillis;
    }

    // (추가) 토큰 유효시간 반환
    public long getTokenExpireMillis() {
        return tokenExpireMillis;
    }

    @Override
    public boolean authenticate(String jwtToken) {
        try {
            readJWTToken(jwtToken);
            log("Token validation success.");
            return true;
        } catch (ExpiredJwtException e) {
            log("Token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            log("Unsupported JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            log("Malformed JWT: " + e.getMessage());
        } catch (SignatureException e) {
            log("Invalid signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log("Illegal argument during validation: " + e.getMessage());
        } catch (JwtException e) {
            log("General JWT error: " + e.getMessage());
        } catch (Exception e) {
            log("Unknown error during JWT validation: " + e.getMessage());
        }

        return false;
    }
}
