package com.classitda.authentication.application.token;

import com.classitda.authentication.application.session.SignupSessionRegistry;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignupTokenIssuer {

    private final SignupSessionRegistry signupSessionRegistry;
    private final JwtTokenEncoder jwtTokenEncoder;
    private final TokenProperties tokenProperties;

    public String issueSignupToken(
            OauthProvider provider,
            String providerSubject,
            String profileImageUrl
    ) {
        validateProvider(provider);
        validateProviderSubject(providerSubject);

        String signupJti = UUID.randomUUID().toString();
        String signupToken = jwtTokenEncoder.encode(
                TokenUse.SIGNUP,
                signupJti,
                signupJti,
                tokenProperties.signupTtl());

        signupSessionRegistry.save(signupJti, provider, providerSubject, profileImageUrl);
        return signupToken;
    }

    private void validateProvider(OauthProvider provider) {
        if (provider == null) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_REQUIRED);
        }
    }

    private void validateProviderSubject(String providerSubject) {
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_SUBJECT_REQUIRED);
        }
    }
}
