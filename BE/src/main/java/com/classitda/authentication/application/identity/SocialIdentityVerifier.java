package com.classitda.authentication.application.identity;

import com.classitda.authentication.domain.OauthProvider;

public interface SocialIdentityVerifier {

    OauthProvider provider();

    SocialIdentity verify(String idToken);
}
