package service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.openssl.PEMKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.security.KeyPair;
import java.util.Date;
// 생략된 import 유지
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Security;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

@Service
public class TokenService implements AuthService {
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static TokenService instance;

    private TokenService() {
        // private 생성자: 외부에서 직접 생성 불가
        try {
            this.keyACPair = loadKeyPair(AC_PUBLIC, AC_PRIVATE);
            this.keyREPair = loadKeyPair(RE_PUBLIC, RE_PRIVATE);
        } catch (Exception e) {
            throw new RuntimeException("키 초기화 실패", e);
        }
    }

    public static synchronized TokenService getInstance() {
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }


    public void initializeKeys() {
        try {
            // Access token 키 불러오기 또는 생성
            try {
                this.keyACPair = loadKeyPair(AC_PUBLIC, AC_PRIVATE);
                if (!isKeyPairValid(this.keyACPair)) {
                    this.keyACPair = generateAndSaveKeyPair(AC_PUBLIC, AC_PRIVATE);
                }
            } catch (Exception e) {
                this.keyACPair = generateAndSaveKeyPair(AC_PUBLIC, AC_PRIVATE);
            }

            // Refresh token 키는 반드시 존재해야 하며, 유효하지 않으면 실패 처리
            try {
                this.keyREPair = loadKeyPair(RE_PUBLIC, RE_PRIVATE);
                if (!isKeyPairValid(this.keyREPair)) {
                    throw new RuntimeException("리프레시 키 쌍이 유효하지 않습니다.");
                }
            } catch (Exception e) {
                throw new RuntimeException("리프레시 키 불러오기 실패", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("키 초기화 실패", e);
        }
    }



    // application.properties에서 주입
    @Value("${jwt.expiration.access-token:3600000}")
    private long accessTokenExpirationTime;

    private KeyPair keyACPair;
    private KeyPair keyREPair;

    private static final String AC_PRIVATE = "./ac_private.pem";
    private static final String AC_PUBLIC = "./ac_public.pem";
    private static final String RE_PRIVATE = "./re_private.pem";
    private static final String RE_PUBLIC = "./re_public.pem";

    @PostConstruct
    public void init() {
        try {
            this.keyACPair = loadKeyPair(AC_PUBLIC, AC_PRIVATE);
            if (!isKeyPairValid(this.keyACPair)) {
                this.keyACPair = generateAndSaveKeyPair(AC_PUBLIC, AC_PRIVATE);
            }
        } catch (Exception e) {
            try {
                this.keyACPair = generateAndSaveKeyPair(AC_PUBLIC, AC_PRIVATE);
            } catch (Exception ex) {
                throw new RuntimeException("AC 키 생성 실패", ex);
            }
        }

        try {
            this.keyREPair = loadKeyPair(RE_PUBLIC, RE_PRIVATE);
            if (!isKeyPairValid(this.keyREPair)) {
                throw new RuntimeException("리프레시 키 쌍이 유효하지 않습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("리프레시 키 불러오기 실패", e);
        }
    }


    private boolean isKeyPairValid(KeyPair keyPair) {
        return keyPair != null && keyPair.getPrivate() != null && keyPair.getPublic() != null;
    }

    public Map<String, String> extractUserInfoFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keyREPair.getPublic())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String userId = claims.getSubject(); // "sub" 값
            String username = claims.get("username", String.class); // 커스텀 claim

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("username", username);

            return userInfo;

        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid refresh token", e);
        }
    }

    public void setKeyACPairByPEMStrings(String publicPem, String privatePem) throws Exception {
        try (
                PEMParser pubParser = new PEMParser(new StringReader(publicPem));
                PEMParser privParser = new PEMParser(new StringReader(privatePem))
        ) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            PublicKey publicKey = converter.getPublicKey((SubjectPublicKeyInfo) pubParser.readObject());
            PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) privParser.readObject());

            this.keyACPair = new KeyPair(publicKey, privateKey);

            try (JcaPEMWriter pubWriter = new JcaPEMWriter(new FileWriter(AC_PUBLIC));
                 JcaPEMWriter privWriter = new JcaPEMWriter(new FileWriter(AC_PRIVATE))) {
                pubWriter.writeObject(publicKey);
                privWriter.writeObject(privateKey);
            }
        }
    }
    private KeyPair generateAndSaveKeyPair(String pubFile, String privFile) throws Exception {
        // 리프레시 키 파일명이라면 무조건 예외 발생시켜서 생성 금지
        if (pubFile.equals(RE_PUBLIC) || privFile.equals(RE_PRIVATE)) {
            throw new UnsupportedOperationException("Refresh token key pair generation is forbidden.");
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        try (JcaPEMWriter pubWriter = new JcaPEMWriter(new FileWriter(pubFile));
             JcaPEMWriter privWriter = new JcaPEMWriter(new FileWriter(privFile))) {
            pubWriter.writeObject(keyPair.getPublic());
            privWriter.writeObject(keyPair.getPrivate());
        }

        return keyPair;
    }

    private KeyPair loadKeyPair(String pubFile, String privFile) throws Exception {
        PublicKey publicKey;
        PrivateKey privateKey;

        try (
                PEMParser pubReader = new PEMParser(new FileReader(pubFile));
                PEMParser privReader = new PEMParser(new FileReader(privFile))
        ) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            Object pubObject = pubReader.readObject();
            SubjectPublicKeyInfo pubInfo = (SubjectPublicKeyInfo) pubObject;
            publicKey = converter.getPublicKey(pubInfo);

            Object privObject = privReader.readObject();
            if (privObject instanceof PEMKeyPair) {
                privateKey = converter.getKeyPair((PEMKeyPair) privObject).getPrivate();
            } else if (privObject instanceof PrivateKeyInfo) {
                privateKey = converter.getPrivateKey((PrivateKeyInfo) privObject);
            } else {
                throw new IllegalArgumentException("지원하지 않는 개인키 형식: " + privObject.getClass().getName());
            }
        }

        return new KeyPair(publicKey, privateKey);
    }
    public String generateAccessToken(String userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(userId)                 // sub: username
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(keyACPair.getPrivate())
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(keyACPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않는 또는 만료된 토큰입니다.", e);
        }
    }

    public String getPublicKey() {
        try (StringWriter sw = new StringWriter();
             JcaPEMWriter writer = new JcaPEMWriter(sw)) {
            writer.writeObject(keyACPair.getPublic());
            writer.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("공개키 반환 실패", e);
        }
    }

    public String extractUserIdFromRefreshTokenWithPEM(String refreshToken, String publicKeyPem) {
        try (PEMParser pemParser = new PEMParser(new StringReader(publicKeyPem))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            SubjectPublicKeyInfo pubKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            PublicKey publicKey = converter.getPublicKey(pubKeyInfo);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            return claims.getSubject();

        } catch (Exception e) {
            throw new IllegalArgumentException("RefreshToken 해독 실패", e);
        }
    }


    @Override
    public boolean authenticate(String input) {
        return false;
    }

    @Override
    public boolean authenticate(Map<String, String> params) {
        return AuthService.super.authenticate(params);
    }

    @Override
    public boolean signup(Map<String, String> params) {
        return AuthService.super.signup(params);
    }
}