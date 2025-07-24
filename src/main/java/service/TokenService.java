package service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Date;

@Service
public class TokenService {
    // 토큰 만료 시간(application.properties 파일에서 관리하는 것이 좋다고 함)
    @Value("${jwt.expiration.access-token:3600000}") // 기본 1시간, 단위 ms
    private long accessTokenExpirationTime;

    // RSA 키페어
    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        // TODO: [중요] 실제 프로덕션 환경에서는 애플리케이션 재시작 시에도 동일한 키를 유지해야함
        // KeyStore(JKS)나 PEM파일 등 안전한 외부 저장소에서 비공개키/공개키를 불러와야 함
        // 현재는 서버시작 시 마다 새로운 RSA 키 페어를 임시로 생성
        this.keyPair = Jwts.SIG.RS256.keyPair().build();
        System.out.println("임시 RSA 키페어 생성, 프로덕션에서는 외부키를 사용하세요.");
    }

    /**
     * 사용자 ID를 받아 Access Token을 생성
     * @param userId 사용자 정보
     * @return 생성된 JWT 문자열
     */
    public String generateAccessToken(String userId) {
        //TODO : Access Token에는 민감하지 않은 최소한의 정보만 담을 것(유저아이디, 롤)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .subject(userId) // 토큰의 주체 (사용자 Id)
                .issuedAt(now) // 토큰 발급 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(keyPair.getPrivate()) // 비공개키로 서명
                .compact();
    }

    /**
     * 토큰의 유효성을 검증하고 토큰에 담긴 정보를 반환
     * @param token 검증할 토큰 문자열
     * @return 토큰의 Claims 객체
     */
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(keyPair.getPublic()) // 공개키로 서명 검증
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // 토큰 위변 조 혹은 형식이 잘 못되었을 때 예외처리
            throw new IllegalArgumentException("유효하지 않는 토큰 입니다.", e);
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 때
            throw new IllegalArgumentException("만료된 토큰 입니다.", e);
        }
    }

    public String getPublicKey() {
        return "공개키값";
    }
}
