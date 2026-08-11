package com.classitda.authentication.application.token;

import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionRegistry;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
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

    public IssuedSignupToken issueSignupToken(
            OauthProvider provider,
            String providerSubject,
            String providerEmail
    ) {
        SignupSession signupSession = new SignupSession(provider, providerSubject, providerEmail);
        String signupJti = UUID.randomUUID().toString();
        String signupToken = jwtTokenEncoder.encode(
                TokenUse.SIGNUP,
                signupJti,
                signupJti,
                tokenProperties.signupTtl());

        signupSessionRegistry.save(signupJti, signupSession);
        return IssuedSignupToken.of(signupToken, tokenProperties.signupTtl().toSeconds());
    }
}
