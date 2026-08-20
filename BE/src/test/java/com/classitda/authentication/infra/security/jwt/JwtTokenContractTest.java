package com.classitda.authentication.infra.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.support.JwtTestSupport;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

class JwtTokenContractTest {

    @Test
    void 액세스_토큰_JWT는_RS256과_공통_claim_및_MEMBER_권한을_가진다() {
        // given
        JwtTestSupport jwtSupport = JwtTestSupport.create();
        JwtTokenEncoder jwtTokenEncoder = new JwtTokenEncoder(jwtSupport.encoder());
        SignupSessionStore signupSessionStore = mock(SignupSessionStore.class);
        JwtDecoder jwtDecoder = jwtSupport.decoder(new SignupSessionJwtValidator(signupSessionStore));

        // when
        String token = jwtTokenEncoder.encode(TokenUse.ACCESS, "42", "access-jti", Duration.ofHours(1));
        Jwt jwt = jwtDecoder.decode(token);
        AbstractOAuth2TokenAuthenticationToken<Jwt> authentication =
                new JwtAuthenticationConverter().convert(jwt);

        // then
        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(JwtContract.ISSUER);
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getId()).isEqualTo("access-jti");
        assertThat(jwt.getClaimAsString(JwtContract.TOKEN_USE_CLAIM)).isEqualTo(TokenUse.ACCESS.name());
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));
        assertThat(authentication.getName()).isEqualTo("42");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly(TokenUse.ACCESS.authority());
    }
}
