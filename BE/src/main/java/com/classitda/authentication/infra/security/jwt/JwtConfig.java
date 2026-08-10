package com.classitda.authentication.infra.security.jwt;

import com.classitda.authentication.infra.security.properties.JwtProperties;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@EnableConfigurationProperties({JwtProperties.class, TokenProperties.class})
@Configuration
public class JwtConfig {

    private static final String ISSUER = "classitda";

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        RSAPrivateKey privateKey = jwtProperties.privateKey();
        if (!(privateKey instanceof RSAPrivateCrtKey privateCrtKey)) {
            throw new IllegalStateException("JWT 개인 키에는 RSA CRT 매개변수가 포함되어야 합니다.");
        }

        RSAKey rsaKey = new RSAKey.Builder(
                Base64URL.encode(privateCrtKey.getModulus()),
                Base64URL.encode(privateCrtKey.getPublicExponent()))
                .privateKey(privateCrtKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withPublicKey(jwtProperties.publicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return jwtDecoder;
    }
}
