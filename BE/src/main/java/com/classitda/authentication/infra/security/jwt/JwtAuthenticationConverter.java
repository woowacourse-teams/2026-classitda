package com.classitda.authentication.infra.security.jwt;

import com.classitda.authentication.domain.TokenUse;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractOAuth2TokenAuthenticationToken<Jwt>> {

    private static final String TOKEN_USE_CLAIM = "token_use";

    @Override
    public AbstractOAuth2TokenAuthenticationToken<Jwt> convert(Jwt jwt) {
        TokenUse tokenUse = parseTokenUse(jwt.getClaimAsString(TOKEN_USE_CLAIM));
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(tokenUse.authority());
        return new JwtAuthenticationToken(jwt, List.of(authority), jwt.getSubject());
    }

    private TokenUse parseTokenUse(String claim) {
        try {
            return TokenUse.valueOf(claim);
        } catch (IllegalArgumentException | NullPointerException exception) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "토큰 용도(token_use)가 없거나 지원하지 않는 값입니다.",
                    null);
            throw new OAuth2AuthenticationException(error);
        }
    }
}
