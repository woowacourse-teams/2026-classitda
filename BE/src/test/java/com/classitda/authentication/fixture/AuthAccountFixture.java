package com.classitda.authentication.fixture;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;

public final class AuthAccountFixture {

    private AuthAccountFixture() {
    }

    public static AuthAccount 기본_인증_계정() {
        return 이메일이_있는_인증_계정("member@example.com");
    }

    public static AuthAccount 이메일이_있는_인증_계정(String providerEmail) {
        return 인증_계정(1L, "google-subject", providerEmail);
    }

    public static AuthAccount 인증_계정(
            Long memberId,
            String providerSubject,
            String providerEmail
    ) {
        return AuthAccount.builder()
                .memberId(memberId)
                .provider(OauthProvider.GOOGLE)
                .providerSubject(providerSubject)
                .providerEmail(providerEmail)
                .build();
    }

    public static AuthAccount 이메일이_없는_인증_계정() {
        return AuthAccount.builder()
                .memberId(1L)
                .provider(OauthProvider.GOOGLE)
                .providerSubject("google-subject")
                .providerEmail(null)
                .build();
    }
}
