package com.classitda.authentication.infra.google;

import com.classitda.authentication.application.identity.SocialIdentityVerifier;
import com.classitda.authentication.application.identity.SocialIdentity;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdTokenVerifierAdapter implements SocialIdentityVerifier {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifierAdapter(
            @Value("${auth.google.web-client-id}") String webClientId,
            @Value("${auth.google.ios-client-id}") String iosClientId
    ) {
        if (webClientId == null || webClientId.isBlank()) {
            throw new IllegalArgumentException("Google OAuth Web Client ID는 필수입니다.");
        }
        if (iosClientId == null || iosClientId.isBlank()) {
            throw new IllegalArgumentException("Google OAuth iOS Client ID는 필수입니다.");
        }

        NetHttpTransport transport = new NetHttpTransport.Builder().build();
        this.verifier = new GoogleIdTokenVerifier.Builder(
                transport,
                JSON_FACTORY)
                .setAudience(List.of(webClientId, iosClientId))
                .build();
    }

    @Override
    public OauthProvider provider() {
        return OauthProvider.GOOGLE;
    }

    @Override
    public SocialIdentity verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }

        GoogleIdToken googleIdToken = parse(idToken);
        verifySignatureAndClaims(googleIdToken);

        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }

        return toIdentity(payload);
    }

    SocialIdentity toIdentity(GoogleIdToken.Payload payload) {
        if (payload.getEmail() == null) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }

        try {
            return SocialIdentity.of(provider(), payload.getSubject(), payload.getEmail());
        } catch (IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }
    }

    private GoogleIdToken parse(String idToken) {
        try {
            return GoogleIdToken.parse(JSON_FACTORY, idToken);
        } catch (IOException | IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
        }
    }

    private void verifySignatureAndClaims(GoogleIdToken googleIdToken) {
        try {
            if (!verifier.verify(googleIdToken)) {
                throw new AuthException(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
            }
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("Google ID 토큰 검증 중 내부 오류가 발생했습니다.", exception);
        }
    }
}
