package com.classitda.authentication.infra.security.jwt;

import com.classitda.authentication.domain.TokenUse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenEncoder {

    private final JwtEncoder jwtEncoder;

    public JwtTokenEncoder(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String encode(
            TokenUse tokenUse,
            String subject,
            String jti,
            Duration ttl
    ) {
        Instant issuedAt = Instant.now();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtContract.ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .id(jti)
                .subject(subject)
                .claim(JwtContract.TOKEN_USE_CLAIM, tokenUse.name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
