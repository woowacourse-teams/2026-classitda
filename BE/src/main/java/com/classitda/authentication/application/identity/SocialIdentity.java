package com.classitda.authentication.application.identity;

import com.classitda.authentication.domain.OauthProvider;

public record SocialIdentity(
        OauthProvider provider,
        String providerSubject,
        String providerEmail
) {

    public SocialIdentity {
        if (provider == null) {
            throw new IllegalArgumentException("OAuth 제공자는 필수입니다.");
        }
        if (providerSubject == null || providerSubject.isBlank() || providerSubject.length() > 255) {
            throw new IllegalArgumentException("OAuth 제공자 사용자 식별자는 255자 이하여야 합니다.");
        }
        if (providerEmail != null && (providerEmail.isBlank() || providerEmail.length() > 254)) {
            throw new IllegalArgumentException("OAuth 제공자 이메일은 254자 이하여야 합니다.");
        }
    }

    public static SocialIdentity of(
            OauthProvider provider,
            String providerSubject,
            String providerEmail
    ) {
        return new SocialIdentity(provider, providerSubject, providerEmail);
    }
}
