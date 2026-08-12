package com.classitda.authentication.application.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Token 패키지에서만 사용하는 refresh token 해싱 전용 유틸리티 클래스
 */
final class RefreshTokenHasher {

    private static final Pattern HASH_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    static String hash(String refreshToken) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("리프레시 토큰 해시를 생성할 수 없습니다.");
        }
    }

    static boolean matches(String refreshToken, String storedHash) {
        byte[] submitted = parseHash(hash(refreshToken));
        byte[] stored = parseHash(storedHash);

        return MessageDigest.isEqual(submitted, stored);
    }

    private static byte[] parseHash(String hash) {
        if (hash == null || !HASH_PATTERN.matcher(hash).matches()) {
            throw new IllegalArgumentException("리프레시 토큰 해시 형식이 올바르지 않습니다.");
        }
        return HexFormat.of().parseHex(hash);
    }

    private RefreshTokenHasher() {
    }
}
