package util;

import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class KeyParsing {

    // 공개키를 Base64 문자열로 변환
    public static String extractPublicKeyAsString(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    public static PublicKey restorePublicKeyFromString(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}