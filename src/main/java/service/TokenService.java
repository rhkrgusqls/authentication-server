package service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
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
import java.util.Map;
import io.jsonwebtoken.*;
@Service
public class TokenService implements AuthService {

    // application.properties에서 주입
    @Value("${jwt.expiration.access-token:3600000}")
    private long accessTokenExpirationTime;

    private KeyPair keyACPair;
    private KeyPair keyREPair;

    private static final String AC_PRIVATE = "ac_private.pem";
    private static final String AC_PUBLIC = "ac_public.pem";
    private static final String RE_PRIVATE = "re_private.pem";
    private static final String RE_PUBLIC = "re_public.pem";

    @PostConstruct
    public void init() {
        try {
            this.keyACPair = loadKeyPair(AC_PUBLIC, AC_PRIVATE);
            if (!isKeyPairValid(this.keyACPair)) {
                this.keyACPair = generateAndSaveKeyPair(AC_PUBLIC, AC_PRIVATE);
            }

            this.keyREPair = loadKeyPair(RE_PUBLIC, RE_PRIVATE);
            if (!isKeyPairValid(this.keyREPair)) {
                this.keyREPair = generateAndSaveKeyPair(RE_PUBLIC, RE_PRIVATE);
            }

        } catch (Exception e) {
            throw new RuntimeException("키 초기화 실패", e);
        }
    }

    private boolean isKeyPairValid(KeyPair keyPair) {
        return keyPair != null && keyPair.getPrivate() != null && keyPair.getPublic() != null;
    }

    public String extractUserIdFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser().verifyWith(keyREPair.getPublic()).build().parseSignedClaims(refreshToken).getPayload();
            return claims.getSubject();
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

        try (PEMParser pubReader = new PEMParser(new FileReader(pubFile));
             PEMParser privReader = new PEMParser(new FileReader(privFile))) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            SubjectPublicKeyInfo pubInfo = (SubjectPublicKeyInfo) pubReader.readObject();
            publicKey = converter.getPublicKey(pubInfo);

            PrivateKeyInfo privInfo = (PrivateKeyInfo) privReader.readObject();
            privateKey = converter.getPrivateKey(privInfo);
        }

        return new KeyPair(publicKey, privateKey);
    }

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .subject(username)
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
