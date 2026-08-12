package com.classitda.authentication.application.identity;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;

public record GoogleIdentity(String providerSubject, String providerEmail) {

    public GoogleIdentity {
        if (providerSubject == null || providerSubject.isBlank() || providerSubject.length() > 255) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }

        if (providerEmail == null || providerEmail.isBlank() || providerEmail.length() > 254) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }
    }

    public static GoogleIdentity of(String providerSubject, String providerEmail) {
        return new GoogleIdentity(providerSubject, providerEmail);
    }
}
