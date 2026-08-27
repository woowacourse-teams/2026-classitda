package com.classitda.authentication.application.identity;

public record SocialIdentityVerificationResult(
        SocialIdentity identity,
        String nonceClaim
) {

    public SocialIdentityVerificationResult {
        if (identity == null) {
            throw new IllegalArgumentException("검증된 소셜 사용자 정보는 필수입니다.");
        }
        if (nonceClaim == null || nonceClaim.isBlank()) {
            throw new IllegalArgumentException("소셜 ID 토큰 nonce Claim은 필수입니다.");
        }
    }

    public static SocialIdentityVerificationResult of(SocialIdentity identity, String nonceClaim) {
        return new SocialIdentityVerificationResult(identity, nonceClaim);
    }
}
