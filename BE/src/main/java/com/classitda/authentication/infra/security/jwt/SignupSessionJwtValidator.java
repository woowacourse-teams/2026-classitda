package com.classitda.authentication.infra.security.jwt;

import com.classitda.authentication.application.session.SignupSessionRegistry;
import com.classitda.authentication.domain.TokenUse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SignupSessionJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final SignupSessionRegistry signupSessionRegistry;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tokenUse = jwt.getClaimAsString(JwtContract.TOKEN_USE_CLAIM);
        if (!TokenUse.SIGNUP.name().equals(tokenUse)) {
            return OAuth2TokenValidatorResult.success();
        }

        String signupJti = jwt.getId();
        if (signupJti == null || signupJti.isBlank() || !signupJti.equals(jwt.getSubject())) {
            return invalidToken("가입 토큰 식별자(jti/sub)가 올바르지 않습니다.");
        }

        if (!signupSessionRegistry.hasActiveSession(signupJti)) {
            return invalidToken("활성 가입 세션이 존재하지 않습니다.");
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult invalidToken(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                BearerTokenErrorCodes.INVALID_TOKEN,
                description,
                null));
    }
}
