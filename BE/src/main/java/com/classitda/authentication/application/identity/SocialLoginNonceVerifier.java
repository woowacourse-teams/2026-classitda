package com.classitda.authentication.application.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SocialLoginNonceVerifier {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final Pattern LOWERCASE_SHA_256_HEX = Pattern.compile("[0-9a-f]{64}");

    public boolean matches(String rawNonce, String nonceClaim) {
        if (rawNonce == null || nonceClaim == null || !LOWERCASE_SHA_256_HEX.matcher(nonceClaim).matches()) {
            return false;
        }

        byte[] hashedRawNonce = hash(rawNonce);
        byte[] decodedTokenNonce = HexFormat.of().parseHex(nonceClaim);
        return MessageDigest.isEqual(hashedRawNonce, decodedTokenNonce);
    }

    private byte[] hash(String rawNonce) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            return messageDigest.digest(rawNonce.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
