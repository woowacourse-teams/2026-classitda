package com.classitda.authentication.application.session;

import java.util.Objects;
import java.util.regex.Pattern;

public record RefreshSession(String tokenHash, Long memberId, long expiresAtEpochSecond) {

    private static final Pattern TOKEN_HASH_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    public RefreshSession {
        Objects.requireNonNull(tokenHash, "리프레시 세션 토큰 해시는 필수입니다.");
        Objects.requireNonNull(memberId, "리프레시 세션 회원 ID는 필수입니다.");

        if (!TOKEN_HASH_PATTERN.matcher(tokenHash).matches()) {
            throw new IllegalArgumentException("리프레시 세션 토큰 해시 형식이 올바르지 않습니다.");
        }
        if (memberId < 1L) {
            throw new IllegalArgumentException("리프레시 세션 회원 ID는 1 이상이어야 합니다.");
        }
        if (expiresAtEpochSecond < 1L) {
            throw new IllegalArgumentException("리프레시 세션 만료 시각은 양수여야 합니다.");
        }
    }

    public static RefreshSession of(
            String tokenHash,
            Long memberId,
            long expiresAtEpochSecond
    ) {
        return new RefreshSession(tokenHash, memberId, expiresAtEpochSecond);
    }
}
