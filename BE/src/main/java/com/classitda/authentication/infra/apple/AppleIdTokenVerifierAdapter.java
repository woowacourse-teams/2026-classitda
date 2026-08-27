package com.classitda.authentication.infra.apple;

import com.classitda.authentication.application.identity.SocialIdentity;
import com.classitda.authentication.application.identity.SocialIdentityVerificationResult;
import com.classitda.authentication.application.identity.SocialIdentityVerifier;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

@Component
public class AppleIdTokenVerifierAdapter implements SocialIdentityVerifier {

    static final String ISSUER = "https://appleid.apple.com";
    static final String JWKS_URI = "https://appleid.apple.com/auth/keys";

    private static final String EMAIL_CLAIM = "email";
    private static final String NONCE_CLAIM = "nonce";

    private final JwtDecoder jwtDecoder;

    public AppleIdTokenVerifierAdapter(
            @Value("${auth.apple.ios-client-id}") String iosClientId,
            RestTemplateBuilder restTemplateBuilder
    ) {
        validateIosClientId(iosClientId);
        this.jwtDecoder = createJwtDecoder(iosClientId, restTemplateBuilder.build());
    }

    @Override
    public OauthProvider provider() {
        return OauthProvider.APPLE;
    }

    @Override
    public SocialIdentityVerificationResult verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw invalidIdToken();
        }

        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            SocialIdentity identity = SocialIdentity.of(
                    provider(),
                    jwt.getSubject(),
                    jwt.getClaimAsString(EMAIL_CLAIM)
            );
            return SocialIdentityVerificationResult.of(identity, jwt.getClaimAsString(NONCE_CLAIM));
        } catch (BadJwtException | IllegalArgumentException exception) {
            throw invalidIdToken();
        } catch (JwtException exception) {
            throw new IllegalStateException("Apple ID 토큰 검증 중 내부 오류가 발생했습니다.", exception);
        }
    }

    private void validateIosClientId(String iosClientId) {
        if (iosClientId == null || iosClientId.isBlank()) {
            throw new IllegalArgumentException("Apple OAuth iOS Client ID는 필수입니다.");
        }
    }

    private NimbusJwtDecoder createJwtDecoder(
            String iosClientId,
            RestOperations restOperations
    ) {
        NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder = NimbusJwtDecoder
                .withJwkSetUri(JWKS_URI)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations);

        NimbusJwtDecoder decoder = builder.build();
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        timestampValidator.setAllowEmptyExpiryClaim(false);
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
                timestampValidator,
                new JwtIssuerValidator(ISSUER),
                new JwtAudienceValidator(iosClientId))));
        return decoder;
    }

    private AuthException invalidIdToken() {
        return new AuthException(AuthErrorCode.APPLE_ID_TOKEN_INVALID);
    }
}
