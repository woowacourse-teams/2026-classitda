package com.classitda.authentication.infra.security.properties;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(String privateKeyBase64, String publicKeyBase64) {

    public JwtProperties {
        if (privateKeyBase64 == null || privateKeyBase64.isBlank()) {
            throw new IllegalArgumentException("JWT 개인 키 설정은 필수입니다.");
        }
        if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
            throw new IllegalArgumentException("JWT 공개 키 설정은 필수입니다.");
        }
    }

    public RSAPrivateKey privateKey() {
        try {
            byte[] encodedKey = Base64.getDecoder().decode(privateKeyBase64.trim());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("JWT 개인 키 설정이 올바르지 않습니다.");
        }
    }

    public RSAPublicKey publicKey() {
        try {
            byte[] encodedKey = Base64.getDecoder().decode(publicKeyBase64.trim());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("JWT 공개 키 설정이 올바르지 않습니다.");
        }
    }

    @Override
    public String toString() {
        return "JwtProperties[privateKeyBase64=<redacted>, publicKeyBase64=<redacted>]";
    }
}
