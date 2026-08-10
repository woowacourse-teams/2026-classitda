package com.classitda.authentication.application.identity;

public interface GoogleIdentityVerifier {

    GoogleIdentity verify(String idToken);
}
