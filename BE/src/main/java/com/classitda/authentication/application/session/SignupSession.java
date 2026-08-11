package com.classitda.authentication.application.session;

import com.classitda.authentication.domain.OauthProvider;

public record SignupSession(
        OauthProvider provider,
        String providerSubject,
        String providerEmail
) {
    public SignupSession {
        if (provider == null
                || providerSubject == null
                || providerSubject.isBlank()
                || providerSubject.length() > 255
                || (providerEmail != null
                && (providerEmail.isBlank() || providerEmail.length() > 254))) {
            throw new IllegalStateException("가입 세션이 올바르지 않습니다.");
        }
    }
}
